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
#include "hrac_methods.c"
#include "lobby.h"
#include "lobby_methods.c"

//WHITELIST serveru login/heslo
FILE *passwd;
//Pole vsech uzivatelu, kteri maji pristup na server
char** whitelist;
//Pocet polozek v whitelistu
int listNum;

//!!!!!!DO BUDOUCNA MOZNA ZVETSIT NA 100 ZNAKU!!!!!!
struct Zprava
{
	char msg[50];
	int length;
	int zaznamInd;
	int error;//bool
};

void uvolniWhitelist()
{
	for(int i = 0; i < listNum; i++)
	{
  		free(whitelist[i]);
	}
	free(whitelist);
}

struct Zprava getMessage(int client_socket)//Monza pridelat stav erroru ($asda#asdasd#), kde pochyti jen prvni cast textu
{
	struct Zprava p;
	int return_value = 0;
	int zac = 0;//bool
	int kon = 0;//bool
	char pole[50];
	int indexP = 0;
	int delka = 0;
	memset(&pole, 0, sizeof(pole));
	do
	{
		char z;
		return_value=recv(client_socket, &z, 1, 0);
		if(zac == 1)
		{
		    delka++;
		    pole[indexP++] = z;  
		}
		            
		if(z == '$')
		{
		    zac = 1;
		}
		else if(z == '#')
		{
		    if(zac == 0)
		    {
		    	printf("Spatny format zpravy (# drive nez $)!\n");
		    	p.error = 1;
		    	return p;
			}
			kon = 1;
			break;
	    }
	}while(return_value > 0);
	if(kon == 0 && zac == 0)
	{
		printf("Zprava: |%s|\n", pole);
		printf("Spatny format zpravy (zprava neni mezi $ a #)!\n");
		p.error = 2;
		return p;
	}
	else if(kon == 0)
	{
		printf("Zprava: |%s|\n", pole);
		printf("Spatny format zpravy (Neni ukoncovaci znak #)!\n");
		p.error = 3;
		return p;
	}
	if(delka == 0)
	{
		printf("Zprava: |%s|\n", pole);
		printf("Spatny format zpravy (Zadna zprava)!\n");
		p.error = 4;
		return p;
	}
	pole[indexP - 1] = '\0';
	memcpy(p.msg, pole, sizeof(pole));
	p.length = delka;
	p.error = 0;
	return p;
}

int nactiFileSHesly()
{
	char buf[100];
	memset(&buf, 0, sizeof(buf));
	passwd = fopen("passwd.txt", "r");
	
	if(passwd == NULL)
    {
        perror("Error\n");   
        exit(1);             
    }
    
    int z = 0;
    int ind = 0;
	while(fgets(buf, 100, passwd)!=NULL)
    {	
		for(int i = 0; i < 100; i++)
		{
			if((int)buf[i] == 13)
			{
				buf[i] = '\0';
				break;
			}
			else if(buf[i] == '\0')
			{
				break;
			}
		}
		if(z == 1)
		{
			listNum = atoi(buf);
			whitelist = malloc(listNum * sizeof(char*));
			for(int i = 0; i < listNum; i++)
			{
				whitelist[i] = malloc(51 * sizeof(char));
			} 
		}
		else if(z >= 2)
		{
			strcpy(whitelist[ind++], buf);
			//printf("nacteniF: |%s|\n", whitelist[ind - 1]);	
		}
		memset(&buf, '\0', sizeof(buf));
		z++;	
	}
	fclose(passwd);
	printf("WHITELIST NACTEN\n");
	return 0;
}

/**
 * Funkce, ktera na zaklade prijmute zpravy dekoduje,
 * co s ni ma provest a nasledne danou akci provede
 */
