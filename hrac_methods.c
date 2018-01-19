#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include "hrac.h"

//Deklarace funkci
int addHrac(int c_socket, char* nazev);

int removeHrac(int id);

int removeHracSoc(int clie_soc);

int getHracIndex(int socket);

int sendMessage(int client_soc, char *text);

int broadcastHraci(char *text);

int broadcastToLobby(int *hraciX, int SocketHraceVynechat, char *text);

int broadcastToAllLobby(int *hraciX, char *text);

struct Hrac* boostHraci();

struct Hrac* reduceHraci();

int initHraci();

int uvolniHrace();


//globalni promene
struct Hrac *hraci;
int length_hraci = 5;
int obsazeny_hraci = 0;

int id_plus = 1;

//MUTEX
pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;


int initHraci()
{
	hraci = (struct Hrac*)malloc(sizeof(struct Hrac) * length_hraci);
	for(int i = 0; i < length_hraci; i++)
	{
		hraci[i].init = 0;	
	}	
}

struct Hrac* boostHraci()
{
	struct Hrac *doubleHraci;
	doubleHraci = (struct Hrac*)malloc(sizeof(struct Hrac) * (length_hraci * 2));
	
	for(int i = 0; i < length_hraci; i++)
	{
		doubleHraci[i].client_socket = hraci[i].client_socket;
		doubleHraci[i].id = hraci[i].id;
		doubleHraci[i].stav = hraci[i].stav;
		strcpy(doubleHraci[i].jmeno, hraci[i].jmeno);
		doubleHraci[i].init = hraci[i].init;
	}
	
	for(int i = length_hraci; i < length_hraci * 2; i++)
	{
		hraci[i].init = 0;	
	}
	
	free(hraci);
	length_hraci *= 2;
	return doubleHraci;
}

struct Hrac* reduceHraci()
{
	struct Hrac *halfHraci;
	halfHraci = (struct Hrac*)malloc(sizeof(struct Hrac) * (length_hraci * 2));
	
	for(int i = 0; i < length_hraci; i++)
	{
		halfHraci[i].client_socket = hraci[i].client_socket;
		halfHraci[i].id = hraci[i].id;
		halfHraci[i].stav = hraci[i].stav;
		strcpy(halfHraci[i].jmeno, hraci[i].jmeno);
		halfHraci[i].init = hraci[i].init;
	}
	
	free(hraci);
	length_hraci /= 2;
	return halfHraci;
}

int uvolniHrace()
{
	free(hraci);
}

int getHracIndex(int socket)
{
	int ind = -1;
	//printf("length hraci: %d\n", length_hraci);
	for(int i = 0;i < length_hraci; i++)
	{
		//printf("%s\n",hraci[i].jmeno);
		if(hraci[i].init == 1)
		{
			if(hraci[i].client_socket == socket)
			{
				ind = i;
				break;
			}	
		}
	}
	//printf("index hrace v seznamu: %d\n", ind);
	return ind;
}

int addHrac(int c_socket, char *nazev)
{
	pthread_mutex_lock(&lock);
	
	if(obsazeny_hraci == length_hraci)
	{
		hraci = boostHraci();
	}
	int ind = -1;
	for(int i = 0; i < length_hraci; i++)
	{
		if(hraci[i].init == 0)
		{
			ind = i;
			break;
		}
	}
	
	if(ind == -1)
	{
		printf("CHYBA - nenasel misto v poli, nebo neinicializovane pole.\n");
		return 1;
	}
	
	hraci[ind].client_socket = c_socket;
	memset(&hraci[ind].jmeno, 0, sizeof(hraci[ind].jmeno));
	memcpy(hraci[ind].jmeno, nazev, sizeof(nazev));
	hraci[ind].id = id_plus++;
	hraci[ind].init = 1;
	hraci[ind].stav = 1;
	obsazeny_hraci++;
	
	pthread_mutex_unlock(&lock);
	return 0;
}

int removeHrac(int id)
{
	pthread_mutex_lock(&lock);
	
	int ind = -1;
	for(int i = 0; i < length_hraci; i++)
	{
		if(hraci[i].id == id)
		{
			ind = i;
			break;
		}
	}
	
	if(ind == -1)
	{
		pthread_mutex_unlock(&lock);
		printf("CHYBA - nenasel hrace s ID:%d v poli hracu!\n", ind);
		return 1;
	}
	
	hraci[ind].client_socket = -1;
	memset(&hraci[ind].jmeno, 0, sizeof(hraci[ind].jmeno));
	hraci[ind].id = -1;
	hraci[ind].stav = 0;
	hraci[ind].init = 0;
	obsazeny_hraci--;
	
	if((obsazeny_hraci * 2) == length_hraci)
	{
		hraci = reduceHraci();
	}
	
	pthread_mutex_unlock(&lock);
	return 0;
}

