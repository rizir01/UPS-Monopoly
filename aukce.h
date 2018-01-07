#ifndef AUKCE_H
#define AUKCE_H
struct Aukce
{	
	//Boolean, jestli ma neustale kolovat aukce
	//Mozna jiz nebude treba
	//0-neni aukce, 1-aukce probiha, 2-provedeni post akci
	//pro uzivatele, ktery aukce puvodne spustil
	int auction;
	
	//Ktery hrac ma pravo pristupovat k prihazovani
	int aukceNatahu;
	
	//Index pozemku na mape, o ktery se momentalne hraje
	int pozice;
	
	//Ceny za pozmeky, tak ja je nabizi hraci
	int auctionPrice[4];
	
	//Pocet ucastnenych hracu na aukci
	int pocetHrajicich;	
	
	//Kolik lidi ukoncilo ucast na aukci za pozemek
	int peopleDone;
	
	//Nejvyssi cena za pozemek v aukci
	int max;
};
#endif