struct Zprava rozdeleniZprav(struct Zprava z)
{
	struct Zprava k;
	k.zaznamInd = -1;
	char front[10];
	char back[51];
	memset(&front, 0, sizeof(front));
	memset(&back, 0, sizeof(back));
	int length;
	int naselZnacku = 0;//bool
	for(int i = 0; i < z.length; i++)
	{
		if(z.msg[i] == '!')
		{
			naselZnacku = 1;
			memcpy(back, &z.msg[i + 1], z.length - (i + 1));
			break;
		}
		else
		{
			front[i] = z.msg[i];
		}
	}
	if(naselZnacku == 0)
	{
		strcpy(k.msg, "V zadanem textu neni symbol '!'.\n");
		k.length = strlen(k.msg);
		k.error = 2;
		return k;
	}
	//printf("%s\n", front);
	//printf("%s\n", back);
	
	int naselZaznam = 0;
	int zazInd = -1;
	if(strcmp(front, "login") == 0)
	{
		for(int i = 0; i < listNum; i++)
		{
			if(strcmp(back, whitelist[i]) == 0)
			{
				char naz[50];
				memset(&naz, 0, sizeof(naz));
				for(int j =0;j < strlen(back);j++)
				{
					if(back[j] == '!')
					{
						naz[j] = '\0';
						break;
					}
					else
					{
						naz[j] = back[j];
					}
				}
				int prihlasen = 0;
				for(int j =0;j < length_hraci;j++)
				{
					if(hraci[j].init == 1)
					{
						if(strcmp(hraci[j].jmeno, naz) == 0)
						{
							prihlasen = 1;
							break;
						}
					}
				}
				if(prihlasen == 0)
				{
					zazInd = i;
					naselZaznam = 1;	
				}
				else
				{
					strcpy(k.msg, "login!decline!3!\n");
					k.length = strlen(k.msg);
					k.error = 4;
					return k;
				}
				break;
			}
		}
		if(naselZaznam == 0)
		{
			strcpy(k.msg, "login!decline!1!\n");
			k.length = strlen(k.msg);
			k.error = 3;
			return k;
		}
	}
	else
	{
		strcpy(k.msg, "login!decline!2!\n");
		k.length = strlen(k.msg);
		k.error = 1;
		return k;
	}
	
	strcpy(k.msg, "login!accept!0!\n");
	k.zaznamInd = zazInd;
	k.length = strlen(k.msg);
	k.error = 0;
	return k;
}

struct Zprava rozdeleniZpravyLobby(struct Zprava z)
{
	struct Zprava k;
	k.zaznamInd = -1;
	char front[10];
	char back[51];
	memset(&front, 0, sizeof(front));
	memset(&back, 0, sizeof(back));
	int length;
	int naselZnacku = 0;//bool
	for(int i = 0; i < z.length; i++)
	{
		if(z.msg[i] == '!')
		{
			naselZnacku = 1;
			memcpy(back, &z.msg[i + 1], z.length - (i + 1));
			break;
		}
		else
		{
			front[i] = z.msg[i];
		}
	}
	if(naselZnacku == 0)
	{
		strcpy(k.msg, "V zadanem textu neni symbol '!'.\n");
		k.length = strlen(k.msg);
		k.error = 2;
		return k;
	}
	printf("%s\n", front);
	printf("%s\n", back);
	if(strcmp(front, "refresh") == 0)
	{
		//Vytvorit string s naplni vsech dat z lobbies
	}
	else if(strcmp(front, "create") == 0)
	{
		//Vytvorit novou lobby s prevzatym nazvem
	}
	else if(strcmp(front, "join") == 0)
	{
		//Pridat index tohoto hrace do lobby s indexem lobby
	}
	else if(strcmp(front, "leave") == 0)
	{
		//Opusteni hrace z konkretni lobby
	}
	else if(strcmp(front, "discon") == 0)
	{
		//Odhlaseni hrace ze lobby a okamzity navrat do prihlasovaci obrazovky
	}
	else
	{
		printf("Nic z toho nesedi na %s\n", front)
	}	
}

