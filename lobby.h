#ifndef LOBBY_H
#define LOBBY_H
struct Lobby
{
	//Pole indexu hracu do pole
	//na serveru pro ziskani informaci
	//o hraci
	int hraciLobby[4];
	
	//Nazev lobby
	char lobbyName[100];
	
	//Pocet hracu v lobby
	int pocetHracu;
	
	//ID lobby
	int idLobby;
};
#endif
