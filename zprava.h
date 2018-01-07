#ifndef ZPRAVA_H
#define ZPRAVA_H
struct Zprava
{
	char msg[1000];
	int length;
	int zaznamInd;
	int error;//bool
};
#endif
