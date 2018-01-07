#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void retezec(char* str, int size)
{
	char t[50];
	strcpy(t, "0123456789abcdefghijklmnopqrstuvwxyz");
	strncpy(str, t, size);
}

int main(void)
{
	char text[50];
	retezec(text, 50);	
	printf("%s\n", text);
}
