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
#include "pozemek.h"
#include "game.h"
#include "lobby.h"
#include "zprava_methods.c"
#include "game_array.c"
#include "hrac_methods.c"
#include "lobby_methods.c"
#include "pozemek_methods.c"

//Funkce s posilanim zprav
int broadcastToGame(struct Game* hra, int clientSocket, char* text);

int broadcastToAllGame(struct Game* hra, char* text);

int broadcastToGameToSpecificPlayer(struct Game* hra, int index, char* text);


//Funkce
int setupGameBoard();

void shuffleChestCards(struct Game* hra);

int takeChestCard(struct Game* hra);

void shuffleChanceCards(struct Game* hra);

int takeChanceCard(struct Game* hra);

int isPRUOwned(int pozice, struct Game* hra);

int getIndexForHousing(int index, int pozice, struct Game* hra);

void setGameStatusFull(char *input, struct Game* hra);

void generateGameFullStats(char* text, int size, struct Lobby* lob);

int gameRules(int index, int indexHrace, struct Game* hra);

int makeAction(int index,int  indexHrace, int pozice, struct Game* hra);

void makeActionJail(int index, int indexHrace, int pozice, struct Game* hra);

void makeActionChance(int index, int indexHrace, int pozice, struct Game* hra);

void makeActionChest(int index, int indexHrace, int pozice, struct Game* hra);

int makeActionPRU(int index, int indexHrace, int pozice, struct Game* hra);

void makeActionPropertyOwned(int index, int indexHrace, int pozice, struct Game* hra);

void makeActionRailroadOwned(int index, int indexHrace, int pozice, struct Game* hra);

void makeActionUtilityOwned(int index, int indexHrace, int pozice, struct Game* hra);

int uvolniGameBoard();

int toString(int index);

//Globalni promene
struct Pozemek *game_board;
int length_game_board = 0;

char chestCards[17][10] = {"+200", "+75", "-50", "o", "i", "a10", "a50", "+20",
					"+100", "-100", "-50", "+25", "h40;115", "+10", "+100", 
					"+50", "+100"};

	
char chanceCards[16][10] = {"l0", "l24", "u", "r", "l11", "+50", "jo",
					 "k3", "ji", "h25;100", "t15", "l5", "l39", "c50", 
					 "+150", "+100"};

int setupGameBoard()
{
	char buf[100];
	memset(&buf, '\0', sizeof(buf));
	FILE *board = fopen("CardsInfo.txt", "r");
	
	if(board == NULL)
    {
        perror("Error\n");   
        exit(1);             
    }
    
    int z = 0;
    int ind = 0;
	while(fgets(buf, 100, board)!=NULL)
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
			length_game_board = atoi(buf);
			game_board = malloc(length_game_board * sizeof(struct Pozemek));
		}
		else if(z >= 2)
		{
			//Pridani vsech informaci do konkretniho pozemku
			pthread_mutex_lock(&lockSep);
			separeter(buf, '|');
			int de = length_p;
			char sepa2[de][100];
			for(int i = 0; i < de; i++)
			{
				memset(&sepa2[i], '\0', sizeof(sepa2[i]));
				strcpy(sepa2[i], sepa[i]);
			}
			pthread_mutex_unlock(&lockSep);
			
			if(sepa2[0][0] == 'P')
			{
				game_board[ind] = makePozemek(sepa2[1], sepa2[0][0], atoi(sepa2[2]), atoi(sepa2[4]), sepa2[3], atoi(sepa2[5]), atoi(sepa2[6]));	
			}
			else if(sepa2[0][0] == 'R' || sepa[0][0] == 'U')
			{
				game_board[ind] = makePozemek(sepa2[1], sepa2[0][0], 0, 0, "", 0, 0);
			}
			else if(sepa2[0][0] == 'C')
			{
				game_board[ind] = makePozemek("Community chest", sepa2[0][0], 0, 0, "", 0, 0);
			}
			else if(sepa2[0][0] == 'H')
			{
				game_board[ind] = makePozemek("Chance", sepa2[0][0], 0, 0, "", 0, 0);
			}
			else if(sepa2[0][0] == 'T')
			{
				game_board[ind] = makePozemek("Tax", sepa2[0][0], atoi(sepa2[1]), 0, "", 0, 0);
			}
			else if(sepa2[0][0] == 'S')
			{
				game_board[ind] = makePozemek("Start", sepa2[0][0], 0, 0, "", 0, 0);
			}
			else if(sepa2[0][0] == 'J')
			{
				game_board[ind] = makePozemek("Jail", sepa2[0][0], 0, 0, "", 0, 0);
			}
			else if(sepa2[0][0] == 'L')
			{
				game_board[ind] = makePozemek("Parking lot", sepa2[0][0], 0, 0, "", 0, 0);
			}
			else if(sepa2[0][0] == 'G')
			{
				game_board[ind] = makePozemek("Go to jail", sepa2[0][0], 0, 0, "", 0, 0);
			}
			//int z = toString(ind);
			ind++;
			//strcpy(whitelist[ind++], buf);
			//printf("nacteniF: |%s|\n", whitelist[ind - 1]);	
		}
		memset(&buf, '\0', sizeof(buf));
		z++;	
	}
	fclose(board);
	printf("GAME BOARD NACTEN\n");
	return 0;
}

//index - index ve hre, indexHrace - index v poli hracu
int gameRules(int index, int indexHrace, struct Game* hra)
{
	//printf("Hrac " + hraci[index].getName() + " je na tahu!\n");
	//System.out.println("Nachazi se na pozici: " + hraci[index].getPosition());
	char t[50];
	if(hra->hodStejnych == 3)
	{
		hra->poziceHracu[index] = 10;
		hra->vezeni[index] = 1;
		printf("Hrac %s jde do vezeni!\n", hraci[indexHrace].jmeno);
		hra->hodStejnych = 0;
	}
	else
	{
		if(hra->vezeni[index] == 0)
		{
			int kostka1 = randint(6) + 1;
			int kostka2 = randint(6) + 1;
			//int kostka1 = 3;
			//int kostka2 = 1;
			hra->poslHod[0] = kostka1;
			hra->poslHod[1] = kostka2;
			int hod = kostka1 + kostka2;
			printf("%s hodil %d a %d\n", hraci[indexHrace].jmeno, kostka1, kostka2);
			memset(&t, '\0', sizeof(t));
			sprintf(t, "$game!start!%d,%d!#\n", kostka1, kostka2);
			broadcastToAllGame(hra, t);
			if(hra->poziceHracu[index] + hod > 39)
			{
				hra->penize[index] += 200;
				printf("%s ziskal za projezd startem 200 penez.\n", hraci[indexHrace].jmeno);
				hod = (hra->poziceHracu[index] + hod) % 40;
				hra->poziceHracu[index] = hod;
			}
			else
			{
				hra->poziceHracu[index] += hod;
			}
			//provest u konkretniho policka vsechny akce, ke kterym ma dojit
			//index ve hre, index hrace v celkovem poli hracu, pozice hrace, konkretni hra
			printf("%s, penize: %d, pozice: %d, misto: %s\n", hraci[indexHrace].jmeno, hra->penize[index], hra->poziceHracu[index], game_board[hra->poziceHracu[index]].nazev);
			int re = makeAction(index, indexHrace, hra->poziceHracu[index], hra);
			if(re == 1)
			{
				return 1;
			}
			//gameTable[hraci[index].getPosition()].action(hraci[index], hraci);
			if(kostka1 == kostka2 && hra->vezeni[index] != 1)
			{
				hra->hodStejnych++;
				hra->anotherRun = 1;
			}
			else
			{
				hra->hodStejnych = 0;
			}
		}
		else
		{
			printf("%s je ve vezeni!\n", hraci[indexHrace].jmeno);
			printf("%s, penize: %d, pozice: %d, misto: %s\n", hraci[indexHrace].jmeno, hra->penize[index], hra->poziceHracu[index], game_board[hra->poziceHracu[index]].nazev);
			makeAction(index, indexHrace, hra->poziceHracu[index], hra);//Jenom probehne vezeni
			//gameTable[hraci[index].getPosition()].action(hraci[index], hraci);
		}
		if(game_board[hra->poziceHracu[index]].typPozemku == 1)
		{
			//Dale jestli vsechny ostatni domy v kategorii vlastni take ta sama osoba
			//Tak se ma spustit funkce, ktera provede nakupy domu do techto oblasti
			/**
			if(((Property)gameTable[hraci[index].getPosition()]).getOwnPlayerID() == hraci[index].getId())
			{
				addHouses(hraci[index]);					
			}
			*/
		}
		if(hra->penize[index] <= 0)//Prohral hrac
		{
			hra->changeOfPlayers = 1;
			memset(&t, '\0', sizeof(t));
			sprintf(t, "$game!lose!%d!#\n", index);
			broadcastToAllGame(hra, t);
			//TRANSMISSION!!!
			//transmission("l!" + index + "!");
			//TRANSMISSION!!!
		}
		if(hra->anotherRun != 1)//!anotherRun - jestlize dany hrac nehraju znovu, tak ..
		{
			hra->natahu++;
			if(hra->natahu == 4)
			{
				hra->natahu = 0;
			}
			int nasel = 1;
			while(nasel)
			{
				if(hra->penize[hra->natahu] <= 0)
				{
					hra->natahu++;
					if(hra->natahu == 4)
					{
						hra->natahu = 0;
					}
				}
				else
				{
					nasel = 0;
				}
			}
			//!!
			//TRANSMISSION!!!
			//transmission("e!");
			//TRANSMISSION!!!				
		}
		else // jestli-ze dany hrac hazi znova
		{
			printf("Hrac %s hraje znovu.\n", hra->jmena[index]);
			//!!
			//TRANSMISSION!!!
			//transmission("n!");
			//TRANSMISSION!!!
		}
		printf("post %s, penize: %d, pozice: %d, misto: %s\n", hraci[indexHrace].jmeno, hra->penize[index], hra->poziceHracu[index], game_board[hra->poziceHracu[index]].nazev);
	}
	return 0;
}

