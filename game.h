#ifndef GAME_H
#define GAME_H
struct Game
{
	//Kdo je momentalne natahu
	int natahu;
	
	//Kolik kdo ma penez
	int penize[4];
	
	//Index hrace ktery je ve vezeni
	int vezeni[4];
	
	//Pole, ktere v prvni bloku ma seznam vsech vlastnenych polozek
	//daneho hrace a v druhem bloku pocet upgradu daneho pozemku, pokud
	//jsou upgrady pritomny
	char majetek[4][2];
}
#endif
