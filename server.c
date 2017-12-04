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

struct Zprava
{
	char msg[1000];
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
	char pole[1000];
	int indexP = 0;
	int delka = 0;
	memset(&pole, '0', sizeof(pole));
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
					//Pokus o prihalseni jiz prihlaseneho hrace
					strcpy(k.msg, "login!decline!3!\n");
					k.length = strlen(k.msg);
					k.error = 3;
					return k;
				}
				break;
			}
		}
		if(naselZaznam == 0)
		{
			//Hrac nebyl nalezen ve whitelistu
			strcpy(k.msg, "login!decline!1!\n");
			k.length = strlen(k.msg);
			k.error = 1;
			return k;
		}
	}
	else
	{
		//Spatny parametr login, neco jineho, treba llogin
		strcpy(k.msg, "login!decline!2!\n");
		k.length = strlen(k.msg);
		k.error = 2;
		return k;
	}
	
	//Vse probehlo spravne
	strcpy(k.msg, "login!accept!0!\n");
	k.zaznamInd = zazInd;
	k.length = strlen(k.msg);
	k.error = 0;
	return k;
}

struct Zprava rozdeleniZpravyLobby(struct Zprava z, int cl)
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
	if(strcmp(front, "refresh") == 0)
	{
		//Vytvorit string s naplni vsech dat z lobbies
		char textK[1000];
		memset(&textK, '\0', sizeof(textK));
		sprintf(textK, "$refresh!%d!", length_lobbies);
		//Nazvy lobbyn
		for(int i = 0;i < length_lobbies;i++)
		{
			strcat(textK, lobbies[i].lobbyName);
			if(i + 1 < length_lobbies)
			{
				strcat(textK, ",");		
			}
			else
			{
				strcat(textK, "!");	
			}
		}
		//Pocet lidi v lobbynach
		for(int i = 0;i < length_lobbies;i++)
		{
			char z[1];
			sprintf(z, "%d", lobbies[i].pocetHracu);
			strcat(textK, z);
			if(i + 1 < length_lobbies)
			{
				strcat(textK, ",");		
			}
			else
			{
				strcat(textK, "!#\n");	
			}
		}
		strcpy(k.msg, textK);
		k.length = strlen(k.msg);
		k.error = 0;
		printf("%s", k.msg);
		return k;
	}
	else if(strcmp(front, "create") == 0)
	{
		//Vytvorit novou lobby s prevzatym nazvem
		int v = addLobby(back);
		if(v > 0)
		{
			sprintf(k.msg, "$create!decline!%d!#\n", v);
			k.error = v;
		}
		else
		{
			sprintf(k.msg, "$create!accpet!#\n");	
			k.error = 0;
		}
		k.length = strlen(k.msg);
		printf("%s", k.msg);
		return k; 
	}
	else if(strcmp(front, "join") == 0)
	{
		//Pridat index tohoto hrace do lobby s indexem lobby
		printf("%s index pro join hrace do lobby!\n", back);
		int inL = atoi(back);
		int inH = -1;
		for(int i = 0;i < length_hraci;i++)
		{
			if(hraci[i].init == 1 && hraci[i].client_socket == cl)
			{
				inH = i;
				break;
			}
		}
		if(inH == -1)
		{
			printf("Chyba, hrac nebyl nalezen v seznamu hracu!\n");
			sprintf(k.msg, "$join!decline!4!#\n");
			k.length = strlen(k.msg);
			k.error = 4;
			printf("%s", k.msg);
			return k;
		}
		int returnValue1 = addPlayer(inH, inL);
		if(returnValue1 >= 1)
		{
			sprintf(k.msg, "$join!decline!%d!#\n", returnValue1);
			k.error = returnValue1;
		}
		else
		{
			//Poslat hracum v jiz pripojene lobby
			char textL[100];
			memset(&textL, 0, sizeof(textL));
			printf("client socket: %d\n", cl);
			printf("BC[%s]\n", hraci[getHracIndex(cl)].jmeno);
			sprintf(textL, "$lobby!add!%s!#\n", hraci[getHracIndex(cl)].jmeno);	
			broadcastToLobby(lobbies[inL].hraciLobby, cl, textL);
			
			sprintf(k.msg, "$join!accept!");
			char x[2];
			sprintf(x, "%d!", lobbies[inL].pocetHracu);
			strcat(k.msg, x);
			int aktPocet = 0;
			for(int i = 0;i < 4;i++)
			{
				int y = lobbies[inL].hraciLobby[i];
				if(y >= 0)
				{
					strcat(k.msg, hraci[y].jmeno);
					aktPocet++;
					if(aktPocet == lobbies[inL].pocetHracu)
					{
						strcat(k.msg, "!#\n");
						break;		
					}
					else
					{
						strcat(k.msg, ",");	
					}	
				}
			}
			k.error = 0;
		}
		k.length = strlen(k.msg);
		printf("%s", k.msg);
		return k;
	}
	else if(strcmp(front, "leave") == 0)
	{
		//Opusteni hrace z konkretni lobby
		int indH = -1;
		for(int i = 0;i < length_hraci;i++)
		{
			if(hraci[i].init == 1 && hraci[i].client_socket == cl)
			{
				indH = i;
				break;
			}
		}
		if(indH == -1)
		{
			printf("Chyba, hrac nebyl nalezen v seznamu hracu!\n");
			sprintf(k.msg, "$leave!error!8!#\n");
			k.length = strlen(k.msg);
			k.error = 8;
			printf("%s", k.msg);
			return k;
		}
		int indL = -1;
		for(int i = 0; i < length_lobbies; i++)
		{
			for(int j = 0; j < 4; j++)
			{
				if(lobbies[i].hraciLobby[j] == indH)
				{
					indL = i;
					break;
				}
			}
		}
		if(indL == -1)
		{
			printf("Chyba, hrac neni v zadne lobby!\n");
			sprintf(k.msg, "$leave!error!7!#\n");
			k.length = strlen(k.msg);
			k.error = 7;
			printf("%s", k.msg);
			return k;
		}
		
		int returnValue = removePlayer(indH, indL);
		if(returnValue >= 1)
		{
			sprintf(k.msg, "$leave!decline!6!#\n");
			k.error = 6;
		}
		else
		{
			//Poslat hracum v jiz pripojene lobby
			char textL[100];
			memset(&textL, 0, sizeof(textL));
			sprintf(textL, "$lobby!rem!%s!#\n", hraci[getHracIndex(cl)].jmeno);	
			broadcastToLobby(lobbies[indL].hraciLobby, cl, textL);
			
			sprintf(k.msg, "$leave!accepted!#\n");
			k.error = 5;	
		}
		k.length = strlen(k.msg);
		printf("%s", k.msg);
		return k;		
	}
	else if(strcmp(front, "discon") == 0)
	{
		//Odhlaseni hrace z lobby a okamzity navrat do prihlasovaci obrazovky
		int indH = -1;
		for(int i = 0;i < length_hraci;i++)
		{
			if(hraci[i].init == 1 && hraci[i].client_socket == cl)
			{
				indH = i;
				break;
			}
		}
		if(indH == -1)
		{
			printf("Chyba, hrac nebyl nalezen v seznamu hracu!\n");
			sprintf(k.msg, "$discon!error!7!#\n");
			k.length = strlen(k.msg);
			k.error = 7;
			printf("%s", k.msg);
			return k;
		}
		int indL = -1;
		for(int i = 0; i < length_lobbies; i++)
		{
			for(int j = 0; j < 4; j++)
			{
				if(lobbies[i].hraciLobby[j] == indH)
				{
					indL = i;
					break;
				}
			}
		}
		if(indL != -1)
		{
			int returnValue = removePlayer(indH, indL);
			if(returnValue >= 1)
			{
				sprintf(k.msg, "$discon!decline!5!#\n");
				k.error = 5;
			}
			else
			{
				sprintf(k.msg, "$discon!accepted!#\n");
				k.error = 100;
				//SPECIALNI ERROR, bude predstavovat ukonceni celkove
				//apklikace jako takove, jednoduse se ukonci spojeni 	
			}
		}
		else
		{
			sprintf(k.msg, "$discon!accepted!#\n");
			k.error = 100;
			//SPECIALNI ERROR, bude predstavovat ukonceni celkove
			//apklikace jako takove, jednoduse se ukonci spojeni
		}
		k.length = strlen(k.msg);
		printf("%s", k.msg);
		return k;
	}
	else
	{
		printf("Nic ze znamych parametru nesedi na |%s|\n", front);
		k.error = 10;
		k.length = strlen(k.msg);
		return k;
	}	
}