int gameRulesPost(int index, int indexHrace, struct Game* hra)
{
	char t[50];
	if(hra->poslHod[0] == hra->poslHod[1] && hra->vezeni[index] != 1)
	{
		hra->hodStejnych++;
		hra->anotherRun = 1;
	}
	else
	{
		hra->hodStejnych = 0;
	}	
	
	if(game_board[hra->poziceHracu[index]].typPozemku == 1)
	{
		//Dale jestli vsechny ostatni domy v kategorii vlastni take ta sama osoba
		//Tak se ma spustit funkce, ktera provede nakupy domu do techto oblasti
		/**
		if(((Property)gameTable[hraci[index].getPosition()]).getOwnPlayerID() == hraci[index].getId())
		{
			addHouses(hraci[index]);					
		}
		*/
	}
	if(hra->penize[index] <= 0)//Prohral hrac
	{
		hra->changeOfPlayers = 1;
		memset(&t, '\0', sizeof(t));
		sprintf(t, "$game!lose!%d!#\n", index);
		broadcastToAllGame(hra, t);
		//TRANSMISSION!!!
		//transmission("l!" + index + "!");
		//TRANSMISSION!!!
	}
	if(hra->anotherRun != 1)//!anotherRun - jestlize dany hrac nehraju znovu, tak ...
	{
		hra->natahu++;
		if(hra->natahu == 4)
		{
			hra->natahu = 0;
		}
		int nasel = 1;
		while(nasel)
		{
			if(hra->penize[hra->natahu] <= 0)
			{
				hra->natahu++;
				if(hra->natahu == 4)
				{
					hra->natahu = 0;
				}
			}
			else
			{
				nasel = 0;
			}
		}
		//TRANSMISSION!!!
		//transmission("e!");
		//TRANSMISSION!!!				
	}
	else // jestlize dany hrac hazi znova
	{
		//TRANSMISSION!!!
		//transmission("n!");
		//TRANSMISSION!!!
	}
}

int makeAction(int index, int  indexHrace, int pozice, struct Game* hra)
{
	int typ = game_board[pozice].typPozemku;
	int pay;
	int re = -1;
	char t[50];
	switch(typ)
	{
		case 1:re = makeActionPRU(index, indexHrace, pozice, hra);
		       if(re == 1)
			   {
			   	   return 1;	
			   }
			   break;
		case 2:re = makeActionPRU(index, indexHrace, pozice, hra);
			   if(re == 1)
			   {
			   	   return 1;	
			   }
		       break;
		case 3:re = makeActionPRU(index, indexHrace, pozice, hra);
			   if(re == 1)
			   {
			   	   return 1;	
			   }
			   break;
		case 4:makeActionChest(index, indexHrace, pozice, hra);
			   break;
	    case 5:makeActionChance(index, indexHrace, pozice, hra);
			   break;
	    case 6:pay = game_board[pozice].cena;
			   hra->penize[index] -= pay;
			   memset(&t, '\0', sizeof(t));
			   sprintf(t, "$game!pay!%d!%d!#\n", index, pay);
			   broadcastToAllGame(hra, t);
			   //TRANSMISSION!!!
			   //Table.transmission("p!" + index + "!" + pay +  "!");
			   //TRANSMISSION!!!
			   printf("%s zaplatil bance %d poplatek.\n", hraci[indexHrace].jmeno, pay);
			   break;
	    case 7://Prave se nachazi na startu
			   break;
	    case 8:makeActionJail(index, indexHrace, pozice, hra);
			   break;
	    case 9:printf("%s stoji na parkovisti.\n", hraci[indexHrace].jmeno);
			   break;
	    case 10:hra->poziceHracu[index] = 10;
	    		hra->vezeni[index] = 1;
	    		printf("Hrac %s jde do vezeni!\n", hraci[indexHrace].jmeno);
	    		memset(&t, '\0', sizeof(t));
				strcpy(t, "$game!gojail!#\n");
				broadcastToAllGame(hra, t);
				//transmission JAIL j!i!
			    break;
	}
	return 0;
}

void makeActionJail(int index, int indexHrace, int pozice, struct Game* hra)
{
	if(hra->jailFree == index)
	{
		printf("%s pouzil kartu, aby se dostal z vezeni.\n", hraci[indexHrace].jmeno);
		printf("%s se dostal z vezeni a haze koustkou.\n", hraci[indexHrace].jmeno);
		hra->jailFree = -1;
		hra->anotherRun = 1;
		hra->vezeni[index] = 0;
	}
	else if(hra->vezeni[index] == 1)
	{
		printf("%s hazi kostky, aby se dostal z vezeni.\n", hraci[indexHrace].jmeno);
		int kostka1 = randint(6) + 1;
		int kostka2 = randint(6) + 1;
		printf("%s hodil %d a %d.\n", hraci[indexHrace].jmeno, kostka1, kostka2);
		if(kostka1 == kostka2)
		{
			hra->vezeni[index] = 0;
			printf("%s se dostal z vezeni.\n", hraci[indexHrace].jmeno);
			hra->anotherRun = 1;
		}
		else
		{
			hra->vezeniLuck[index] += 1;
			if(hra->vezeniLuck[index] == 5)
			{
				hra->vezeni[index] = 0;
				printf("%s se dostal z vezeni na milost.\n", hraci[indexHrace].jmeno);
				hra->anotherRun = 1;
			}
			printf("%s je stale ve vezeni.\n", hraci[indexHrace].jmeno);
		}
	}
}

