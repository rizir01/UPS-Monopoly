#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>

//Pomocne pro random genereator
int startRand = 1;

//Separator pomoc. promene
char sepa[25][100];
int length_p;
pthread_mutex_t lockSep = PTHREAD_MUTEX_INITIALIZER;

/**
 * Funkce, ktera rozdeli String na tolik casti, kolik je tam
 * znaku, ktere se zadavaji jako parametr a preda vysledne rozdeleni
 * jako pole <String>
 *
 * !!! Velmi dulezite !!!
 * Nutno kontrolovat, aby se nespustila metoda znova, pokud nekdo jeste nad ni
 * pracuje
 * 
 * @param		str		vstupni retezec
 * @param 		znak	oddelovaci znak
 * @return				pole retezcu
 */
void separeter(char *str1, char znak)
{
	char str[1000] = "";
	strcpy(str, str1);
	int l = 0;
	int nenasel = 1;//Nenasel znak v celem retezci
	int length = strlen(str);
	for(int i = 0; i < length; i++)
	{
		if(str[i] == znak)
		{
			nenasel = 0;
			l++;
		}
	}
	if(nenasel)
	{
		l = 1;
	}
	int neniPosledniZnak = 0;//Posledni znak retezce neni hledany znak
	if(str[length - 1] != znak)
	{
		if(nenasel != 1)//!nenasel
		{
			neniPosledniZnak = 1;				
			l++;
		}
	}
	for(int k = 0; k < 25; k++)
	{
		memset(&sepa, '\0', sizeof(sepa[k]));
	}
	int indexPole = 0;
	char tmp[100];
	memset(&tmp, '\0', sizeof(tmp));
	for(int i = 0; i < length; i++)
	{
		if(str[i] == znak)
		{
			//result[indexPole++] = tmp;
			strcpy(sepa[indexPole++], tmp);
			memset(&tmp, '\0', sizeof(tmp));
			//printf("%s\n", sepa[indexPole - 1]);
		}
		else
		{
			sprintf(tmp, "%s%c", tmp, str[i]);
			//tmp += str.charAt(i);
		}
	}
	if(neniPosledniZnak == 1 || nenasel == 1)
	{
		strcpy(sepa[indexPole], tmp);
		//result[indexPole] = tmp;
	}
	if(length == 0)
	{
		length_p = 0;
	}
	else
	{
		length_p = l;	
	}
}

int randint(int n)
{
	if(startRand)
	{
		srand(time(NULL));
		startRand = 0;
	}
	return rand() % n;
}
