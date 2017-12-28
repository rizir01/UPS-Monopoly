#ifndef POZEMEK_H
#define POZEMEK_H
struct Pozemek
{
	//Nazev bloku
	char nazev[50];
	
	//O jaky typ bloku se jedna
	int typPozemku;
	
	//Kolik pozmek stoji, pokud ma cenu
	int cena;
	
	//Kolik stoji upgrady na vyssi uroven
	int cenaUpgradu;
	
	//Cena za stoupnuti na pozemek
	int zisky[6];
	
	//Do jake kategorie patri a kolik je
	//v dane kategorii pozemku
	int kategorie[2];
};
#endif