// telo vlakna co zprostredkuje nejdrive prijem
//loginu a hesla a nasledne posle klientovi jestli
//byl uspesne prihlasen, nasledne zprostredkuje chat
//mezi vsemi uzivateli.
void *serve_request(void *arg)
{
	int client_socket;
	char cbuf='A';

	//pretypujem parametr z netypoveho ukazate na ukazatel na int
	client_socket = *(int *) arg;
		
	printf("Hura nove spojeni\n");
	struct Zprava z;
	char jmeno[50];
	do
	{
		//RECIEVE
		z = getMessage(client_socket);
		if(z.error == 0)
		{
			printf("Prijato ve formatu: %s\n", z.msg);	
		}
		
		//SEND
		memset(&jmeno, 0, sizeof(jmeno));	
		z = rozdeleniZprav(z);
		
		if(z.zaznamInd > -1)
		{
			for(int i = 0; i < strlen(whitelist[z.zaznamInd]); i++)
			{
				if(z.msg[i] != '!')
				{
					jmeno[i] = whitelist[z.zaznamInd][i];
				}
				else
				{
					jmeno[i] = '\0';
					break;
				}
			}	
		}	
		send(client_socket, &z.msg, strlen(z.msg), 0);
	}while(z.error >= 3);
		
	if(z.error == 0)//nedoslo k chybe
	{
		//JSEM UZ V MENU S LOBBY
		addHrac(client_socket, jmeno);
		char textK[100];
		memset(&textK, 0, sizeof(textK));
		//sprintf(textK, "[%s]: se pripojil na server.\n", jmeno);
		printf("[%s]: se pripojil na server.\n", jmeno);
		while(1)
		{
			z = getMessage(client_socket);
			if(z.error == 0)
			{
				printf("Prijato: %s\n", z.msg);
					
			}
			else
			{
				if(z.error == 2)
				{
					//memset(&textK, 0, sizeof(textK));
					//sprintf(textK, "$[%s]: se odpojil od chatu.\n", jmeno);
					printf("[%s]: se odpojil od serveru.\n", jmeno);
				}
				else
				{
					printf("Error jiny: %d\n", z.error);
				}
			}
		}
		removeHracSoc(client_socket);
	}
	close(client_socket);
	
	free(arg);
	printf("UZIVATEL %s SE SOKETEM %d BYL ODPOJEN!\n", jmeno,client_socket);
	return 0;
}


int main (void)
{
	//NACTENI WHITELISTU
	nactiFileSHesly(passwd);
	//NACTENI POLE HRACU
	initHraci();
	
	int server_socket=0;
	int client_socket=0;
	int return_value=0;
	
	int *th_socket;
	pthread_t thread_id;
	
	char cbuf[50];
	int len_addr;
	struct sockaddr_in my_addr, peer_addr;
	
	server_socket = socket(AF_INET, SOCK_STREAM, 0);
	
	memset(&my_addr, 0, sizeof(struct sockaddr_in));
	
	my_addr.sin_family = AF_INET;
	my_addr.sin_port = htons(8192);
	my_addr.sin_addr.s_addr = INADDR_ANY;
	
	return_value = bind(server_socket, (struct sockaddr *) &my_addr, sizeof(struct sockaddr_in));
	
	if(return_value == 0) 
		printf("Bind - OK\n");
	else
	{
		printf("Bind - ERR\n");
		return -1;
	}
	
	return_value = listen(server_socket, 5);
	if(return_value == 0)
	{
		printf("Listen OK\n");
	}
	else
	{
		printf("Listen ERROR\n");
		return -1;
	}

	while(1)
	{
		client_socket = accept(server_socket, (struct sockaddr *) &peer_addr, &len_addr);
		if(client_socket>0)
		{
			th_socket = malloc(sizeof(int));
			*th_socket = client_socket;
			pthread_create(&thread_id, NULL,(void *)&serve_request, (void *)th_socket);
		}
		else
		{
			uvolniWhitelist();
			printf ("Brutal Fatal ERROR\n");
			return -1;
		}
	}
	
	uvolniWhitelist();
	printf("HLAVNI PROCES SKONCIL\n");
	return 0;
}

