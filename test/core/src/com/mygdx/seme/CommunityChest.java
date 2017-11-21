package com.mygdx.seme;

public class CommunityChest extends Card
{
	
	public CommunityChest(String name)
	{
		super(name);
	}
	
	public void action(Player hrac, Player [] hraci)
	{
		String whatToDo = Table.chestCards[Assets.rand.nextInt(Table.chestCards.length)];
		System.out.println("Padla karta z comunitychest " + whatToDo);
		switch(whatToDo.charAt(0))
		{
		case '+':hrac.addMoney(Integer.parseInt(whatToDo.substring(1)));
				 System.out.println(hrac.getName() + " ziskal " + whatToDo.substring(1) + " penez");
			     break;
		case '-':hrac.removeMoney(Integer.parseInt(whatToDo.substring(1)));
				 System.out.println(hrac.getName() + " zaplatil " + whatToDo.substring(1) + " penez");
	     	     break;
		case 'o':hrac.jailFree = true;
				 System.out.println(hrac.getName() + " ziskal kartu, ktera mu umozni se dostat z vezeni");
				 System.out.println("Po pouziti tuto kartu ztraci!");
		         break;
		case 'i':hrac.setPosition(10);
				 hrac.jail = true;
				 System.out.println(hrac.getName() + " odchazi do vezeni");
				 break;
		case 'a':String in = whatToDo.substring(1);
		 		 int money = Integer.parseInt(in);
		 		 System.out.println("Kazdy hrac zaplatil " + hrac.getName() + " " + money + "$");
				 for(int i = 0; i < hraci.length; i++)
				 {
					  if(hraci[i].getId() != hrac.getId())
					  {
						  hrac.addMoney(money);
						  hraci[i].removeMoney(money);
					  }
				 }
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
				 break;
		}
		//TRANSMISSION!!!
		Table.transmission("d!" + whatToDo + "!");
		//TRANSMISSION!!!
	}

}
