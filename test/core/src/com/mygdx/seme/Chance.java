package com.mygdx.seme;

public class Chance extends Card
{

	public Chance(String name)
	{
		super(name);
	}
	
	//DODELAT - rozpracovane ale ne dokonale
	public void action(Player hrac, Player [] hraci)
	{
		String whatToDo = Table.chanceCards[Assets.rand.nextInt(Table.chanceCards.length)];
		System.out.println(hrac.getName() + " si lizl kartu chance: " + whatToDo);
		switch(whatToDo.charAt(0))
		{
		case '+':hrac.addMoney(Integer.parseInt(whatToDo.substring(1)));
				 System.out.println(hrac.getName() + " ziskal " + whatToDo.substring(1) + " penez");
				 //TRANSMISSION!!!
				 Table.transmission("c!" + whatToDo + "!");
				 //TRANSMISSION!!!
			     break;
		case '-':hrac.removeMoney(Integer.parseInt(whatToDo.substring(1)));
				 System.out.println(hrac.getName() + " ztrail " + whatToDo.substring(1) + " penez");
				 //TRANSMISSION!!!
				 Table.transmission("c!" + whatToDo + "!");
				 //TRANSMISSION!!!
			     break;
		case 'c':for(int i = 0; i < hraci.length; i++)
				 {
					  if(hraci[i].getId() != hrac.getId())
					  {
						  hrac.removeMoney(50);
						  hraci[i].addMoney(50);
					  }
				 }
				 System.out.println(hrac.getName() + " zaplatil kazdemu hraci 50$");
				 //TRANSMISSION!!!
				 Table.transmission("c!" + whatToDo + "!");
				 //TRANSMISSION!!!
				 break;
		case 'l':String in = whatToDo.substring(1);
				 int targetDest = Integer.parseInt(in);
				 if(hrac.getPosition() > targetDest)
				 {
					 hrac.addMoney(200);
					 System.out.println(hrac.getName() + " ziskal za projezd startem 200$");
				 }
				 hrac.setPosition(targetDest);
				 System.out.println(hrac.getName() + " se presunul na pozici " + Table.gameTable[targetDest].getNazev());
				 //TRANSMISSION!!!
				 Table.transmission("c!" + whatToDo + "!");
				 //TRANSMISSION!!!
				 Table.gameTable[targetDest].action(hrac, hraci);
				 break;
		case 'j':if(whatToDo.charAt(1) == 'i')
				 {
					 hrac.jail = true;
					 hrac.setPosition(10);
					 System.out.println(hrac.getName() + " jde do vezeni!");
				 }
				 else
				 {
					 hrac.jailFree = true;
					 System.out.println(hrac.getName() + " ziskal kartu, ktera mu umozni se dostat z vezeni");
					 System.out.println("Po pouziti tuto kartu ztraci!");
				 }
				 //TRANSMISSION!!!
				 Table.transmission("c!" + whatToDo + "!");
				 //TRANSMISSION!!!
				 break;
		case 'k':in = whatToDo.substring(1);
		 		 targetDest = Integer.parseInt(in);
		 		 hrac.setPosition(hrac.getPosition() - targetDest);
				 System.out.println(hrac.getName() + " se posouva o " + targetDest + " misto/a zpet!");
				 //TRANSMISSION!!!
				 Table.transmission("c!" + whatToDo + "!");
				 //TRANSMISSION!!!
				 Table.gameTable[hrac.getPosition()].action(hrac, hraci);
				 break;
		case 'h':in = whatToDo.substring(1);
				 String [] pom = Assets.separeter(in, ';');
				 int hotel = 0, houses = 0;
				 for (int i = 0; i < Table.gameTable.length; i++)
				 {
					 if(Table.gameTable[i] instanceof Property)
					 {
						 if(((Property)Table.gameTable[i]).getOwnPlayerID() == hrac.getId())
						 {
							 if(((Property)Table.gameTable[i]).isHotel())
							 {
								 hotel++;
							 }
							 else
							 {
								 houses += ((Property)Table.gameTable[i]).getHouseCount();
							 }
						 }						 
					 }
				 }
				 int cost1 = Integer.parseInt(pom[0]);
				 int cost2 = Integer.parseInt(pom[1]);
				 hrac.removeMoney(houses * cost1);
				 hrac.removeMoney(hotel * cost2);
				 System.out.println(hrac.getName() + " musi zaplatit bance za kazdy svuj hotel(" + cost2 + "$) a kazdy svuj dum(" + cost1 + "$)");
				 System.out.println("Tedy celkove predal bance: " + (houses * cost1) + "$ za domy a " + (hotel * cost2) + "$ za hotely!");
				 //TRANSMISSION!!!
				 Table.transmission("c!" + whatToDo + "!");
				 //TRANSMISSION!!!
				 break;
		case 't':in = whatToDo.substring(1);
		 	     int money = Integer.parseInt(in);
		 	     hrac.removeMoney(money);
		 	     System.out.println(hrac.getName() + " zaplatil bance dane v hodnote " + money + "$!");
		 	     //TRANSMISSION!!!
		 		 Table.transmission("c!" + whatToDo + "!");
		 		 //TRANSMISSION!!!
		 	     break;
		case 'u':int ind = hrac.getPosition();
				 for (int i = ind + 1; i < Table.gameTable.length; i++)
				 {
					if(i + 1 == Table.gameTable.length)
					{
						i = 0;
					}
					else if(Table.gameTable[i] instanceof Utility)
					{
						ind = i;
						break;
					}
				 }
				 if(hrac.getPosition() > ind)
				 {
					 hrac.addMoney(200);
					 System.out.println(hrac.getName() + " ziskal za projezd startem 200$");
				 }
				 hrac.setPosition(ind);
			     System.out.println(hrac.getName() + " se presunul na pozici " + Table.gameTable[ind].getNazev());
			     //TRANSMISSION!!!
			     Table.transmission("c!" + whatToDo + "!");
			     //TRANSMISSION!!!
			     Table.gameTable[ind].action(hrac, hraci);
			     break;
		case 'r':ind = hrac.getPosition();
				 for (int i = ind + 1; i < Table.gameTable.length; i++)
				 {
					if(i + 1 == Table.gameTable.length)
					{
						i = 0;
					}
					else if(Table.gameTable[i] instanceof Railroad)
					{
						ind = i;
						break;
					}
				 }
				 if(hrac.getPosition() > ind)
				 {
					 hrac.addMoney(200);
					 System.out.println(hrac.getName() + " ziskal za projezd startem 200$");
				 }
				 hrac.setPosition(ind);
			     System.out.println(hrac.getName() + " se presunul na pozici " + Table.gameTable[ind].getNazev());
			     //TRANSMISSION!!!
			     Table.transmission("c!" + whatToDo + "!");
			     //TRANSMISSION!!!
			     Table.gameTable[ind].action(hrac, hraci);
			     break;
		}
	}

}
