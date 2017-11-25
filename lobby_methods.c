#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include "lobby.h"

//deklarace funkci

int addLobby(char *nazev);

int removeLobby(int index);

int addPlayer(int indexHrac, int indexLobby);

int removePlayer(int indexHrac, int indexLobby);

struct Lobby* boostLobby();

struct Lobby* reduceLobby(int index);

int initLobby();

int uvolniLobby();

//globalni promene
struct Lobby *lobbies;
int length_lobbies = 0;
int id_plus_lobby = 1;

//MUTEX
pthread_mutex_t lockLobby = PTHREAD_MUTEX_INITIALIZER;

int initLobby()
{
	pthread_mutex_lock(&lockLobby);
	
	lobbies = (struct Lobby*)malloc(sizeof(struct Lobby) * (length_lobbies + 1));
	memset(&lobbies[0].hraciLobby, -1, sizeof(int) * 4);
	strcpy(lobbies[0].lobbyName, "default");
	lobbies[0].pocetHracu = 0;
	lobbies[0].idLobby = id_plus_lobby++;
	length_lobbies += 1;
	
	pthread_mutex_unlock(&lockLobby);
}

int addLobby(char *nazev)
{
	pthread_mutex_lock(&lockLobby);
	
	lobbies = boostLobby();
	memset(&lobbies[length_lobbies - 1].hraciLobby, -1, sizeof(int) * 4);
	memset(&lobbies[length_lobbies - 1].lobbyName, 0, sizeof(lobbies[length_lobbies - 1].lobbyName));
	memcpy(lobbies[length_lobbies - 1].lobbyName, nazev, sizeof(nazev));
	
	pthread_mutex_unlock(&lockLobby);
	return 0;			
}

int removeLobby(int index)
{
	if(index < 0 || index >= length_lobbies)
	{
		printf("Zadany index %d je mimo ramec lobbies!\n", index);
		return 1;
	}
	pthread_mutex_lock(&lockLobby);
	
	lobbies = reduceLobby(index);
	
	pthread_mutex_unlock(&lockLobby);
	return 0;
}

struct Lobby* boostLobby()
{
	struct Lobby *moreLobbies;
	moreLobbies = (struct Lobby*)malloc(sizeof(struct Lobby) * (length_lobbies + 1));
	
	for(int i = 0; i < length_lobbies; i++)
	{
		memcpy(moreLobbies[i].hraciLobby, lobbies[i].hraciLobby, sizeof(lobbies[i].hraciLobby));
		strcpy(moreLobbies[i].lobbyName, lobbies[i].lobbyName);
		moreLobbies[i].idLobby = lobbies[i].idLobby;
		moreLobbies[i].pocetHracu = lobbies[i].pocetHracu;
		moreLobbies[i].idLobby = lobbies[i].idLobby;
	}
	
	moreLobbies[length_lobbies].pocetHracu = 0;
	moreLobbies[length_lobbies].idLobby = id_plus_lobby++;
	
	free(lobbies);
	length_lobbies += 1;
	return moreLobbies;
}

struct Lobby* reduceLobby(int index)
{
	struct Lobby *lessLobbies;
	lessLobbies = (struct Lobby*)malloc(sizeof(struct Lobby) * (length_lobbies - 1));
	
	int j = 0;
	for(int i = 0; i < length_lobbies; i++)
	{
		if(i != index)
		{
			memcpy(lessLobbies[j].hraciLobby, lobbies[i].hraciLobby, sizeof(lobbies[i].hraciLobby));
			strcpy(lessLobbies[j].lobbyName, lobbies[i].lobbyName);
			lessLobbies[j].idLobby = lobbies[i].idLobby;
			lessLobbies[j].pocetHracu = lobbies[i].pocetHracu;
			lessLobbies[j].idLobby = lobbies[i].idLobby;
			j++;	
		}
	}
	
	free(lobbies);
	length_lobbies -= 1;
	return lessLobbies;
}

int addPlayer(int indexHrac, int indexLobby)
{
	if(indexHrac < 0 || indexHrac >= length_hraci)
	{
		printf("[ADD]Index hrace %d neni ve stanovenych mezich!", indexHrac);
		return 1;
	}
	if(indexLobby < 0 || indexLobby >= length_lobbies)
	{
		printf("[ADD]Index lobby %d neni ve stanovenych mezich!", indexHrac);
		return 1;
	}
	int ind = -1;
	if(hraci[indexHrac].init == 0)
	{
		printf("[ADD]Na zadanem indexu %d neni zadny hrac!", indexHrac);
		return 2;
	}
	pthread_mutex_lock(&lockLobby);
	
	int nasel = 0;//bool
	for(int i = 0; i < 4; i++)
	{
		if(lobbies[indexLobby].hraciLobby[i] == -1)
		{
			nasel = 1;
			lobbies[indexLobby].hraciLobby[i] = indexHrac;	
			break;
		}
	}
	if(nasel == 0)
	{
		pthread_mutex_unlock(&lockLobby);
		printf("[ADD]Neni jiz misto v lobby s indexem %d!\n", indexLobby);
		return 3;
	}
	lobbies[indexLobby].pocetHracu += 1;
	
	pthread_mutex_unlock(&lockLobby);
	return 0;
}

int removePlayer(int indexHrac, int indexLobby)
{
	if(indexHrac < 0 || indexHrac >= length_hraci)
	{
		printf("[RM]Index hrace %d neni ve stanovenych mezich!", indexHrac);
		return 1;
	}
	if(indexLobby < 0 || indexLobby >= length_lobbies)
	{
		printf("[RM]Index lobby %d neni ve stanovenych mezich!", indexHrac);
		return 1;
	}
	int ind = -1;
	if(hraci[indexHrac].init == 0)
	{
		printf("[RM]Na zadanem indexu %d neni zadny hrac!", indexHrac);
		return 2;
	}
	pthread_mutex_lock(&lockLobby);
	
	int nasel = 0;//bool
	for(int i = 0; i < 4; i++)
	{
		if(lobbies[indexLobby].hraciLobby[i] == indexHrac)
		{
			nasel = 1;
			lobbies[indexLobby].hraciLobby[i] = -1;	
			break;
		}
	}
	if(nasel == 0)
	{
		pthread_mutex_unlock(&lockLobby);
		printf("[RM]Nenasel se hrac v lobby %d s indexem %d!\n", indexLobby, indexHrac);
		return 3;
	}
	lobbies[indexLobby].pocetHracu -= 1;
	
	pthread_mutex_unlock(&lockLobby);
	return 0;
}

int uvolniLobby()
{
	free(lobbies);
}