//Telo vlakna co zprostredkuje nejdrive prijem
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
			if(whitelist[z.zaznamInd][i] != '!')
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
		
	if(z.error == 0)//nedoslo k chybe v login fazi
	{
		//JSEM UZ V MENU S LOBBY
		addHrac(client_socket, jmeno);
		printf("[%s]: se pripojil na server.\n", jmeno);
		while(1)
		{
			z = getMessage(client_socket);
			if(z.error == 0)
			{
				printf("\nPrijato: %s\n", z.msg);
				if(hraci[getHracIndex(client_socket)].stav == 1)
				{
					z = rozdeleniZpravyLobby(z, client_socket);
					if(z.error < 5)
					{
						send(client_socket, &z.msg, strlen(z.msg), 0);
					}
					else if(z.error == 100)
					{
						break;
					}
				}				
			}
			else
			{
				if(z.error <= 4)
				{
					printf("[%s]: se odpojil od serveru.\n", jmeno);
					break;
				}
				else if(z.error == 100)
				{
					printf("[%s]: se odpojil od serveru.\n", jmeno);
					break;
				}
				else
				{
					printf("Error jiny: %d\n", z.error);
					printf("|%s|\n", z.msg);
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
	//NACTENI LOBBYN
	initLobby();
	
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
	uvolniLobby();
	uvolniHrace();
	printf("HLAVNI PROCES SKONCIL\n");
	return 0;
}