int broadcastToGame(struct Game* hra, int clientSocket, char* text)
{
	int id = hra->idLobby;
	int indL = -1;
	for(int i =0; i < length_lobbies; i++)
	{
		if(lobbies[i].idLobby == id)
		{
			indL = i;
			break;
		}
	}
	char text1[1000] = "";
	strcpy(text1, text);
	broadcastToLobby(lobbies[indL].hraciLobby, clientSocket, text1);
}

int broadcastToAllGame(struct Game* hra, char* text)
{
	int id = hra->idLobby;
	int indL = -1;
	for(int i =0; i < length_lobbies; i++)
	{
		if(lobbies[i].idLobby == id)
		{
			indL = i;
			break;
		}
	}
	char text1[1000] = "";
	strcpy(text1, text);
	broadcastToAllLobby(lobbies[indL].hraciLobby, text1);
}

int broadcastToGameToSpecificPlayer(struct Game* hra, int index, char* text)
{
	int id = hra->idLobby;
	int indL = -1;
	for(int i =0; i < length_lobbies; i++)
	{
		if(lobbies[i].idLobby == id)
		{
			indL = i;
			break;
		}
	}
	char text1[1000] = "";
	strcpy(text1, text);
	
	pthread_mutex_lock(&lockLobby);
	
	int indH = lobbies[indL].hraciLobby[index];
	
	pthread_mutex_unlock(&lockLobby);
	pthread_mutex_lock(&lock);
	
	send(hraci[indH].client_socket, &text1, strlen(text1), 0);
	
	pthread_mutex_unlock(&lock);
}

/**
 * Funkce, ktera proved kontrolu, jestli na dane
 * pozici nevlastni nekdo tento pozemek.
 *
 * return		index hrace kdo vlastni, nebo
 *				-1 pokud nikdo
 */
int isPRUOwned(int pozice, struct Game* hra)
{
	for(int i = 0; i < 4; i++)
	{
		if(hra->penize[i] > 0)
		{
			pthread_mutex_lock(&lockSep);
			
			separeter(hra->budovy[i], ',');
			int delka = length_p;
			char pom[delka][100];
			for(int j = 0; j < delka; j++)
			{
				memset(&pom[j], '\0', sizeof(pom[j]));
				strcpy(pom[j], sepa[j]);
			}
			
			pthread_mutex_unlock(&lockSep);
			//printf("isPRU %d hrac s poctem budov %d pro pozici %d.\n", i, delka, pozice);
			for(int j = 0; j < delka; j++)
			{
				//printf("%s,", pom[j]);
				if(pozice == atoi(pom[j]))
				{
					return i;
				}
			}
			//printf("\n");		
		}	
	}
	//	printf("isPRUOwned() nenasel index!\n");
	return -1;
}
/**
 *Funkce, ktera zjisti na zaklade cisla
 *v retezci budov index daneho umisteni
 *v tomto retezci a vrati tuto hodnotu.
 */
int getIndexForHousing(int index, int pozice, struct Game* hra)
{
	pthread_mutex_lock(&lockSep);
	//uzamknout
	separeter(hra->budovy[index], ',');
	int delka = length_p;
	char pom[delka][100];
	for(int i = 0; i < delka; i++)
	{
		memset(&pom[i], '\0', sizeof(pom[i]));
		strcpy(pom[i], sepa[i]);
	}
	//odemknout
	pthread_mutex_unlock(&lockSep);
	for(int i = 0; i < delka; i++)
	{
		if(pozice == atoi(pom[i]))
		{
			return i;
		}
	}
	printf("getIndexForHousing() nenasel index!\n");
	return -1;
}

int rozdeleniZpravyBuyTmp(struct Zprava z, int cl)
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
		//!!
		strcpy(k.msg, "V zadanem textu neni symbol '!'.\n");
		k.length = strlen(k.msg);
		k.error = 2;
		return -2;
	}
	//printf("%s\n", front);
	//printf("%s\n", back);
	if(strcmp(front, "game") == 0)
	{
		if(strcmp(back, "buy") == 0)
		{
			return 1;
		}
		else if(strcmp(back, "auction") == 0)
		{
			return 2;
		}
		else
		{
			return -1;
		}
	}
	else
	{
		return -1;
	}
}

void makeActionPRUAuction(int index, int indexHrace, int typOdpovedi, int castka, struct Game* hra)
{
	char t[50];
	memset(&t, '\0', sizeof(t));
	//hra->aukce.auctionPrice[i] != -1 && hra->aukce.peopleDone < hra->aukce.pocetHrajicich - 1
	
	printf("\nHrac %s ma %d.\n", hra->jmena[index], hra->penize[index]);
	printf("Aktualni nejvyssi cena strediska je %d penez.\n", hra->aukce.max);
	printf("%s nabizi za pozemek %d penez.\n", hra->jmena[index], hra->aukce.auctionPrice[index]);
	printf("Pokud chcete prihodit, zadejte hodnotu , kterou jste ochotni za pozemek zaplatit.\n");
	printf("Pokud uz nechcete prihodit, zadejte do koznole <K>\n");
	
	//Informace od klienta
	//typOdpovedi 0- ukonceni, 1-prihozeni
	if(typOdpovedi == 0)
	{
		hra->aukce.auctionPrice[index] = -1;
		hra->aukce.peopleDone++;
		memset(&t, '\0', sizeof(t));
		strcpy(t, "$game!aukce!done!#\n");
		broadcastToAllGame(hra, t);
		//TRANSMISSION!!!
		//Table.transmission("a!k!");
		//TRANSMISSION!!!
	}
	else if(typOdpovedi == 1)
	{
		int hod = castka;
		if(hod > hra->penize[index])
		{
			printf("Nelze provest prihozeni! - Nedostatek penez\n");
			memset(&t, '\0', sizeof(t));
			strcpy(t, "$game!aukce!fail!#\n");
			broadcastToAllGame(hra, t);
			return;
			//TRANSMISSION!!!
			//Table.transmission("a!f!");
			//TRANSMISSION!!!
		}
		else
		{
			if(hod <= hra->aukce.max)
			{
				printf("Prihozeni je mensi nez max: %d, prihodte vice nebo zruste prihazovani!\n",hra->aukce.max);
				memset(&t, '\0', sizeof(t));
				strcpy(t, "$game!aukce!fail!#\n");
				broadcastToAllGame(hra, t);
				return;
				//TRANSMISSION!!!
				//.transmission("a!f!");
				//TRANSMISSION!!!
			}
			else
			{
				hra->aukce.max = hod;
				hra->aukce.auctionPrice[index] = hod;
				memset(&t, '\0', sizeof(t));
				sprintf(t, "$game!aukce!max!%d!#\n", hod);
			    broadcastToAllGame(hra, t);
				//TRANSMISSION!!!
				//Table.transmission("a!a!" + max + "!");
				//TRANSMISSION!!!																								
			}												
		}
	}
	else
	{
		printf("Error - chyba pri u typOdpovedi %d, neseedi ani jedna podm.!\n", typOdpovedi);
		return;
	}
	
	//Posunuti na dalsiho hrace, ktery je v aukci stale pritomen
	printf("PeopleDone: %d\n", hra->aukce.peopleDone);
	printf("pocetHrajicich: %d\n", hra->aukce.pocetHrajicich);
	if(hra->aukce.peopleDone >= hra->aukce.pocetHrajicich - 1)
	{
		hra->aukce.auction = 0;
	}
	else
	{
		hra->aukce.aukceNatahu++;
		if(hra->aukce.aukceNatahu == 4)
		{
			hra->aukce.aukceNatahu = 0;
		}
		memset(&t, '\0', sizeof(t));
		strcpy(t, "$game!aukce!next!#\n");
		broadcastToAllGame(hra, t);
		int nasel = 1;
		while(nasel)
		{
			if(hra->aukce.auctionPrice[hra->aukce.aukceNatahu] == -1)
			{
				if(hra->penize[hra->aukce.aukceNatahu] > 0)
				{
					//Poslat ze ma skocit na dalsiho hrace
					memset(&t, '\0', sizeof(t));
					strcpy(t, "$game!aukce!next!#\n");
					broadcastToAllGame(hra, t);
					//TRANSMISSION!!!
					//Table.transmission("a!n!");
					//TRANSMISSION!!!
				}
				hra->aukce.aukceNatahu++;
				if(hra->aukce.aukceNatahu == 4)
				{
					hra->aukce.aukceNatahu = 0;
				}
			}
			else
			{
				nasel = 0;
			}
		}	
	}
	
	//Pokud uz je aukce kompletni, provest detekci kdo pozemek ziskal
	//a nasledne mu privlastnit tento pozemek	
	if(hra->aukce.auction == 0)
	{
		int index3 = -1;
		for(int i = 0; i < 4; i++)
		{
			if(hra->aukce.auctionPrice[i] != -1)
			{
				index3 = i;
			}
		}
		printf("%s ziskal stredisko z aukce za %d.\n", hra->jmena[index3], hra->aukce.auctionPrice[index3]);
		hra->penize[index3] -= hra->aukce.auctionPrice[index3];
		sprintf(hra->budovy[index3], "%s%d,", hra->budovy[index3], hra->aukce.pozice);
		sprintf(hra->upgrady[index3], "%s0,", hra->upgrady[index3]);
		memset(&t, '\0', sizeof(t));
		sprintf(t, "$game!aukce!end!#\n");
		broadcastToAllGame(hra, t);
		memset(&t, '\0', sizeof(t));
		sprintf(t, "$game!pay!%d!%d!#\n", index3, hra->aukce.auctionPrice[index3]);
		broadcastToAllGame(hra, t);
		memset(&t, '\0', sizeof(t));
		sprintf(t, "$game!buy!%d!%d!#\n", index3, hra->aukce.pozice);
		broadcastToAllGame(hra, t);
		hra->aukce.auction = 2;
		//TRANSMISSION!!!
		//Table.transmission("p!" + index + "!" + auctionPrice[index] + "!");
		//Table.transmission("b!"+ index + "!" + hrac.getPosition() + "!");
		//TRANSMISSION!!!
	}
}

