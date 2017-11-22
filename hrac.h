#ifndef HRAC_H
#define HRAC_H
struct Hrac
{
	//Cislo na kterem soketu lze s klientem komunikovat
	int client_socket;
	
	//Jmeno hrace
	char jmeno[50];
	
	//Promena, ktera definuje v jakem stavu(oblasti) se hrac nachazi, pr. menu s lobby, lobby, hra atd.
	//A pak take v jakem stavu primo ve hre, pr. hazeni kostkou, nakup nemovitosti, aukce atd.
	//1=menu lobby,
	int stav;
	
	//ID hrace prirazene serverem
	int id;
	
	//Jestli dany hrac byl inicializovan
	int init;//bool
};
#endif