int removeHracSoc(int clie_soc)
{
	pthread_mutex_lock(&lock);
	
	int ind = -1;
	for(int i = 0; i < length_hraci; i++)
	{
		if(hraci[i].client_socket == clie_soc)
		{
			ind = i;
			break;
		}
	}
	
	if(ind == -1)
	{
		pthread_mutex_unlock(&lock);
		printf("CHYBA - nenasel hrace s SOCKETEM:%d v poli hracu!\n", ind);
		return 1;
	}
	
	hraci[ind].client_socket = -1;
	memset(&hraci[ind].jmeno, 0, sizeof(hraci[ind].jmeno));
	hraci[ind].id = -1;
	hraci[ind].stav = 0;
	hraci[ind].init = 0;
	obsazeny_hraci--;
	
	if((obsazeny_hraci * 2) == length_hraci)
	{
		hraci = reduceHraci();
	}
	
	pthread_mutex_unlock(&lock);
	return 0;
}

//Z nejakeho divneho duvodu musim text
//prevzaty z hlavni funkce prekopirovat
//do noveho pole charu, jelikoz pak se to
//neposle!!!
int broadcastHraci(char *text)
{
	pthread_mutex_lock(&lock);
	char text1[1000] = "";
	strcpy(text1, text);
	for(int i = 0; i < length_hraci; i++)
	{
		if(hraci[i].init == 1)
		{
			//printf("Hrac %s s client_socketem %d a id: %d\n", hraci[i].jmeno, hraci[i].client_socket, hraci[i].id);
			send(hraci[i].client_socket, &text1, strlen(text1), 0);	
		}
	}	
	pthread_mutex_unlock(&lock);
}

int sendMessage(int client_soc, char *text)
{
	pthread_mutex_lock(&lock);
	
	char text1[1000] = "";
	strcpy(text1, text);
	printf("message to %d: %s", client_soc, text1);
	send(client_soc, &text, strlen(text1), 0);
	
	pthread_mutex_unlock(&lock);
}

int broadcastToLobby(int *hraciX, int SocketHraceVynechat, char *text)
{
	pthread_mutex_lock(&lock);
	char text1[1000] = "";
	strcpy(text1, text);
	printf("broadcast: %s", text1);
	//printf("%d %d %d %d\n", hraciX[0], hraciX[1], hraciX[2], hraciX[3]);
	//Poslani samotnych zprav
	for(int i = 0; i < 4; i++)
	{
		if(hraciX[i] != -1 && hraci[hraciX[i]].client_socket != SocketHraceVynechat)
		{
			//printf("%d \n", hraciX[i]);
			send(hraci[hraciX[i]].client_socket, &text1, strlen(text1), 0);
		}
	}
	pthread_mutex_unlock(&lock);		
}

int broadcastToAllLobby(int *hraciX, char *text)
{
	pthread_mutex_lock(&lock);
	char text1[1000] = "";
	strcpy(text1, text);
	printf("broadcast all: %s", text1);

	//Poslani samotnych zprav
	for(int i = 0; i < 4; i++)
	{
		if(hraciX[i] != -1)
		{
			printf("%d a sc: %d\n", hraciX[i], hraci[hraciX[i]].client_socket);
			send(hraci[hraciX[i]].client_socket, &text1, strlen(text1), 0);
		}
	}
	pthread_mutex_unlock(&lock);
}

/**
	Seradit indexy
	int tmp;
	int nenasel = 1;
	int maxIndex = 3;//Length - 1;
	while(nenasel == 1)
	{
		int max = hraciX[maxIndex];
		int indexM = maxIndex;
		for(int i = 0; i < maxIndex; i++)
		{
			if(hraciX[i] > max)
			{
				max = hraciX[i];
				indexM = i;
			}
		}
		tmp = hraciX[maxIndex];
		hraciX[maxIndex] = max;
		hraciX[indexM] = tmp;
		if(maxIndex == 1)
		{
			nenasel = 0;
		}
		else
		{
			maxIndex--;		
		}
	}
*/