int makeActionPRU(int index, int indexHrace, int pozice, struct Game* hra)
{
	int typPoz = game_board[pozice].typPozemku;
	int cenaPoz = game_board[pozice].cena;
	char nazevPoz[25];
	char t[50];
	memset(&nazevPoz, '\0', sizeof(nazevPoz));
	if(typPoz == 1)
	{
		strcpy(nazevPoz, "Property");
	}
	else if(typPoz == 2)
	{
		strcpy(nazevPoz, "Railroad");
	}
	else
	{
		strcpy(nazevPoz, "Utility");
	}
	if(isPRUOwned(pozice, hra) == -1)
	{
		//Posilam klientovi
		printf("%s %s stoji %d.\n", nazevPoz, game_board[pozice].nazev, game_board[pozice].cena);
		printf("<k> - Koupit utility!\n");
		printf("<a> - Aukcni sin!\n");
		int done = 1;
		while(done == 1)
		{
			//Cekam na zpravu od Klienta
			int cl = hraci[indexHrace].client_socket;
			struct Zprava z = getMessage(cl);
			int coD = rozdeleniZpravyBuyTmp(z, cl);
			//char in[10] = "k\n";
			if(coD > -1)//!in.equals("") - Prijde prazdny retezec, opakuj cely proces
			{
				if(coD == 1)
				{
					if(hra->penize[index] - cenaPoz < 0)
					{
						printf("Nelze provest koupi! - Nedostatek penez.\n");
						memset(&t, '\0', sizeof(t));
						strcpy(t, "$game!buy!fail!#\n");
						broadcastToGameToSpecificPlayer(hra, index, t);
					}
					else
					{
						hra->penize[index] -= cenaPoz;
						sprintf(hra->budovy[index], "%s%d,", hra->budovy[index], pozice);
						sprintf(hra->upgrady[index], "%s0,", hra->upgrady[index]);
						printf("%s koupil pozemek %s za %d.\n", hraci[indexHrace].jmeno, game_board[pozice].nazev, cenaPoz);
						
						memset(&t, '\0', sizeof(t));
						sprintf(t, "$game!pay!%d!%d!#\n", index, cenaPoz);
						broadcastToAllGame(hra, t);
						memset(&t, '\0', sizeof(t));
						sprintf(t, "$game!buy!%d!%d!#\n", index, pozice);
						broadcastToAllGame(hra, t);
						done = 0;
						//TRANSMISSION!!!
						//Table.transmission("b!" + index + "!" + hrac.getPosition() + "!");
						//Table.transmission("p!" + index + "!" + 200 + "!");
						//TRANSMISSION!!!
					}
				}
				else if(coD == 2)
				{
					//Nastavit return na 1, z duvodu preskoceni na aukce
					hra->aukce.auction = 1;
					hra->aukce.pocetHrajicich = 0;
					hra->aukce.peopleDone = 0;
					hra->aukce.max = 0;
					hra->aukce.aukceNatahu = index;
					hra->aukce.pozice = pozice;
					for(int i = 0; i < 4; i++)//Nastaveni nehrajicim hracum
					{
						if(hra->penize[i] <= 0)
						{
							hra->aukce.auctionPrice[i] = -1;
						}
						else
						{
							hra->aukce.auctionPrice[i] = 0;
							hra->aukce.pocetHrajicich++;
						}
					}
					done = 0;
					return 1;
				}
			}
		}
	}
	else
	{
		if(typPoz == 1)
		{
			makeActionPropertyOwned(index, indexHrace, pozice, hra);	
		}
		else if(typPoz == 2)
		{
			makeActionRailroadOwned(index, indexHrace, pozice, hra);
		}
		else
		{
			makeActionUtilityOwned(index, indexHrace, pozice, hra);
		}
	}
	return 0;
}

