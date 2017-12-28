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
#include "tools.c"

struct Pozemek makePozemek(char *nazevP, char typ, int cenaP, int cenaU, char* upgrady, int kat, int katPoc);

struct Pozemek makePozemek(char *nazevP, char typ, int cenaP, int cenaU, char* upgrady, int kat, int katPoc)
{
	struct Pozemek poz;
	char naz[50];
	strcpy(naz, nazevP);
	strcpy(poz.nazev, naz);
	char up[75];
	strcpy(up, upgrady);
	poz.cena = 0;
	poz.cenaUpgradu = 0;
	poz.kategorie[0] = -1;
	poz.kategorie[2] = -1;
	//poz.zisky = {0,0,0,0,0,0};
	memset(&poz.zisky, 0, sizeof(int) * 6);
	switch(typ)
	{
		case 'P':poz.cena = cenaP;
				 poz.cenaUpgradu = cenaU;
				 poz.typPozemku = 1;//pozemek
				 poz.kategorie[0] = kat;
				 poz.kategorie[1] = katPoc;
				 separeter(up, ';');
				 for(int i = 0; i < 6; i++)// nebo length_p
				 {
				 	poz.zisky[i] = atoi(sepa[i]);
				 	//printf("%d\n", poz.zisky[i]);
				 }	 
				 break;
		case 'R':poz.cena = 200;
				 poz.typPozemku = 2;//pozemek
				 memset(&poz.zisky, 0, sizeof(int) * 6);
				 poz.zisky[0] = 25;
				 poz.zisky[1] = 50;
				 poz.zisky[2] = 100;
				 poz.zisky[3] = 200;
				 break;//zeleznice
		case 'U':poz.cena = 150;
				 poz.typPozemku = 3;//Utility
				 break;
		case 'C':poz.typPozemku = 4;//community chest
				 break;
		case 'H':poz.typPozemku = 5;//sance
		         break;
		case 'T':poz.cena = cenaP;
				 poz.typPozemku = 6;//Poplatky
		         break;
		case 'S':poz.typPozemku = 7;//start
				 break;
		case 'J':poz.typPozemku = 8;//vezeni
				 break;
		case 'L':poz.typPozemku = 9;//Parkoviste
				 break;
		case 'G':poz.typPozemku = 10;//Jit do vezeni
				 break;
	}
	return poz;
}
