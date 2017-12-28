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
#include "pozemek_methods.c"

//Funkce
int setupGameBoard();

int uvolniGameBoard();

int toString(int index);

//Globalni promene
struct Pozemek *game_board;
int length_game_board = 0;

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

int main(void)
{
	setupGameBoard();
	uvolniGameBoard();
}