void makeActionPropertyOwned(int index, int indexHrace, int pozice, struct Game* hra)
{
	char t[50];
	int index4 = isPRUOwned(pozice, hra);//index hrace, ktery vlastni tento pozemek
	printf("Pozemek vlastni %s.\n", hra->jmena[index4]);
	
	int suma = 0;
	int multi = 1;
	for(int i = 0; i < length_game_board; i++)
	{
		if(game_board[i].typPozemku == 1)
		{
			if(isPRUOwned(i, hra) == index4)
			{
				if(game_board[i].kategorie[0] == game_board[pozice].kategorie[0])
				{
					suma++;
				}	
			}					
		}
	}
	if(suma == game_board[pozice].kategorie[1])//Jestli pocet vlastnenych je stejny jako celkovy pocet v dane kategorii
	{
		multi = 2;
	}
	printf("%s vlastni %d stejne pozemky z grupy %d z max. poctu %d.\n", hra->jmena[index4], suma, game_board[pozice].kategorie[0], game_board[pozice].kategorie[1]);
	
	int index5 = getIndexForHousing(index, pozice, hra);//Index umisteni v retezci s upgrady u konkretniho hrace
	
	pthread_mutex_lock(&lockSep);
	separeter(hra->upgrady[index], ',');
	char pom3[100];
	memset(&pom3, '\0', sizeof(pom3));
	strcpy(pom3, sepa[index5]);
	pthread_mutex_unlock(&lockSep);
	
	int house = atoi(pom3);
	
	if(house == 6)//Pokud na danem pozmeku je jiz hotel
	{
		int cast2 = game_board[pozice].zisky[5];
		printf("%s zaplati %s %d.\n", hraci[indexHrace].jmeno, hra->jmena[index4], cast2);
		hra->penize[index4] += cast2;
		hra->penize[index] -= cast2;
		memset(&t, '\0', sizeof(t));
		sprintf(t, "$game!pay!%d!%d!#\n", index, cast2);
		broadcastToAllGame(hra, t);
		memset(&t, '\0', sizeof(t));
		sprintf(t, "$game!get!%d!%d!#\n", index4, cast2);
		broadcastToAllGame(hra, t);
		//TRANSMISSION!!!
		//Table.transmission("p!"+ ind + "!" + this.getHousesAndHotelRent()[5] + "!");
		//Table.transmission("g!"+ index + "!" + this.getHousesAndHotelRent()[5] + "!");
		//TRANSMISSION!!!
	}
	else if(house > 0)//Jestli na danem pozemku jsou nejake domy
	{
		int cast3 = game_board[pozice].zisky[house];
		printf("%s zaplati %s %d.\n", hraci[indexHrace].jmeno, hra->jmena[index4], cast3);
		hra->penize[index4] += cast3;
		hra->penize[index] -= cast3;
		memset(&t, '\0', sizeof(t));
		sprintf(t, "$game!pay!%d!%d!#\n", index, cast3);
		broadcastToAllGame(hra, t);
		memset(&t, '\0', sizeof(t));
		sprintf(t, "$game!get!%d!%d!#\n", index4, cast3);
		broadcastToAllGame(hra, t);
		//TRANSMISSION!!!
		//Table.transmission("p!"+ ind + "!" + this.getHousesAndHotelRent()[houses] + "!");
		//Table.transmission("g!"+ index + "!" + this.getHousesAndHotelRent()[houses] + "!");
		//TRANSMISSION!!!
	}
	else//Zakladni pozemek
	{
		int cast4 = game_board[pozice].zisky[0] * multi;
		printf("%s zaplati %s %d.\n", hraci[indexHrace].jmeno, hra->jmena[index4], cast4);
		hra->penize[index4] += cast4;
		hra->penize[index] -= cast4;
		memset(&t, '\0', sizeof(t));
		sprintf(t, "$game!pay!%d!%d!#\n", index, cast4);
		broadcastToAllGame(hra, t);
		memset(&t, '\0', sizeof(t));
		sprintf(t, "$game!get!%d!%d!#\n", index4, cast4);
		broadcastToAllGame(hra, t);
		//TRANSMISSION!!!
		//Table.transmission("p!"+ ind + "!" + this.getHousesAndHotelRent()[0] * multi + "!");
		//Table.transmission("g!"+ index + "!" + this.getHousesAndHotelRent()[0] * multi + "!");
		//TRANSMISSION!!!
	}
}

void makeActionRailroadOwned(int index, int indexHrace, int pozice, struct Game* hra)
{
	char t[50];
	int index4 = isPRUOwned(pozice, hra);//index hrace, ktery vlastni tento pozemek
	printf("Pozemek vlastni %s.\n", hra->jmena[index4]);
	
	int suma = 0;
	for(int i = 0; i < length_game_board; i++)
	{
		if(game_board[i].typPozemku == 2)
		{
			if(isPRUOwned(i, hra) == index4)
			{
				suma++;
			}					
		}
	}
	
	printf("%s vlastni %d stanic.\n", hra->jmena[index4],suma);
	int castka = game_board[pozice].zisky[suma - 1];
	printf("%s zaplati %s %d.\n", hraci[indexHrace].jmeno, hra->jmena[index4], castka);
	hra->penize[index4] += castka;
	hra->penize[index] -= castka;
	memset(&t, '\0', sizeof(t));
	sprintf(t, "$game!pay!%d!%d!#\n", index, castka);
	broadcastToAllGame(hra, t);
	memset(&t, '\0', sizeof(t));
	sprintf(t, "$game!get!%d!%d!#\n", index4, castka);
	broadcastToAllGame(hra, t);
	//TRANSMISSION!!!
	//Table.transmission("p!"+ ind + "!" + rent[suma - 1] + "!");
	//Table.transmission("g!"+ index + "!" + rent[suma - 1] + "!");
	//TRANSMISSION!!!
}

void makeActionUtilityOwned(int index, int indexHrace, int pozice, struct Game* hra)
{
	char t[50];
	int index4 = isPRUOwned(pozice, hra);//index hrace, ktery vlastni tento pozemek
	printf("Pozemek vlastni %s.\n", hra->jmena[index4]);
	
	int count = 0;
	for(int i = 0; i < length_game_board; i++)//Jestli jeste hrac nevlastni dalsi Utility pozemky
	{
		if(game_board[i].typPozemku == 3)
		{
			if(isPRUOwned(i, hra) == index4)
			{
				count++;
			}					
		}
	}
	int multi[2] = {4,10};
	printf("count: %d\n", count);
	int kostka1 = randint(6) + 1;
	int kostka2 = randint(6) + 1;
	printf("%s hodil %d + %d\n", hraci[indexHrace].jmeno, kostka1, kostka2);
	printf("%s vlastni %d stredisek.\n", hra->jmena[index4], count);
	int zap = (kostka1 + kostka2) * multi[count - 1];
	printf("%s zaplati %s %d.\n", hraci[indexHrace].jmeno, hra->jmena[index4], zap);
	hra->penize[index4] += zap;
	hra->penize[index] -= zap;
	memset(&t, '\0', sizeof(t));
	sprintf(t, "$game!pay!%d!%d!#\n", index, zap);
	broadcastToAllGame(hra, t);
	memset(&t, '\0', sizeof(t));
	sprintf(t, "$game!get!%d!%d!#\n", index4, zap);
	broadcastToAllGame(hra, t);
	//TRANSMISSION!!!
	//Table.transmission("p!"+ ind + "!" + (kostka1 + kostka2)*multi[count - 1] + "!");
	//Table.transmission("g!"+ index + "!" + (kostka1 + kostka2)*multi[count - 1] + "!");
	//TRANSMISSION!!!
}


