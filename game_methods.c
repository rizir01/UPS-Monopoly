#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include "pozemek.h"
#include "game.h"
#include "lobby.h"
#include "game_array.c"
#include "pozemek_methods.c"
//#include "hrac_methods.c"

//Funkce
int setupGameBoard();

void shuffleChestCards(struct Game* hra);

int takeChestCard(struct Game* hra);

void shuffleChanceCards(struct Game* hra);

int takeChanceCard(struct Game* hra);

int isPRUOwned(int pozice, struct Game* hra);

int getIndexForHousing(int index, int pozice, struct Game* hra);

void setGameStatusFull(char *input, struct Game* hra);

char* generateGameFullStats(struct Lobby* lob);

void gameRules(int index, int indexHrace, struct Game* hra);

void makeAction(int index,int  indexHrace, int pozice, struct Game* hra);

void makeActionJail(int index, int indexHrace, int pozice, struct Game* hra);

void makeActionChance(int index, int indexHrace, int pozice, struct Game* hra);

void makeActionChest(int index, int indexHrace, int pozice, struct Game* hra);

void makeActionPRU(int index, int indexHrace, int pozice, struct Game* hra);

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
			if(sepa[0][0] == 'P')
			{
				game_board[ind] = makePozemek(sepa[1], sepa[0][0],atoi(sepa[2]), atoi(sepa[4]), sepa[3], atoi(sepa[5]), atoi(sepa[6]));	
			}
			else if(sepa[0][0] == 'R' || sepa[0][0] == 'U')
			{
				game_board[ind] = makePozemek(sepa[1], sepa[0][0], 0, 0, "", 0, 0);
			}
			else if(sepa[0][0] == 'C')
			{
				game_board[ind] = makePozemek("Community chest", sepa[0][0], 0, 0, "", 0, 0);
			}
			else if(sepa[0][0] == 'H')
			{
				game_board[ind] = makePozemek("Chance", sepa[0][0], 0, 0, "", 0, 0);
			}
			else if(sepa[0][0] == 'T')
			{
				game_board[ind] = makePozemek("Tax", sepa[0][0], atoi(sepa[1]), 0, "", 0, 0);
			}
			else if(sepa[0][0] == 'S')
			{
				game_board[ind] = makePozemek("Start", sepa[0][0], 0, 0, "", 0, 0);
			}
			else if(sepa[0][0] == 'J')
			{
				game_board[ind] = makePozemek("Jail", sepa[0][0], 0, 0, "", 0, 0);
			}
			else if(sepa[0][0] == 'L')
			{
				game_board[ind] = makePozemek("Parking lot", sepa[0][0], 0, 0, "", 0, 0);
			}
			else if(sepa[0][0] == 'G')
			{
				game_board[ind] = makePozemek("Go to jail", sepa[0][0], 0, 0, "", 0, 0);
			}
			pthread_mutex_unlock(&lockSep);
			int z = toString(ind);
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
void gameRules(int index, int indexHrace, struct Game* hra)
{
	//printf("Hrac " + hraci[index].getName() + " je na tahu!\n");
	//System.out.println("Nachazi se na pozici: " + hraci[index].getPosition());
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
			int hod = kostka1 + kostka2;
			printf("%s hodil %d %d\n", hraci[indexHrace].jmeno, kostka1, kostka2);
			//TRANSMISSION!!!
			//transmission("s!" + kostka1 + "!" + kostka2 + "!");
			//TRANSMISSION!!!
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
			makeAction(index, indexHrace, hra->poziceHracu[index], hra);
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
			makeAction(index, indexHrace, hra->poziceHracu[index], hra);
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
			//TRANSMISSION!!!
			//transmission("l!" + index + "!");
			//TRANSMISSION!!!
		}
		if(hra->anotherRun != 1)//!anotherRun - jestlize dany hrac nehraju znovu, tak ..
		{
			//TRANSMISSION!!!
			//transmission("e!");
			//TRANSMISSION!!!				
		}
		else // jestli-ze dany hrac hazi znova
		{
			//TRANSMISSION!!!
			//transmission("n!");
			//TRANSMISSION!!!
		}
		printf("post %s, penize: %d, pozice: %d, misto: %s\n", hraci[indexHrace].jmeno, hra->penize[index], hra->poziceHracu[index], game_board[hra->poziceHracu[index]].nazev);
	}
}

void makeAction(int index, int  indexHrace, int pozice, struct Game* hra)
{
	int typ = game_board[pozice].typPozemku;
	int pay;
	switch(typ)
	{
		case 1:makeActionPRU(index, indexHrace, pozice, hra);
			   break;
		case 2:makeActionPRU(index, indexHrace, pozice, hra);
		       break;
		case 3:makeActionPRU(index, indexHrace, pozice, hra);
			   break;
		case 4:makeActionChest(index, indexHrace, pozice, hra);
			   break;
	    case 5:makeActionChance(index, indexHrace, pozice, hra);
			   break;
	    case 6:pay = game_board[pozice].cena;
			   hra->penize[index] -= pay;
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
				//transmission JAIL j!i!
			    break;
	}
}

