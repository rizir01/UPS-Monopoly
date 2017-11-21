#ifndef HRAC_H
#define HRAC_H
struct Hrac
{
	int client_socket;
	char jmeno[50];
	//Promena, ktera definuje v jakem stavu(oblasti) se hrac nachazi, pr. menu s lobby, lobby, hra atd.
	//A pak take v jakem stavu primo ve hre, pr. hazeni kostkou, nakup nemovitosti, aukce atd.
	//1=menu lobby,
	int stav;
	int id;
	int init;//bool
};
#endif