void makeActionChest(int index, int indexHrace, int pozice, struct Game* hra)
{
	char t[50];
	char whatToDo[10];
	memset(&whatToDo, '\0', sizeof(whatToDo));
	int co = takeChestCard(hra);
	strcpy(whatToDo, chestCards[co]);
	printf("%s si lizl kartu z comm. chest: %s\n", hraci[indexHrace].jmeno, whatToDo);
	char subbuff[10];
	switch(whatToDo[0])
	{
	case '+':memset(&subbuff, '\0', sizeof(subbuff));
	         memcpy(subbuff, &whatToDo[1], strlen(whatToDo) - 1);
	         //strncpy(subbuff, whatToDo+1, 4);
	         printf("chest + %s s delkou %d\n", subbuff, (int)strlen(subbuff));
			 hra->penize[index] += atoi(subbuff);
			 printf("%s ziskal %s penez.\n", hraci[indexHrace].jmeno, subbuff);
		     break;
	case '-':memset(&subbuff, '\0', sizeof(subbuff));
	         memcpy(subbuff, &whatToDo[1], strlen(whatToDo) - 1);
	         printf("chest - %s\n", subbuff);
			 hra->penize[index] -= atoi(subbuff);
			 printf("%s zaplatil %s penez.\n", hraci[indexHrace].jmeno, subbuff);
     	     break;
	case 'o':hra->jailFree = index;
			 printf("%s ziskal kartu, ktera mu umozni se dostat z vezeni.\n", hraci[indexHrace].jmeno);
			 printf("Po pouziti tuto kartu ztraci!\n");
	         break;
	case 'i':hra->poziceHracu[index] = 10;
			 hra->vezeni[index] = 1;
			 printf("%s odchazi do vezeni!\n", hraci[indexHrace].jmeno);
			 break;
	case 'a':memset(&subbuff, '\0', sizeof(subbuff));
	         memcpy(subbuff, &whatToDo[1], strlen(whatToDo) - 1);
	         printf("chest a %s\n", subbuff);
	 		 int money = atoi(subbuff);
			 for(int i = 0; i < 4; i++)
			 {
				  if(hra->penize[i] > 0)
				  {
				  	    if(index != i)
				  	    {
				  	   		hra->penize[index] += money;
				  	   		hra->penize[i] -= money;
					    }
				  }
			 }
			 printf("Kazdy hrac zaplatil %s %d penez.\n", hraci[indexHrace].jmeno, money);
			 break;
	case 'h':memset(&subbuff, '\0', sizeof(subbuff));
	         memcpy(subbuff, &whatToDo[1], strlen(whatToDo) - 1);
	         
	         pthread_mutex_lock(&lockSep);
	         
			 separeter(subbuff, ';');
			 int delka = length_p;
			 char pomS[delka][25];
			 for(int i = 0; i < delka; i++)
			 {
			 	memset(&pomS[i], '\0', sizeof(pomS[i]));
			 	strcpy(pomS[i], sepa[i]);
			 }
			 
			 pthread_mutex_unlock(&lockSep);
			 
			 int hotel = 0, houses = 0;
			 
			 pthread_mutex_lock(&lockSep);
	         
			 separeter(hra->upgrady[index], ',');
			 int delka2 = length_p;
			 char pomS2[delka2][20];
			 for(int i = 0; i < delka2; i++)
			 {
			 	memset(&pomS2[i], '\0', sizeof(pomS2[i]));
			 	strcpy(pomS2[i], sepa[i]);
			 }
			 
			 pthread_mutex_unlock(&lockSep);
			 
			 for(int i = 0; i < delka2; i++)
			 {
			 	int tmpInt = atoi(pomS2[i]);
			 	if(tmpInt == 6)
			 	{
			 		hotel++;
				}
				else
				{
					houses += tmpInt;
				}
			 }
			 
			 int cost1 = atoi(pomS[0]);
			 int cost2 = atoi(pomS[1]);
			 hra->penize[index] -= houses * cost1;
			 hra->penize[index] -= hotel * cost2;
			 printf("%s musi zaplatit bance za kazdy svuj hotel(%d penez) a kazdy svuj dum(%d penez).\n", hraci[indexHrace].jmeno, cost2, cost1);
			 printf("Tedy celkove predal bance: %d penez za domy a %d penez za hotely!\n", (houses * cost1), (hotel * cost2));
			 break;
	}
	memset(&t, '\0', sizeof(t));
	sprintf(t, "$game!chest!%s!#\n", whatToDo);
	broadcastToAllGame(hra, t);
	//TRANSMISSION!!!
	//Table.transmission("d!" + whatToDo + "!");
	//TRANSMISSION!!!
}

