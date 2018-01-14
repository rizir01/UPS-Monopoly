#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include "zprava.h"
#include "hrac.h"
#include "lobby.h"
#include "game.h"
#include "game_methods.c"

//WHITELIST serveru login/heslo
FILE *passwd;
//Pole vsech uzivatelu, kteri maji pristup na server
char** whitelist;
//Pocet polozek ve whitelistu
int listNum;

void uvolniWhitelist()
{
	for(int i = 0; i < listNum; i++)
	{
  		free(whitelist[i]);
	}
	free(whitelist);
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

int saveWhiteList()
{
	char buf[100];
	memset(&buf, 0, sizeof(buf));
	passwd = fopen("passwd.txt", "w");
	
	if(passwd == NULL)
    {
        perror("Error\n");   
        exit(1);             
    }
    
	fprintf(passwd, "//WHITELIST lidi na server, [login#heslo]\n");
	fprintf(passwd, "%d\n" , listNum);
	
	for(int i = 0; i < listNum; i++)
	{
		fprintf(passwd, "%s\n", whitelist[i]);
	}
	
	fclose(passwd);
	printf("WHITELIST ULOZEN\n");
	return 0;
}

int addMemberToWhitelist(char *record)
{
	//Vytvorit
	char** whitelist2 = malloc((listNum + 1) * sizeof(char*));
	for(int i = 0; i < listNum + 1; i++)
	{
		whitelist2[i] = malloc(51 * sizeof(char));
	}
	//Zkopirovat
	for(int i = 0; i < listNum; i++)
	{
		strcpy(whitelist2[i], whitelist[i]);	
	}
	strcpy(whitelist2[listNum], record);
	uvolniWhitelist();
	whitelist = whitelist2; 
	listNum++;
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
			if(strcmp(back, whitelist[i]) == 0)//!!Pozor, kdyz soubor s hesly ulozim v PSPadu tak to pak nefunguje
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
	else if(strcmp(front, "register") == 0)
	{
		pthread_mutex_lock(&lockSep);
			
		separeter(back, '!');
		char jm[50];
		char psw[50];
		strcpy(jm, sepa[0]);
		strcpy(psw, sepa[1]);
		
		pthread_mutex_unlock(&lockSep);
		
		for(int i = 0; i < listNum; i++)
		{
			pthread_mutex_lock(&lockSep);
			
			separeter(whitelist[i], '!');
			char vys[2][50];
			strcpy(vys[0], sepa[0]);
			strcpy(vys[1], sepa[1]);
			
			pthread_mutex_unlock(&lockSep);
			
			if(strcmp(jm, vys[0]) == 0)
			{
				//Hrac jiz s timto loginem existuje
				strcpy(k.msg, "register!decline!1!\n");
				k.length = strlen(k.msg);
				k.error = 1;
				return k;
			}
		}
		
		//Pridat tohoto hrace do whitelistu
		addMemberToWhitelist(back);
		saveWhiteList();
		zazInd = listNum - 1;
	}
	else
	{
		//Spatny parametr login, neco jineho, treba llogin
		sprintf(k.msg, "%s!decline!2!\n", front);
		k.length = strlen(k.msg);
		k.error = 2;
		return k;
	}
	
	//Vse probehlo spravne
	sprintf(k.msg, "%s!accept!0!\n", front);
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
			sprintf(k.msg, "$create!accept!#\n");	
			k.error = 0;
		}
		
		//Pridat hrace do teto lobby
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
			sprintf(k.msg, "$create!decline!3!#\n");
			k.length = strlen(k.msg);
			k.error = 3;
			printf("%s", k.msg);
			return k;
		}
		int indL = getIndexLobby(back);
		if(indL == -1)
		{
			printf("Chyba, lobby s nazvem %s neni v seznamu lobbyin!\n", back);
			sprintf(k.msg, "$create!decline!4!#\n");
			k.length = strlen(k.msg);
			k.error = 4;
			printf("%s", k.msg);
			return k;
		}
		int returnValue1 = addPlayer(inH, indL);
		if(returnValue1 >= 1)
		{
			printf("Chyba, neslo pridat do konkretni lobby hrace s chybou %d\n", returnValue1);
			sprintf(k.msg, "$create!decline!4-1!#\n");
			k.length = strlen(k.msg);
			k.error = 4;
			printf("%s", k.msg);
			return k;
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
			//Poslat hracum v jiz pripojene lobby ze se hrac opdojil
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
	else if(strcmp(front, "ready") == 0)
	{
		//pridani nebo odebrani u hrace v lobby jestli je ready nebo neni
		int ready = atoi(back);
		//printf("ready klient: %d z atoi: %d\n", cl, ready);
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
			sprintf(k.msg, "$ready!error!5!#\n");
			k.length = strlen(k.msg);
			k.error = 5;
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
			int returnValue = changeReady(indH, indL, ready);
			if(returnValue >= 1)
			{
				sprintf(k.msg, "$ready!error!%d!#\n", returnValue);
				k.length = strlen(k.msg);
				k.error = 6 + returnValue;//7 a vyse errory
			}
			else
			{
				//Poslat hracum v jiz pripojene lobby ze hrac zmenil stav na ready/unready
				char textL[100];
				memset(&textL, '\0', sizeof(textL));
				if(ready == 0)
				{
					sprintf(textL, "$ready!rem!%s!#\n", hraci[getHracIndex(cl)].jmeno);		
				}
				else
				{
					sprintf(textL, "$ready!add!%s!#\n", hraci[getHracIndex(cl)].jmeno);	
				}
				broadcastToLobby(lobbies[indL].hraciLobby, cl, textL);
			}
		}
		else
		{
			sprintf(k.msg, "$ready!error!6!#\n");
			k.length = strlen(k.msg);
			k.error = 6;
		}
		sprintf(k.msg, "$ready!accept!#\n");
		k.length = strlen(k.msg);
		printf("%s", k.msg);
		return k;
	}
	else if(strcmp(front, "loaded") == 0)
	{
		printf("Prosel spravne az sem u loaded!");
		//Nastaveni hrace na ready = 2 a poslani teto informace vsem ostatnim
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
			sprintf(k.msg, "$pre!error!5!#\n");
			k.length = strlen(k.msg);
			k.error = 5;
			printf("%s", k.msg);
			return k;
		}
		int indHracLobby = -1;
		int indL = -1;
		for(int i = 0; i < length_lobbies; i++)
		{
			for(int j = 0; j < 4; j++)
			{
				if(lobbies[i].hraciLobby[j] == indH)
				{
					indHracLobby = j;
					indL = i;
					break;
				}
			}
		}
		if(indL == -1)
		{
			printf("Chyba, hrac se nenachazi v zadne lobby!\n");
			sprintf(k.msg, "$pre!error!6!#\n");
			k.length = strlen(k.msg);
			k.error = 6;
			printf("%s", k.msg);
			return k;	
		}
		if(lobbies[indL].hraciReady[indHracLobby] == 1)
		{
			lobbies[indL].hraciReady[indHracLobby] = 2;	
			char vys[250];//!!
			
			if(isEveryoneReadyLevel2(indL))
			{
				sprintf(vys, "$pre!add!%d!#\n", indHracLobby);
				broadcastToAllLobby(lobbies[indL].hraciLobby, vys);
				
				//Vsichni uz cekaji ve hre
				//Vsem nastavit stav = 2
				for(int i = 0; i < lobbies[indL].pocetHracu; i++)
				{
					int iH = lobbies[indL].hraciLobby[i];
					hraci[iH].stav = 2;					
				}
				
				//PRIDANI NOVE HRY
				int bi = addGame(lobbies[indL].idLobby);
				printf("pocetHracu ve hre: %d\n", lobbies[indL].pocetHracu);
				int za = lobbies[indL].pocetHracu;
				for(int m = 0; m < 4; m++)
				{
					if(za > 0)
					{
						list_games[bi].penize[m] = 1500;
						za--;	
					}
					else
					{
						list_games[bi].penize[m] = 0;
					}
				}
				shuffleChanceCards(&list_games[bi]);
				shuffleChestCards(&list_games[bi]);
				char text1[250];//!!	
				generateGameFullStats(text1, 250, &lobbies[indL]);//Vygenerovat stav hry
				setGameStatusFull(text1, &list_games[bi]);//Nastavit hru
				printf("Nagenerovana hra:\n%s\n", text1);
				sprintf(vys, "$pre!start|%s|#\n", text1);
				broadcastToAllLobby(lobbies[indL].hraciLobby, vys);//Poslat vsem stav hry
					
				strcpy(k.msg, vys);
				k.length = strlen(k.msg);//Z duvodu toho, aby index nedosel pozdeji nez muze hra zacit,
				//davama zde error kod na cislo tak, aby zprava nebyla odeslana serverem danemu klientovi
				k.error = 7;
			}
			else
			{
				sprintf(vys, "$pre!add!%d!#\n", indHracLobby);
				broadcastToLobby(lobbies[indL].hraciLobby, cl, vys);
					
				strcpy(k.msg, vys);
				k.length = strlen(k.msg);
				k.error = 0;
			}
			printf("%s", k.msg);
		}
		else
		{
			printf("Chyba, hrac uz je oznacen za ready!\n");
			sprintf(k.msg, "$pre!error!7!#\n");
			k.length = strlen(k.msg);
			k.error = 7;
			printf("%s", k.msg);
		}
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