void makeActionJail(int index, int indexHrace, int pozice, struct Game* hra)
{
	if(hra->jailFree == index)
	{
		printf("%s pouzil kartu, aby se dostal z vezeni.\n", hraci[indexHrace].jmeno);
		printf("%s se dostal z vezeni a haze koustkou.\n", hraci[indexHrace].jmeno);
		hra->jailFree = -1;
		hra->anotherRun = 1;
	}
	else if(hra->vezeni[index] == 1)
	{
		printf("%s hazi kostky, aby se dostal z vezeni.\n", hraci[indexHrace].jmeno);
		int kostka1 = randint(6) + 1;
		int kostka2 = randint(6) + 1;
		printf("%s hodil %d %d.\n", hraci[indexHrace].jmeno, kostka1, kostka2);
		if(kostka1 == kostka2)
		{
			hra->vezeni[index] = 0;
			printf("%s se dostal z vezeni a haze koustkou.\n", hraci[indexHrace].jmeno);
			hra->anotherRun = 1;
		}
		else
		{
			printf("%s je stale ve vezeni.\n", hraci[indexHrace].jmeno);
		}
	}
	/**
	else
	{
		System.out.println(hrac.getName() + " je ve vezeni");
		hrac.jail = true;
	}
	*/
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

void makeActionPRU(int index, int indexHrace, int pozice, struct Game* hra)
{
	int typPoz = game_board[pozice].typPozemku;
	int cenaPoz = game_board[pozice].cena;
	char nazevPoz[25];
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
		printf("%s %s stoji 200.\n", nazevPoz, game_board[pozice].nazev);
		printf("<k> - Koupit utility!\n");
		printf("<a> - Aukcni sin!\n");
		int done = 1;
		while(done == 1)
		{
			//Cekam na zpravu od Klienta
			char in[10];
			memset(&in, '\0', sizeof(in));
			printf("Zadejte hodnotu: ");
   			scanf("%s", in);
			//char in[10] = "k\n";
			if(strlen(in) != 0)//!in.equals("") - Prijde prazdny retezec, opakuj cely proces
			{
				if(in[0] == 'k')
				{
					if(hra->penize[index] - cenaPoz < 0)
					{
						printf("Nelze provest koupi! - Nedostatek penez.\n");
					}
					else
					{
						hra->penize[index] -= cenaPoz;
						sprintf(hra->budovy[index], "%s%d,", hra->budovy[index], pozice);
						sprintf(hra->upgrady[index], "%s0,", hra->upgrady[index]);
						printf("%s koupil pozemek %s za %d.\n", hraci[indexHrace].jmeno, game_board[pozice].nazev, cenaPoz);
						done = 0;
						//TRANSMISSION!!!
						//Table.transmission("b!" + index + "!" + hrac.getPosition() + "!");
						//Table.transmission("p!" + index + "!" + 200 + "!");
						//TRANSMISSION!!!
					}
				}
				else if(in[0] == 'a')
				{
					int auction = 1;
					int auctionPrice[4];
					int pocetHrajicich = 0;
					for(int i = 0; i < 4; i++)//Nastaveni nehrajicim hracum
					{
						if(hra->penize[index] <= 0)
						{
							auctionPrice[i] = -1;
						}
						else
						{
							auctionPrice[i] = 0;
							pocetHrajicich++;
						}
					}
					int peopleDone = 0;
					int max = 0;
					while(auction == 1)
					{
						for(int i = 0; i < 4; i++)
						{
							if(auctionPrice[i] != -1 && peopleDone < pocetHrajicich - 1)
							{
								printf("\nHrac %s ma %d.\n", hra->jmena[i], hra->penize[i]);
								printf("Aktualni nejvyssi cena strediska je %d penez.\n", max);
								printf("%s nabizi za pozemek %d penez.\n", hra->jmena[i], auctionPrice[i]);
								printf("Pokud chcete prihodit, zadejte hodnotu , kterou jste ochotni za pozemek zaplatit.\n");
								printf("Pokud uz nechcete prihodit, zadejte do koznole <K>\n");
								int rightInput = 1;
								while(rightInput == 1)
								{
									//Opet informace od klienta
									memset(&in, '\0', sizeof(in));
									printf("Zadejte hodnotu: ");
   									scanf("%s", in);
									//strcpy(in, "k");
									if(in[0] == 'k')
									{
										auctionPrice[i] = -1;
										peopleDone++;
										//TRANSMISSION!!!
										//Table.transmission("a!k!");
										//TRANSMISSION!!!
										rightInput = 0;
									}
									else
									{
										int hod = atoi(in);
										if(hod > hra->penize[index])
										{
											printf("Nelze provest prihozeni! - Nedostatek penez\n");
											//TRANSMISSION!!!
											//Table.transmission("a!f!");
											//TRANSMISSION!!!
										}
										else
										{
											if(hod <= max)
											{
												printf("Prihozeni je mensi nez max: %d, prihodte vice nebo zruste prihazovani!\n", max);
												//TRANSMISSION!!!
												//.transmission("a!f!");
												//TRANSMISSION!!!
											}
											else
											{
												max = hod;
												auctionPrice[i] = hod;
												//TRANSMISSION!!!
												//Table.transmission("a!a!" + max + "!");
												//TRANSMISSION!!!
												rightInput = 0;																									
											}												
										}
									}																			
								}
							}
							//TRANSMISSION!!!
							//Table.transmission("a!n!");
							//TRANSMISSION!!!
						}
						if(peopleDone >= pocetHrajicich - 1)
						{
							auction = 0;
						}
					}
					int index3 = -1;
					for(int i = 0; i < 4; i++)
					{
						if(auctionPrice[i] != -1)
						{
							index3 = i;
						}
					}
					printf("%s ziskal stredisko z aukce za %d.\n", hra->jmena[index3], auctionPrice[index3]);
					hra->penize[index3] -= auctionPrice[index3];
					sprintf(hra->budovy[index3], "%s%d,", hra->budovy[index3], pozice);
					sprintf(hra->upgrady[index3], "%s0,", hra->upgrady[index3]);
					//TRANSMISSION!!!
					//Table.transmission("p!" + index + "!" + auctionPrice[index] + "!");
					//Table.transmission("b!"+ index + "!" + hrac.getPosition() + "!");
					//TRANSMISSION!!!
					done = 0;
				}
				//TRANSMISSION!!!
				//Table.transmission("a!e!");
				//TRANSMISSION!!!
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
}

void makeActionPropertyOwned(int index, int indexHrace, int pozice, struct Game* hra)
{
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
	//zamknout
	separeter(hra->upgrady[index], ',');
	char pom3[100];
	memset(&pom3, '\0', sizeof(pom3));
	strcpy(pom3, sepa[index5]);
	//odemknout
	pthread_mutex_unlock(&lockSep);
	int house = atoi(pom3);
	
	if(house == 6)//Pokud na danem pozmeku je jiz hotel
	{
		int cast2 = game_board[pozice].zisky[5];
		printf("%s zaplati %s %d.\n", hraci[indexHrace].jmeno, hra->jmena[index4], cast2);
		hra->penize[index4] += cast2;
		hra->penize[index] -= cast2;
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
		//TRANSMISSION!!!
		//Table.transmission("p!"+ ind + "!" + this.getHousesAndHotelRent()[0] * multi + "!");
		//Table.transmission("g!"+ index + "!" + this.getHousesAndHotelRent()[0] * multi + "!");
		//TRANSMISSION!!!
	}
}

void makeActionRailroadOwned(int index, int indexHrace, int pozice, struct Game* hra)
{
	int index4 = isPRUOwned(pozice, hra);//index hrace, ktery vlastni tento pozemek
	printf("Pozemek vlastni %s.\n", hra->jmena[index4]);//DODELAT ZISKANI NAZVU HRACE PODLE INDEXU VE HRE
	
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
	//TRANSMISSION!!!
	//Table.transmission("p!"+ ind + "!" + rent[suma - 1] + "!");
	//Table.transmission("g!"+ index + "!" + rent[suma - 1] + "!");
	//TRANSMISSION!!!
}

void makeActionUtilityOwned(int index, int indexHrace, int pozice, struct Game* hra)
{
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
	//TRANSMISSION!!!
	//Table.transmission("p!"+ ind + "!" + (kostka1 + kostka2)*multi[count - 1] + "!");
	//Table.transmission("g!"+ index + "!" + (kostka1 + kostka2)*multi[count - 1] + "!");
	//TRANSMISSION!!!
}


void makeActionChest(int index, int indexHrace, int pozice, struct Game* hra)
{
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
	//TRANSMISSION!!!
	//Table.transmission("d!" + whatToDo + "!");
	//TRANSMISSION!!!
}

void makeActionChance(int index, int indexHrace, int pozice, struct Game* hra)
{
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
			 //TRANSMISSION!!!
			 //Table.transmission("c!" + whatToDo + "!");
			 //TRANSMISSION!!!
		     break;
	case '-':memset(&subbuff, '\0', sizeof(subbuff));
	         memcpy(subbuff, &whatToDo[1], strlen(whatToDo) - 1);
	         printf("chance - %s\n", subbuff);
			 hra->penize[index] -= atoi(subbuff);
			 printf("%s ztratil %s penez.\n", hraci[indexHrace].jmeno, subbuff);
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

/**
 * POZOR - je zde maloc, ktery se nevynuluje, je treba ho
 * posleze uvolnit
 *
 */
char *generateGameFullStats(struct Lobby* lob)
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
	sprintf(vysledek, "%s%d!\n", vysledek, list_games[indG].natahu);//Index hrace, ktery je natahu
	//Poslani teto zpravy dale
	printf("%s\n", vysledek);
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