void makeActionChance(int index, int indexHrace, int pozice, struct Game* hra)
{
	char t[50];
	char whatToDo[10];
	memset(&whatToDo, '\0', sizeof(whatToDo));
	int co = takeChanceCard(hra);
	strcpy(whatToDo, chanceCards[co]);
	printf("%s si lizl kartu chance: %s\n", hraci[indexHrace].jmeno, whatToDo);
	char subbuff[10];
	int ind;
	switch(whatToDo[0])
	{
	case '+':memset(&subbuff, '\0', sizeof(subbuff));
	         memcpy(subbuff, &whatToDo[1], strlen(whatToDo) - 1);
	         printf("chance + %s\n", subbuff);
			 hra->penize[index] += atoi(subbuff);
			 printf("%s ziskal %s penez.\n", hraci[indexHrace].jmeno, subbuff);
			 memset(&t, '\0', sizeof(t));
			 sprintf(t, "$game!chance!%s!#\n", whatToDo);
			 broadcastToAllGame(hra, t);
			 //TRANSMISSION!!!
			 //Table.transmission("c!" + whatToDo + "!");
			 //TRANSMISSION!!!
		     break;
	case '-':memset(&subbuff, '\0', sizeof(subbuff));
	         memcpy(subbuff, &whatToDo[1], strlen(whatToDo) - 1);
	         printf("chance - %s\n", subbuff);
			 hra->penize[index] -= atoi(subbuff);
			 printf("%s ztratil %s penez.\n", hraci[indexHrace].jmeno, subbuff);
			 memset(&t, '\0', sizeof(t));
			 sprintf(t, "$game!chance!%s!#\n", whatToDo);
			 broadcastToAllGame(hra, t);
			 //TRANSMISSION!!!
			 //Table.transmission("c!" + whatToDo + "!");
			 //TRANSMISSION!!!
		     break;
	case 'c':for(int i = 0; i < 4; i++)
			 {
				  if(hra->penize[i] > 0)
				  {
					   if(index != i)
					   {
					  	    hra->penize[i] += 50;
							hra->penize[index] -= 50;		
					   }
				  }
			 }
			 printf("%s ztratil kazdemu hraci 50 penez.\n", hraci[indexHrace].jmeno);
			 memset(&t, '\0', sizeof(t));
			 sprintf(t, "$game!chance!%s!#\n", whatToDo);
			 broadcastToAllGame(hra, t);
			 //TRANSMISSION!!!
			 //Table.transmission("c!" + whatToDo + "!");
			 //TRANSMISSION!!!
			 break;
	case 'l':memset(&subbuff, '\0', sizeof(subbuff));
	         memcpy(subbuff, &whatToDo[1], strlen(whatToDo) - 1);
	         printf("chance l %s\n", subbuff);
			 int targetDest = atoi(subbuff);
			 printf("targetDest: %d\n", targetDest);
			 if(hra->poziceHracu[index] > targetDest)
			 {
				 hra->penize[index] += 200;
				 printf("%s ziskal za projezd startem 200 penez.\n", hraci[indexHrace].jmeno);
			 }
			 hra->poziceHracu[index] = targetDest;
			 printf("%s se presunul na pozici %s.\n", hraci[indexHrace].jmeno, game_board[targetDest].nazev);
			 memset(&t, '\0', sizeof(t));
			 sprintf(t, "$game!chance!%s!#\n", whatToDo);
			 broadcastToAllGame(hra, t);
			 //TRANSMISSION!!!
			 //Table.transmission("c!" + whatToDo + "!");
			 //TRANSMISSION!!!
			 makeAction(index, indexHrace, hra->poziceHracu[index], hra);
			 break;
	case 'j':if(whatToDo[1] == 'i')
			 {
				 hra->vezeni[index] = 1;
				 hra->poziceHracu[index] = 10;
				 printf("%s jde do vezeni!\n", hraci[indexHrace].jmeno);
			 }
			 else
			 {
				 hra->jailFree = index;
				 printf("%s ziskal kartu, ktera mu umozni se dostat z vezeni.\n", hraci[indexHrace].jmeno);
				 printf("Po pouziti tuto kartu ztraci!\n");
			 }
			 memset(&t, '\0', sizeof(t));
			 sprintf(t, "$game!chance!%s!#\n", whatToDo);
			 broadcastToAllGame(hra, t);
			 //TRANSMISSION!!!
			 //Table.transmission("c!" + whatToDo + "!");
			 //TRANSMISSION!!!
			 break;
	case 'k':memset(&subbuff, '\0', sizeof(subbuff));
	         memcpy(subbuff, &whatToDo[1], strlen(whatToDo) - 1);
	         printf("chance k %s\n", subbuff);
	 		 targetDest = atoi(subbuff);
	 		 printf("targetDest: %d\n", targetDest);
	 		 hra->poziceHracu[index] -= targetDest;
	 		 printf("%s se posouva o %d misto/a zpet!\n", hraci[indexHrace].jmeno, targetDest);
	 		 memset(&t, '\0', sizeof(t));
			 sprintf(t, "$game!chance!%s!#\n", whatToDo);
			 broadcastToAllGame(hra, t);
			 //TRANSMISSION!!!
			 //Table.transmission("c!" + whatToDo + "!");
			 //TRANSMISSION!!!
			 makeAction(index, indexHrace, hra->poziceHracu[index], hra);
			 break;
	case 't':memset(&subbuff, '\0', sizeof(subbuff));
	         memcpy(subbuff, &whatToDo[1], strlen(whatToDo) - 1);
	         printf("chance h %s\n", subbuff);
	 	     int money = atoi(subbuff);
	 	     printf("money: %d\n", money);
	 	     hra->penize[index] -= money;
	 	     printf("%s zaplatil bance dane v hodnote %d penez!.\n", hraci[indexHrace].jmeno, money);
	 	     memset(&t, '\0', sizeof(t));
			 sprintf(t, "$game!chance!%s!#\n", whatToDo);
			 broadcastToAllGame(hra, t);
	 	     //TRANSMISSION!!!
	 		 //Table.transmission("c!" + whatToDo + "!");
	 		 //TRANSMISSION!!!
	 	     break;
	case 'u':ind = hra->poziceHracu[index];
			 for(int i = ind + 1; i < 40; i++)//Table.gameTable.length
			 {
				if(i + 1 == 40)
				{
					i = 0;
				}
				else if(game_board[i].typPozemku == 3)
				{
					ind = i;
					break;
				}
			 }
			 if(hra->poziceHracu[index] > ind)
			 {
				 hra->penize[index] += 200;
				 printf("%s ziskal za projezd startem 200 penez.\n", hraci[indexHrace].jmeno);
			 }
			 hra->poziceHracu[index] = ind;
			 printf("%s se presunul na pozici %s.\n", hraci[indexHrace].jmeno, game_board[ind].nazev);
			 memset(&t, '\0', sizeof(t));
			 sprintf(t, "$game!chance!%s!#\n", whatToDo);
			 broadcastToAllGame(hra, t);
		     //TRANSMISSION!!!
		     //Table.transmission("c!" + whatToDo + "!");
		     //TRANSMISSION!!!
		     makeAction(index, indexHrace, hra->poziceHracu[index], hra);
		     break;
	case 'r':ind = hra->poziceHracu[index];
			 for(int i = ind + 1; i < 40; i++)
			 {
				if(i + 1 == 40)
				{
					i = 0;
				}
				else if(game_board[i].typPozemku == 2)
				{
					ind = i;
					break;
				}
			 }
			 if(hra->poziceHracu[index] > ind)
			 {
				 hra->penize[index] += 200;
				 printf("%s ziskal za projezd startem 200 penez.\n", hraci[indexHrace].jmeno);
			 }
			 hra->poziceHracu[index] = ind;
			 printf("%s se presunul na pozici %s.\n", hraci[indexHrace].jmeno, game_board[ind].nazev);
			 memset(&t, '\0', sizeof(t));
			 sprintf(t, "$game!chance!%s!#\n", whatToDo);
			 broadcastToAllGame(hra, t);
		     //TRANSMISSION!!!
		     //Table.transmission("c!" + whatToDo + "!");
		     //TRANSMISSION!!!
		     makeAction(index, indexHrace, hra->poziceHracu[index], hra);
		     break;
	case 'h':memset(&subbuff, '\0', sizeof(subbuff));
	         memcpy(subbuff, &whatToDo[1], strlen(whatToDo) - 1);
	         printf("chance h %s\n", subbuff);
	         
	         pthread_mutex_lock(&lockSep);
	         
			 separeter(subbuff, ';');
			 int delka = length_p;
			 char pomS[delka][25];
			 for(int i = 0; i < delka; i++)
			 {
			 	memset(&pomS[i], '\0', sizeof(pomS[i]));
			 	strcpy(pomS[i], sepa[i]);
			 }
			 
			 pthread_mutex_unlock(&lockSep);
			 
			 int hotel = 0, houses = 0;
			 
			 pthread_mutex_lock(&lockSep);
	         
			 separeter(hra->upgrady[index], ',');
			 int delka2 = length_p;
			 char pomS2[delka2][20];
			 for(int i = 0; i < delka2; i++)
			 {
			 	memset(&pomS2[i], '\0', sizeof(pomS2[i]));
			 	strcpy(pomS2[i], sepa[i]);
			 }
			 
			 pthread_mutex_unlock(&lockSep);
			 
			 for(int i = 0; i < delka2; i++)
			 {
			 	int tmpInt = atoi(pomS2[i]);
			 	if(tmpInt == 6)
			 	{
			 		hotel++;
				}
				else
				{
					houses += tmpInt;
				}
			 }
			 
			 int cost1 = atoi(pomS[0]);
			 int cost2 = atoi(pomS[1]);
			 hra->penize[index] -= houses * cost1;
			 hra->penize[index] -= hotel * cost2;
			 printf("%s musi zaplatit bance za kazdy svuj hotel(%d penez) a kazdy svuj dum(%d penez).\n", hraci[indexHrace].jmeno, cost2, cost1);
			 printf("Tedy celkove predal bance: %d penez za domy a %d penez za hotely!\n", (houses * cost1), (hotel * cost2));
			 memset(&t, '\0', sizeof(t));
			 sprintf(t, "$game!chance!%s!#\n", whatToDo);
			 broadcastToAllGame(hra, t);
			 //TRANSMISSION!!!
			 //Table.transmission("c!" + whatToDo + "!");
			 //TRANSMISSION!!!
			 break;
	}
}

void setGameStatusFull(char *input, struct Game* hra)
{
	pthread_mutex_lock(&lockSep);
	
	separeter(input, '!');
	int delka = length_p;
	char sepaP[delka][100];
	for(int i = 0; i < delka; i++)
	{
		memset(&sepaP[i], '\0', sizeof(sepaP[i]));
		strcpy(sepaP[i], sepa[i]);
	}
	
	pthread_mutex_unlock(&lockSep);
	
	int pocetHracu = atoi(sepaP[0]);
	printf("%d\n", pocetHracu);
	for(int i =0; i < 4; i++)
	{
		hra->vezeni[i] = 0;
	}
	char pomZakl[pocetHracu + 2][100];
	for(int i = 0; i < pocetHracu + 2; i++)
	{
		memset(&pomZakl[i], '\0', sizeof(pomZakl[i]));
		strcpy(pomZakl[i], sepaP[i]);
	}
	for(int i = 0; i < pocetHracu; i++)
	{
		pthread_mutex_lock(&lockSep);
		
		separeter(pomZakl[i+1], '?');//hracInfo
		char hracInfo[length_p][75];
		for(int j = 0; j < length_p; j++)
		{
			memset(&hracInfo[j], '\0', sizeof(hracInfo[j]));
			strcpy(hracInfo[j], sepa[j]);
		}
		
		pthread_mutex_unlock(&lockSep);
		
		char jmeno[50];
		memset(&jmeno, '\0', sizeof(jmeno));
		strcpy(jmeno, hracInfo[0]);
		printf("jmeno: %s, ", jmeno);
		memset(&hra->jmena[i], '\0', sizeof(hra->jmena[i]));
		strcpy(hra->jmena[i], jmeno);
		int hracMoney = atoi(hracInfo[2]);
		hra->penize[i] = hracMoney;
		printf("penize: %d, ", hra->penize[i]);
		hra->poziceHracu[i] = atoi(hracInfo[1]);
		printf("pozice: %d, ", hra->poziceHracu[i]);
		int pocetBudov = atoi(hracInfo[3]);
		printf("poc. budov: %d\n", pocetBudov);
		memset(&hra->budovy[i], '\0', sizeof(hra->budovy[i]));
		memset(&hra->upgrady[i], '\0', sizeof(hra->upgrady[i]));
		if(pocetBudov != 0)
		{
			strcpy(hra->budovy[i], hracInfo[4]);	
			strcpy(hra->upgrady[i], hracInfo[5]);
		}
	}
	hra->natahu = atoi(pomZakl[pocetHracu + 1]);
	hra->hodStejnych = 0;
	hra->anotherRun = 0;
	hra->changeOfPlayers = 0;
	hra->jailFree = -1;
	printf("index hrace natahu: %d\n", hra->natahu);
}

