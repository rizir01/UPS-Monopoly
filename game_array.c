#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include "game.h"

//Globalni promene
struct Game *list_games;
int length_list_games = 0;

//MUTEX
pthread_mutex_t lockGame = PTHREAD_MUTEX_INITIALIZER;

int initGames()
{
	pthread_mutex_lock(&lockGame);
	
	list_games = (struct Game*)malloc(sizeof(struct Game) * (length_list_games));
	
	pthread_mutex_unlock(&lockGame);
}

struct Game* boostGames()
{
	struct Game *moreGames;
	moreGames = (struct Game*)malloc(sizeof(struct Game) * (length_list_games + 1));
	
	for(int i = 0; i < length_list_games; i++)
	{
		//pole
		memcpy(moreGames[i].poziceHracu, list_games[i].poziceHracu, sizeof(list_games[i].poziceHracu));
		memcpy(moreGames[i].penize, list_games[i].penize, sizeof(list_games[i].penize));
		memcpy(moreGames[i].vezeni, list_games[i].vezeni, sizeof(list_games[i].vezeni));
		memcpy(moreGames[i].chestIndex, list_games[i].chestIndex, sizeof(list_games[i].chestIndex));
		memcpy(moreGames[i].chanceIndex, list_games[i].chanceIndex, sizeof(list_games[i].chanceIndex));
		//retezce
		for(int j = 0; j < 4; j++)
		{
			strcpy(moreGames[i].jmena[j], list_games[i].jmena[j]);
			strcpy(moreGames[i].budovy[j], list_games[i].budovy[j]);
			strcpy(moreGames[i].upgrady[j], list_games[i].upgrady[j]);
		}		
		//promene
		moreGames[i].idLobby = list_games[i].idLobby;
		moreGames[i].natahu = list_games[i].natahu;
		moreGames[i].hodStejnych = list_games[i].hodStejnych;
		moreGames[i].anotherRun = list_games[i].anotherRun;
		moreGames[i].changeOfPlayers = list_games[i].changeOfPlayers;
		moreGames[i].jailFree = list_games[i].jailFree;
	}
	
	free(list_games);
	length_list_games += 1;
	return moreGames;
}

struct Game* reduceGames(int index)
{
	struct Game *lessGames;
	lessGames = (struct Game*)malloc(sizeof(struct Game) * (length_list_games - 1));
	
	int j = 0;
	for(int i = 0; i < length_list_games; i++)
	{
		if(i != index)
		{
			//pole
			memcpy(lessGames[i].poziceHracu, list_games[i].poziceHracu, sizeof(list_games[i].poziceHracu));
			memcpy(lessGames[i].penize, list_games[i].penize, sizeof(list_games[i].penize));
			memcpy(lessGames[i].vezeni, list_games[i].vezeni, sizeof(list_games[i].vezeni));
			memcpy(lessGames[i].chestIndex, list_games[i].chestIndex, sizeof(list_games[i].chestIndex));
			memcpy(lessGames[i].chanceIndex, list_games[i].chanceIndex, sizeof(list_games[i].chanceIndex));
			//retezce
			for(int m = 0; m < 4; m++)
			{
				strcpy(lessGames[i].jmena[m], list_games[i].jmena[m]);
				strcpy(lessGames[i].budovy[m], list_games[i].budovy[m]);
				strcpy(lessGames[i].upgrady[m], list_games[i].upgrady[m]);
			}		
			//promene
			lessGames[i].idLobby = list_games[i].idLobby;
			lessGames[i].natahu = list_games[i].natahu;
			lessGames[i].hodStejnych = list_games[i].hodStejnych;
			lessGames[i].anotherRun = list_games[i].anotherRun;
			lessGames[i].changeOfPlayers = list_games[i].changeOfPlayers;
			lessGames[i].jailFree = list_games[i].jailFree;
			j++;	
		}
	}
	
	free(list_games);
	length_list_games -= 1;
	return lessGames;	
} 

int addGame(int idLob)
{
	for(int i = 0; i < length_list_games; i++)
	{
		if(list_games[i].idLobby == idLob)
		{
			printf("Prdani nove hry nelze provest, jelikoz hra s id lobby %d jiz existuje!\n", idLob);
			return -1;
		}
	}
	int index = length_list_games;
	
	pthread_mutex_lock(&lockGame);
	
	list_games = boostGames();
	list_games[index].idLobby = idLob;
	
	pthread_mutex_unlock(&lockGame);
	return index;
}

int removeGame(int index)
{
	if(index < 0 || index >= length_list_games)
	{
		printf("Zadany index %d je mimo ramec seznamu her!\n", index);
		return 1;
	}
	pthread_mutex_lock(&lockGame);
	
	list_games = reduceGames(index);
	
	pthread_mutex_unlock(&lockGame);
	return 0;
}

int getIndexOfGame(int idLob)
{
	for(int i = 0; i < length_list_games; i++)
	{
		if(list_games[i].idLobby = idLob)
		{
			return i;
		}
	}
	return -1;
}

int uvolniGames()
{
	free(list_games);
}