//ZPracovani logicke zpravy od klienta, porovnani spravnosti
//nasledne provedeni dane akce.
struct Zprava rozdeleniZpravyEnd(struct Zprava z, int cl)
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
	
	if(strcmp(front, "return") == 0)
	{
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
			sprintf(k.msg, "$return!decline!5!#\n");
			k.length = strlen(k.msg);
			k.error = 5;
			printf("%s", k.msg);
			return k;
		}
		int indHracLobby = -1;//Pozice hrace v lobby <0,3>
		int indL = -1;
		for(int i = 0; i < length_lobbies; i++)
		{
			for(int j = 0; j < 4; j++)
			{
				if(lobbies[i].hraciLobby[j] == indH)
				{
					indHracLobby = j;
					indL = i;
					break;
				}
			}
		}
		if(indL == -1)
		{
			printf("Chyba, hrac se nenachazi v zadne lobby!\n");
			sprintf(k.msg, "$return!decline!6!#\n");
			k.length = strlen(k.msg);
			k.error = 6;
			printf("%s", k.msg);
			return k;	
		}
		
		hraci[indH].stav = 1;
		lobbies[indL].hraciReady[indHracLobby] = 0;
		
		sprintf(k.msg, "$return!accept!#\n");
		k.length = strlen(k.msg);
		k.error = 0;
		printf("%s", k.msg);
		return k;
	}
	else if(strcmp(front, "discon") == 0)
	{
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
			sprintf(k.msg, "$discon!decline!5!#\n");
			k.length = strlen(k.msg);
			k.error = 5;
			printf("%s", k.msg);
			return k;
		}
		int indHracLobby = -1;//Pozice hrace v lobby <0,3>
		int indL = -1;
		for(int i = 0; i < length_lobbies; i++)
		{
			for(int j = 0; j < 4; j++)
			{
				if(lobbies[i].hraciLobby[j] == indH)
				{
					indHracLobby = j;
					indL = i;
					break;
				}
			}
		}
		if(indL == -1)
		{
			printf("Chyba, hrac se nenachazi v zadne lobby!\n");
			sprintf(k.msg, "$discon!decline!6!#\n");
			k.length = strlen(k.msg);
			k.error = 6;
			printf("%s", k.msg);
			return k;	
		}
		else
		{
			int returnValue = removePlayer(indH, indL);
			if(returnValue >= 1)
			{
				sprintf(k.msg, "$discon!decline!7!#\n");
				k.error = 7;
			}
			else
			{
				sprintf(k.msg, "$discon!accepted!#\n");
				k.error = 100; 	
			}	
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

//ZPracovani logicke zpravy od klienta, porovnani spravnosti
//nasledne provedeni dane akce.
struct Zprava rozdeleniZpravyHra(struct Zprava z, int cl)
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
	printf("SEM uroven -1.\n");
	if(strcmp(front, "game") == 0)
	{
		if(strcmp(back, "roll") == 0)
		{	
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
				sprintf(k.msg, "$roll!error!5!#\n");
				k.length = strlen(k.msg);
				k.error = 5;
				printf("%s", k.msg);
				return k;
			}
			int indHracLobby = -1;//Pozice hrace v lobby <0,3>
			int indL = -1;
			for(int i = 0; i < length_lobbies; i++)
			{
				for(int j = 0; j < 4; j++)
				{
					if(lobbies[i].hraciLobby[j] == indH)
					{
						indHracLobby = j;
						indL = i;
						break;
					}
				}
			}
			if(indL == -1)
			{
				printf("Chyba, hrac se nenachazi v zadne lobby!\n");
				sprintf(k.msg, "$roll!error!6!#\n");
				k.length = strlen(k.msg);
				k.error = 6;
				printf("%s", k.msg);
				return k;	
			}
			int indG = -1;
			for(int i = 0; i < length_list_games; i++)
			{
				if(lobbies[indL].idLobby == list_games[i].idLobby)
				{
					indG = i;
					break;
				}
			}
			if(indG == -1)
			{
				printf("Chyba, pro danou lobby neexistuje zadna hra!\n");
				sprintf(k.msg, "$roll!error!7!#\n");
				k.length = strlen(k.msg);
				k.error = 7;
				printf("%s", k.msg);
				return k;	
			}
			
			if(list_games[indG].natahu != indHracLobby)
			{
				printf("Chyba, dany hrac %d neni natahu, na tahu je %d!\n", indHracLobby, list_games[indG].natahu);
				sprintf(k.msg, "$roll!error!8!#\n");
				k.length = strlen(k.msg);
				k.error = 8;
				printf("%s", k.msg);
				return k;	
			}

			//i - index hrace ve strukture Hry, index do pole se vsemi hraci, a pak konkretni hra
			int ret = gameRules(indHracLobby, indH, &list_games[indG]);
			printf("\n");
			if(ret == 0)
			{
				int znova = 0;
				if(list_games[indG].changeOfPlayers)
				{
					printf("change of Players true\n");
					list_games[indG].changeOfPlayers = 1;
					list_games[indG].anotherRun = 0;
					znova = 1;
					//Poslat zpravu o tom ze hrac ma hrat znovu 
				}
				if(list_games[indG].anotherRun == 1)
				{
					printf("another Run true\n");
					znova = 1;
					list_games[indG].anotherRun = 0;
					//Poslat zpravu o tom ze hrac ma hrat znovu 
				}
				//Kontrola, jestli neni konec hry
				int zbytek = 0;
				int winInd = -1;
				for(int b = 0; b < 4;b++)
				{
					if(list_games[indG].penize[b] > 0)
					{
						zbytek++;
						winInd = b;
					}
				}
				if(zbytek <= 1)//Posledni hrac
				{
					//Nastavit vsem hracum v lobby, ze jsou ve stavu 3
					for(int v = 0; v < 4; v++)
					{
						if(lobbies[indL].hraciLobby[v] != -1)
						{
							hraci[lobbies[indL].hraciLobby[v]].stav = 3;
						}
					}
					
					sprintf(k.msg, "$game!win!%s!#\n", list_games[indG].jmena[winInd]);
					broadcastToGame(&list_games[indG], cl, k.msg);
					
					//Vymazat hru ze seznamu her
					removeGame(indG);
					k.error = 0;
					k.length = strlen(k.msg);
					printf("%s", k.msg);
					return k;
				}
				else
				{
					if(znova == 1)
					{
						sprintf(k.msg, "$game!again!#\n");
					}
					else
					{
						sprintf(k.msg, "$game!end!#\n");		
					}
					k.error = 0;	
				}
			}
			else if(ret == 1)
			{
				sprintf(k.msg, "$game!aukce!start!%d!#\n", list_games[indG].poziceHracu[indHracLobby]);
				k.error = 9;//Poslat zpravu jen tem, kteri nespustily aukci
			}
			broadcastToLobby(lobbies[indL].hraciLobby, cl, k.msg);
			k.length = strlen(k.msg);
			printf("%s", k.msg);
			return k;
		}
		else
		{
			printf("Nic ze znamych parametru nesedi na |%s|\n", back);
			k.error = 10;
			k.length = strlen(k.msg);
			return k;
		}		
	}
	else if(strcmp(front, "aukce") == 0)
	{
		char *e;
		int indexI;
		e = strchr(back, '!');
		indexI = (int)(e - back);
		char forbuff[10];
		memcpy(forbuff, &back[0], indexI);
		forbuff[indexI] = '\0';
		if(strcmp(forbuff, "add") == 0)
		{
			char subbuff[10];
			memcpy(subbuff, &back[indexI + 1], (int)strlen(back) - (indexI + 1));
			subbuff[(int)strlen(back) - (indexI + 1)] = '\0';
			//Zkontrolovat, jestli vubec je nejaka aukce
			//Zkontrolovat, jestli je narade
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
				sprintf(k.msg, "$aukce!add!error!5!#\n");
				k.length = strlen(k.msg);
				k.error = 5;
				printf("%s", k.msg);
				return k;
			}
			int indHracLobby = -1;//Pozice hrace v lobby <0,3>
			int indL = -1;
			for(int i = 0; i < length_lobbies; i++)
			{
				for(int j = 0; j < 4; j++)
				{
					if(lobbies[i].hraciLobby[j] == indH)
					{
						indHracLobby = j;
						indL = i;
						break;
					}
				}
			}
			if(indL == -1)
			{
				printf("Chyba, hrac se nenachazi v zadne lobby!\n");
				sprintf(k.msg, "$aukce!add!error!6!#\n");
				k.length = strlen(k.msg);
				k.error = 6;
				printf("%s", k.msg);
				return k;	
			}
			int indG = -1;
			for(int i = 0; i < length_list_games; i++)
			{
				if(lobbies[indL].idLobby == list_games[i].idLobby)
				{
					indG = i;
					break;
				}
			}
			if(indG == -1)
			{
				printf("Chyba, pro danou lobby neexistuje zadna hra!\n");
				sprintf(k.msg, "$aukce!add!error!7!#\n");
				k.length = strlen(k.msg);
				k.error = 7;
				printf("%s", k.msg);
				return k;	
			}
			if(list_games[indG].aukce.auction == 1 && list_games[indG].aukce.aukceNatahu == indHracLobby)
			{
				int cislo = atoi(subbuff);
				if(cislo == 0)
				{
					printf("Chyba, hrac neposlal validni cislo %s!\n", subbuff);
					sprintf(k.msg, "$aukce!add!error!8!#\n");
					k.length = strlen(k.msg);
					k.error = 8;
					printf("%s", k.msg);
					return k;		
				}
				makeActionPRUAuction(indHracLobby, indH, 1, cislo, &list_games[indG]);
				k.error = 9;//!! Nechci nic posilat
				return k;
			}
			else
			{
				printf("Chyba, aukce neni spustena nebo k ni nema pristup!\n");
				printf("aukce: %d, natahu: %d hrac ma index ve hre: %d\n", list_games[indG].aukce.auction, list_games[indG].aukce.aukceNatahu, indHracLobby);
				sprintf(k.msg, "$aukce!add!error!9!#\n");
				k.length = strlen(k.msg);
				k.error = 9;
				printf("%s", k.msg);
				return k;
			}
		}
		else if(strcmp(forbuff, "end") == 0)
		{
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
				sprintf(k.msg, "$aukce!add!error!5!#\n");
				k.length = strlen(k.msg);
				k.error = 5;
				printf("%s", k.msg);
				return k;
			}
			int indHracLobby = -1;//Pozice hrace v lobby <0,3>
			int indL = -1;
			for(int i = 0; i < length_lobbies; i++)
			{
				for(int j = 0; j < 4; j++)
				{
					if(lobbies[i].hraciLobby[j] == indH)
					{
						indHracLobby = j;
						indL = i;
						break;
					}
				}
			}
			if(indL == -1)
			{
				printf("Chyba, hrac se nenachazi v zadne lobby!\n");
				sprintf(k.msg, "$aukce!add!error!6!#\n");
				k.length = strlen(k.msg);
				k.error = 6;
				printf("%s", k.msg);
				return k;	
			}
			int indG = -1;
			for(int i = 0; i < length_list_games; i++)
			{
				if(lobbies[indL].idLobby == list_games[i].idLobby)
				{
					indG = i;
					break;
				}
			}
			if(indG == -1)
			{
				printf("Chyba, pro danou lobby neexistuje zadna hra!\n");
				sprintf(k.msg, "$aukce!add!error!7!#\n");
				k.length = strlen(k.msg);
				k.error = 7;
				printf("%s", k.msg);
				return k;	
			}
			if(list_games[indG].aukce.auction == 1 && list_games[indG].aukce.aukceNatahu == indHracLobby)
			{
				makeActionPRUAuction(indHracLobby, indH, 0, -1, &list_games[indG]);
				if(list_games[indG].aukce.auction == 2)
				{
					gameRulesPost(list_games[indG].natahu, lobbies[indL].hraciLobby[list_games[indG].natahu], &list_games[indG]);
					int znova = 0;
					if(list_games[indG].changeOfPlayers)
					{
						printf("change of Players true\n");
						list_games[indG].changeOfPlayers = 1;
						list_games[indG].anotherRun = 0;
						znova = 1;
						//Poslat zpravu o tom ze hrac ma hrat znovu 
					}
					if(list_games[indG].anotherRun == 1)
					{
						printf("another Run true\n");
						znova = 1;
						list_games[indG].anotherRun = 0;
						//Poslat zpravu o tom ze hrac ma hrat znovu 
					}
					if(znova == 1)
					{
						sprintf(k.msg, "$game!again!#\n");
					}
					else
					{
						sprintf(k.msg, "$game!end!#\n");		
					}
					k.error = 0;
					//!!
					broadcastToLobby(lobbies[indL].hraciLobby, cl, k.msg);
					k.length = strlen(k.msg);
					printf("%s", k.msg);
					return k;
				}	
			}
			else
			{
				printf("Chyba, aukce neni spustena nebo k ni nema pristup!\n");
				printf("aukce: %d, natahu: %d hrac ma index ve hre: %d\n",list_games[indG].aukce.auction, list_games[indG].aukce.aukceNatahu, indHracLobby);
				sprintf(k.msg, "$aukce!add!error!9!#\n");
				k.length = strlen(k.msg);
				k.error = 9;
				printf("%s", k.msg);
				return k;
			}
		}
		else
		{
			printf("Nic ze znamych parametru nesedi na |%s|\n", forbuff);
			k.error = 10;
			k.length = strlen(k.msg);
			return k;	
		}	
	}
	else if(strcmp(front, "leave") == 0 || strcmp(front, "discon") == 0)
	{
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
			sprintf(k.msg, "$%s!decline!5!#\n", front);
			k.length = strlen(k.msg);
			k.error = 5;
			printf("%s", k.msg);
			return k;
		}
		int indHracLobby = -1;//Pozice hrace v lobby <0,3>
		int indL = -1;
		for(int i = 0; i < length_lobbies; i++)
		{
			for(int j = 0; j < 4; j++)
			{
				if(lobbies[i].hraciLobby[j] == indH)
				{
					indHracLobby = j;
					indL = i;
					break;
				}
			}
		}
		if(indL == -1)
		{
			printf("Chyba, hrac se nenachazi v zadne lobby!\n");
			sprintf(k.msg, "$%s!decline!6!#\n", front);
			k.length = strlen(k.msg);
			k.error = 6;
			printf("%s", k.msg);
			return k;	
		}
		int indG = -1;
		for(int i = 0; i < length_list_games; i++)
		{
			if(lobbies[indL].idLobby == list_games[i].idLobby)
			{
				indG = i;
				break;
			}
		}
		if(indG == -1)
		{
			printf("Chyba, pro danou lobby neexistuje zadna hra!\n");
			sprintf(k.msg, "$%s!decline!7!#\n", front);
			k.length = strlen(k.msg);
			k.error = 7;
			printf("%s", k.msg);
			return k;	
		}
		//Hrac odstranen ze hry
		list_games[indG].anotherRun = 0;
		memset(&list_games[indG].budovy[indHracLobby], '\0', sizeof(list_games[indG].budovy[indHracLobby]));
		list_games[indG].changeOfPlayers = 0;//!!Nejsem si jist
		list_games[indG].penize[indHracLobby] = -1;
		list_games[indG].poziceHracu[indHracLobby] = -1;
		memset(&list_games[indG].upgrady[indHracLobby], '\0', sizeof(list_games[indG].upgrady[indHracLobby]));
		list_games[indG].vezeni[indHracLobby] = 0;
		
		//!!Mozna chyba s cenou, pokud by hrac co opousti nastavil moc velkou cenu,
		//Tak zbyvajici hrac by na to prohral - musim vyzkouset
		if(list_games[indG].aukce.auction == 1)
		{
			printf("LEAVE/DISCON GAME - hrac co je natahu v aukci odchazi!\n");
			makeActionPRUAuctionAfterLeave(indHracLobby, indH, &list_games[indG]);	
		}
		
		if(list_games[indG].natahu == indHracLobby)
		{
			printf("LEAVE/DISCON GAME - hrac co je natahu odchazi!\n");
			list_games[indG].vezeniLuck[indHracLobby] = 0;
			//Nastavit noveho hrace
			list_games[indG].natahu++;
			if(list_games[indG].natahu == 4)
			{
				list_games[indG].natahu = 0;
			}
			int nasel = 1;
			while(nasel)
			{
				if(list_games[indG].penize[list_games[indG].natahu] <= 0)
				{
					list_games[indG].natahu++;
					if(list_games[indG].natahu == 4)
					{
						list_games[indG].natahu = 0;
					}
				}
				else
				{
					nasel = 0;
				}
			}
			
			
			if(list_games[indG].jailFree == indHracLobby)
			{
				list_games[indG].jailFree = -1;
			}
			list_games[indG].hodStejnych = 0;	
		}
		//Zjistit, jestli nahodou uz neni posledni hrac ve hre,
		//jinak poslat zpravu ze vyhral
		int zbytek = 0;
		int winInd = -1;
		for(int i = 0; i < 4;i++)
		{
			if(list_games[indG].penize[i] > 0)
			{
				zbytek++;
				winInd = i;
			}
		}
		
		//Odstraneni hrace z lobby
		int zp = removePlayer(indH, indL);
		//Nastavit hraci ze je ve stavu 1=menu
		hraci[indH].stav = 1;
		sprintf(k.msg, "$left!%s!#\n", hraci[indH].jmeno);
		broadcastToGame(&list_games[indG], cl, k.msg);
		if(zbytek <= 1)//Posledni hrac
		{
			//Nastavit vsem hracum v lobby, ze jsou ve stavu 3
			for(int i = 0; i < 4; i++)
			{
				if(lobbies[indL].hraciLobby[i] != -1)
				{
					hraci[lobbies[indL].hraciLobby[i]].stav = 3;
				}
			}
			
			sprintf(k.msg, "$game!win!%s!#\n", list_games[indG].jmena[winInd]);
			broadcastToGame(&list_games[indG], cl, k.msg);
			
			//Vymazat hru ze seznamu her
			removeGame(indG);
		}
			
		if(strcmp(front, "discon") == 0)	
		{
			sprintf(k.msg, "$discon!accept!#\n");
			k.length = strlen(k.msg);
			k.error = 100;
			printf("%s", k.msg);
			return k;
		}
		else
		{
			sprintf(k.msg, "$leave!accept!#\n");
			k.length = strlen(k.msg);
			k.error = 0;
			printf("%s", k.msg);
			return k;	
		}
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
	
	if(z.zaznamInd > -1)//Ziskani jmena hrace co se pripojil
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
	
	//Spusten odpocet
	int count = 0;
		
	if(z.error == 0)//nedoslo k chybe v login fazi
	{
		//JSEM UZ V MENU S LOBBY
		addHrac(client_socket, jmeno);
		printf("[%s]: se pripojil na server.\n", jmeno);
		while(1)
		{
			z = getMessage(client_socket);
			printf("Zprava zpracovana - ");
			printf("erorr: %d\n", z.error);
			if(z.error == 0)
			{
				printf("\nPrijato: %s\n", z.msg);
				printf("Hrac cl: %d ma stav: %d\n", client_socket, hraci[getHracIndex(client_socket)].stav);
				if(hraci[getHracIndex(client_socket)].stav == 1)
				{
					z = rozdeleniZpravyLobby(z, client_socket);
					if(z.error < 5)
					{
						send(client_socket, &z.msg, strlen(z.msg), 0);
					}
					else if(z.error == 100)
					{
						printf("[%s]: se odpojil od serveru.\n", jmeno);
						break;
					}
					int inLo = getIndexLobbyWhereIsHrac(getHracIndex(client_socket));
					if(count == 0)
					{
						printf("Odpocet neni spusten\n");
						if(isEveryoneReady(inLo) == 1)
						{
							printf("count == 0, proslo\n");
							//spustit countdown do zacatku hry a posli to vsem v lobby
							broadcastToAllLobby(lobbies[inLo].hraciLobby, "$game!start!#\n");
							printf("Odpocet je spusten!\n");
							count = 1;
						}	
					}
					else
					{
						printf("Odpocet je spusten\n");
						//Kontrolovat, jestli nekdo nedal unready nebo to nelavnul	
						if(isEveryoneReady(inLo) == 0 && isSomeoneReadyLevel2(inLo) == 0)
						{
							printf("count == 1, proslo\n");
							broadcastToAllLobby(lobbies[inLo].hraciLobby, "$game!stop!#\n");
							printf("Odpocet se zastavuje!\n");
							count = 0;
						}
					}
				}
				else if(hraci[getHracIndex(client_socket)].stav == 2)
				{
					printf("Hrac je ve 2. stavu!\n");
					z = rozdeleniZpravyHra(z, client_socket);
					printf("k.error: %d\n", z.error);
					if(z.error < 5)
					{
						send(client_socket, &z.msg, strlen(z.msg), 0);
					}
					else if(z.error == 100)
					{
						printf("[%s]: se odpojil od serveru.\n", jmeno);
						break;
					}
					printf("vracime se zpet na cteni od klienta!\n");
				}
				else if(hraci[getHracIndex(client_socket)].stav == 3)//end game
				{
					printf("Hrac je ve 3. stavu!\n");
					z = rozdeleniZpravyEnd(z, client_socket);
					printf("k.error: %d\n", z.error);
					if(z.error < 5)
					{
						send(client_socket, &z.msg, strlen(z.msg), 0);
					}
					else if(z.error == 100)
					{
						printf("[%s]: se odpojil od serveru.\n", jmeno);
						break;
					}
					printf("vracime se zpet na cteni od klienta!\n");
				}
			}
			else//Spatny format zpravy
			{
				if(z.error < 5 || z.error == 100)
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
	//NACTENI HER
	initGames();
	//NACTENI MAPY
	setupGameBoard();
	
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
	uvolniGameBoard();
	uvolniGames();
	uvolniLobby();
	uvolniHrace();
	printf("HLAVNI PROCES SKONCIL\n");
	return 0;
}

