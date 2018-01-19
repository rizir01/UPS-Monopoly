#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include "zprava.h"

struct Zprava getMessage(int client_socket);

struct Zprava getMessage(int client_socket)//Monza pridelat stav erroru ($asda#asdasd#), kde pochyti jen prvni cast textu
{
	struct Zprava p;
	int return_value = 0;
	int zac = 0;//bool
	int kon = 0;//bool
	char pole[1000];
	int indexP = 0;
	int delka = 0;
	memset(&pole, '\0', sizeof(pole));
	do
	{
		char z;
		return_value=recv(client_socket, &z, 1, 0);
		if(zac == 1)
		{
		    delka++;
		    pole[indexP++] = z;  
		}
		            
		if(z == '$')
		{
		    zac = 1;
		}
		else if(z == '#')
		{
		    if(zac == 0)
		    {
		    	if(pole[0] != '0')
		    	{
		    		printf("Zprava: |%s|\n", pole);	
				}
				printf("Spatny format zpravy (# drive nez $)!\n");
		    	p.error = 1;
		    	return p;
			}
			kon = 1;
			break;
	    }
	}while(return_value > 0);
	if(kon == 0 && zac == 0)
	{
		if(pole[0] == '0' || pole[0] == '\0')
		{
			printf("Zprava: |same nuly|\n");
			p.error = 50;//Error pro spadnuti socketu	
		}
		else
		{
			printf("Zprava: |%s|\n", pole);
			p.error = 2;
		}
		printf("zm: Spatny format zpravy (zprava neni mezi $ a #)!\n");
		return p;
	}
	else if(kon == 0)
	{
		printf("Zprava: |%s|\n", pole);
		printf("Spatny format zpravy (Neni ukoncovaci znak #)!\n");
		p.error = 3;
		return p;
	}
	if(delka == 0)
	{
		printf("Zprava: |%s|\n", pole);
		printf("Spatny format zpravy (Predana zprava neobsahuje zadna data)!\n");
		p.error = 4;
		return p;
	}
	pole[indexP - 1] = '\0';
	memcpy(p.msg, pole, sizeof(pole));
	p.length = delka;
	p.error = 0;
	return p;
}
