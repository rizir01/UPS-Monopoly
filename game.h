#ifndef GAME_H
#define GAME_H
#include "aukce.h"
struct Game
{
	//Ke ktere lobby tato konkretni hra patri
	int idLobby;
	
	//Kdo je momentalne natahu
	int natahu;
	
	//Kolikrat dany hrac hodil zasebou stejne cisla
	//na kostkach
	int hodStejnych;
	
	//Jestli ma dany hrac znovu hrat
	int anotherRun;
	
	//Jestli prohral hrac, tak se ma zmenit index hrace
	int changeOfPlayers;
	
	//Index hrace ktery ma "okamzite z vezeni"
	//MUSI SE UPRAVIT, JINAK DOJDE K PREPSANI INDEXU, MUZE JICH TOTIZ BYT VICE
	int jailFree;
	
	//Z duvody problematiky zapouzdreni a ziskani nazvu
	//konkretniho hrace je zde opet seznam jmen hracu
	char jmena[4][50];
	
	//Posledni hod kostkami v normalnim rezimu,
	//tedy pouze hod kostkami o posun, ne na vezeni
	//nebo na udrzbu atd.
	//specialni tvar pro funkci - gameRulesPost
	int poslHod[2];
	
	//Pozice kazdeho hrace na mape
	int poziceHracu[4];
	
	//Jestli nektery hrac neni aktivni,
	//Jelikoz spadl z pripojeni a ceka se ze se pripojit
	int skipHraci[4];
	
	//Kolik kdo ma penez
	int penize[4];
	
	//Index hrace ktery je ve vezeni
	int vezeni[4];
	
	//Kolikrat dany hrac uz musel hazet na vezeni,
	//pokud tato hodnota dosahne 5x, tak hrac je propusten
	//z vezeni a muze dale pokracovat, protoze "luck" :D
	int vezeniLuck[4];
	
	//Zamychany balicek s kartami na
	//Comunity chest
	int chestIndex[17];
	
	//Zamychany balicek s kartami na
	//Chance
	int chanceIndex[16];
	
	//Pole, ktere v prvni bloku ma seznam vsech vlastnenych polozek
	//daneho hrace a v druhem bloku pocet upgradu daneho pozemku, pokud
	//jsou upgrady pritomny
	char budovy[4][100];
	char upgrady[4][100];
	
	struct Aukce aukce;
};
#endif