void generateGameFullStats(char* text, int size, struct Lobby* lob)
{
	char vysledek[200];
	memset(&vysledek, '\0', sizeof(vysledek));
	sprintf(vysledek, "%s%d!", vysledek, lob->pocetHracu);
	int indG = getIndexOfGame(lob->idLobby);
	if(indG == -1)
	{
		printf("Error - generateGameFullStats nenasel hru s idLobby %d!\n", lob->idLobby);
	}
	for(int i = 0; i < lob->pocetHracu; i++)
	{
		pthread_mutex_lock(&lockSep);
		
		separeter(list_games[indG].budovy[i], ',');
		int d = length_p;//Pocet vlastnenych budov
		
		pthread_mutex_unlock(&lockSep);
		
		int indHrace = lob->hraciLobby[i];
		//Jmeno?pozice?penize?pocet vlastnenych budov?indexy budov?indexy pro vylepseni
		sprintf(vysledek, "%s%s?%d?%d?%d?", vysledek, hraci[indHrace].jmeno, list_games[indG].poziceHracu[i], list_games[indG].penize[i], d);
		if(d > 0)
		{
			sprintf(vysledek, "%s%s?%s!", vysledek, list_games[indG].budovy[i], list_games[indG].upgrady[i]);
		}
		else
		{
			strcat(vysledek, "!");
		}
	}
	sprintf(vysledek, "%s%d!", vysledek, list_games[indG].natahu);//Index hrace, ktery je natahu
	//Poslani teto zpravy dale
	strncpy(text, vysledek, size);
}

void shuffleChestCards(struct Game* hra)
{
	//Vytvorit balicek
	for(int i = 0; i < 17; i++)
	{
		hra->chestIndex[i] = i;
	}
	
	//Zamychat balicek
	int poc = randint(30) + 10;
	for(int i = 0; i < poc; i++)
	{
		int x = randint(17);
		int y = randint(17);
		int pom = hra->chestIndex[x];
		hra->chestIndex[x] = hra->chestIndex[y];
		hra->chestIndex[y] = pom;
	}
	printf("Chest zamychana!\n");
}

void shuffleChanceCards(struct Game* hra)
{
	//Vytvorit balicek
	for(int i = 0; i < 16; i++)
	{
		hra->chanceIndex[i] = i;
	}
	
	//Zamychat balicek
	int poc = randint(30) + 10;
	for(int i = 0; i < poc; i++)
	{
		int x = randint(16);
		int y = randint(16);
		int pom = hra->chanceIndex[x];
		hra->chanceIndex[x] = hra->chanceIndex[y];
		hra->chanceIndex[y] = pom;
	}
	printf("Chance zamychana!\n");
}

int takeChestCard(struct Game* hra)
{
	int k = hra->chestIndex[0];
	for(int i = 0; i < 17 - 1; i++)
	{
		hra->chestIndex[i] = hra->chestIndex[i + 1];	
	}
	hra->chestIndex[16] = k;
	return k;
}

int takeChanceCard(struct Game* hra)
{
	int k = hra->chanceIndex[0];
	for(int i = 0; i < 16 - 1; i++)
	{
		hra->chanceIndex[i] = hra->chanceIndex[i + 1];	
	}
	hra->chanceIndex[15] = k;
	return k;
}

int toString(int index)
{
	printf("nazev: %s\n", game_board[index].nazev);
	printf("cena: %d\n", game_board[index].cena);
	printf("cenaUpgradu: %d\n", game_board[index].cenaUpgradu);
	printf("typ: %d\n", game_board[index].typPozemku);
	printf("zisky: ");
	for(int m = 0; m < 6; m++)
	{
		printf("%d,", game_board[index].zisky[m]);
	}
	printf("\n\n");
}

int uvolniGameBoard()
{
	free(game_board);
}

/**
int main(void)
{
	initHraci();
	initGames();
	setupGameBoard();
	
	addHrac(10, "Michal");
	addHrac(15, "Jirka");
	addHrac(20, "Petr");
	addHrac(25, "Zdenek");
	
	struct Lobby lobby;
	lobby.hraciLobby[0] = 0;
	lobby.hraciLobby[1] = 1;
	lobby.hraciLobby[2] = 2;
	lobby.hraciLobby[3] = 3;
	lobby.pocetHracu = 4;
	strcpy(lobby.lobbyName, "default");
	int bi = addGame(lobby.idLobby);
	
	shuffleChanceCards(&list_games[bi]);
	shuffleChestCards(&list_games[bi]);
	setGameStatusFull("4!Michal?0?1500?0?0?0!Jirka?0?1500?0?0?0!Petr?0?1500?0?0?0!Zdenek?0?1500?0?0?0!0!", &list_games[bi]);
	
	printf("natahu: %d\n", list_games[bi].natahu);
	for(int i = 0; i < 4; i++)
	{
		printf("%d penize: %d\n", i, list_games[bi].penize[i]);
		printf("%d pozice: %d\n", i, list_games[bi].poziceHracu[i]);
		printf("%d vezeni: %d\n", i, list_games[bi].vezeni[i]);
		printf("%d budovy: %s\n", i, list_games[bi].budovy[i]);
		printf("%d upgrady: %s\n", i, list_games[bi].upgrady[i]);
		printf("---\n");
	}
	printf("CHEST\n");
	for(int i = 0; i < 17; i++)
	{
		printf("%d, ", list_games[bi].chestIndex[i]);
	}
	printf("\n");
	printf("CHANCE\n");
	for(int i = 0; i < 16; i++)
	{
		printf("%d, ", list_games[bi].chanceIndex[i]);
	}
	printf("\n");
	
	takeChanceCard(&list_games[bi]);
	takeChanceCard(&list_games[bi]);
	takeChestCard(&list_games[bi]);
	takeChestCard(&list_games[bi]);
	printf("CHEST\n");
	for(int i = 0; i < 17; i++)
	{
		printf("%d, ", list_games[bi].chestIndex[i]);
	}
	printf("\n");
	printf("CHANCE\n");
	for(int i = 0; i < 16; i++)
	{
		printf("%d, ", list_games[bi].chanceIndex[i]);
	}
	printf("\n");
	
	for(int j = 0; j < 5; j++)
	{
		for(int i = 0; i < 4; i++)
		{
			if(list_games[bi].penize > 0)
			{
				gameRules(i, lobby.hraciLobby[i], &list_games[bi]);
				printf("\n");
				if(list_games[bi].changeOfPlayers)
				{
					i--;
					list_games[bi].changeOfPlayers = 1;
					list_games[bi].anotherRun = 0;
				}
				if(list_games[bi].anotherRun == 1)
				{
					i--;
					list_games[bi].anotherRun = 0;
				}	
			}
		}	
	}
	
	generateGameFullStats(&lobby);
	
	
	uvolniHrace();	
	uvolniGames();
	uvolniGameBoard();
}
*/


