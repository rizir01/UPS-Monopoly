#ifndef LOBBY_H
#define LOBBY_H
struct Lobby
{
	//Pole indexu hracu do pole
	//na serveru pro ziskani informaci
	//o hraci
	int hraciLobby[4];
	
	//Pole hracu, ktere predstavuje
	//Jestli je ten ktery hrac jiz
	//pripraven hrat hru
	int hraciReady[4];
	
	//Nazev lobby
	char lobbyName[100];
	
	//Pocet hracu v lobby
	int pocetHracu;
	
	//Pokud jsou hraci ve hre, tak lobby
	//se musi uzamknout
	//0-unlocked, 1=locked
	int isLocked;
	
	//ID lobby
	int idLobby;
};
#endif
