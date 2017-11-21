package com.mygdx.seme;

public class Utility extends Card
{
	int [] multi = new int[]{4, 10};
	private int ownedPlayerID;
	
	public Utility(String name)
	{
		super(name);
		ownedPlayerID = -1;
	}
	
	//DODELAT - s penezi
	public void action(Player hrac, Player [] hraci)
	{
		if(getOwnPlayerID() == -1)
		{
			System.out.println("Utility " + this.getNazev() + " stoji " + 200);
			System.out.println("<k> - Koupit utility!");
			System.out.println("<a> - Aukcni sin!");
			boolean done = true;
			while(done)
			{
				while(!Table.packet)
				{
					System.out.flush();
				}
				String in = Table.recognizeInput();
				in = in.toLowerCase();
				if(!in.equals(""))
				{
					if(in.charAt(0) == 'k')
					{
						if(hrac.getMoney() - 200 < 0)
						{
							System.out.println("Nelze provest koupi! - Nedostatek penez");
						}
						else
						{
							hrac.removeMoney(200);
							this.setOwnPlayerID(hrac.getId());
							done = false;
							int index = -1;
							for (int i = 0; i < hraci.length; i++)
							{
								if(hraci[i] == hrac)
								{
									index = i;
								}
							}
							//TRANSMISSION!!!
							Table.transmission("b!" + index + "!" + hrac.getPosition() + "!");
							Table.transmission("p!" + index + "!" + 200 + "!");
							//TRANSMISSION!!!
						}
					}
					else if(in.charAt(0) == 'a')
					{
						boolean auction = true;
						int [] auctionPrice = new int[hraci.length];
						int peopleDone = 0;
						int max = 0;
						while(auction)
						{
							for (int i = 0; i < hraci.length; i++)
							{
								if(auctionPrice[i] != -1 && peopleDone < auctionPrice.length - 1)
								{
									System.out.println("Hrac " + hraci[i].getName() + " ma " + hraci[i].getMoney());
									System.out.println("Aktualni nejvyssi cena strediska je " + max + "$");
									System.out.println(hraci[i].getName() + " nabizi za strediska " + auctionPrice[i] + "$");
									System.out.println("Hrac " + hraci[i].getName() + " ma " + hraci[i].getMoney());
									System.out.println("Pokud chcete prihodit, zadejte hodnotu , kterou jste odhotni za pozemek zaplatit.");
									System.out.println("Pokud uz nechcete prihodit, zadejte do koznole <K>");
									boolean rightInput = true;
									while(rightInput)
									{
										while(!Table.packet)
										{
											System.out.flush();
										}
										in = Table.recognizeInput();
										in = in.toLowerCase();
										if(in.charAt(0) == 'k')
										{
											auctionPrice[i] = -1;
											peopleDone++;
											//TRANSMISSION!!!
											Table.transmission("a!k!");
											//TRANSMISSION!!!
											rightInput = false;
										}
										else
										{
											int hod = Integer.parseInt(in);
											if(hod > hraci[i].getMoney())
											{
												System.out.println("Nelze provest prihozeni! - Nedostatek penez");
												//TRANSMISSION!!!
												Table.transmission("a!f!");
												//TRANSMISSION!!!
											}
											else
											{
												if(hod < max)
												{
													System.out.println("Prihozeni je mensi nez max: " + max + ", prihodte vice nebo zruste prihazovani!");
													//TRANSMISSION!!!
													Table.transmission("a!f!");
													//TRANSMISSION!!!
												}
												else
												{
													max = hod;
													auctionPrice[i] = hod;
													//TRANSMISSION!!!
													Table.transmission("a!a!" + max + "!");
													//TRANSMISSION!!!
													rightInput = false;																									
												}												
											}
										}																			
									}
								}
								//TRANSMISSION!!!
								Table.transmission("a!n!");
								//TRANSMISSION!!!
							}
							if(peopleDone >= auctionPrice.length - 1)
							{
								auction = false;
							}
						}
						int index = -1;
						for (int i = 0; i < auctionPrice.length; i++)
						{
							if(auctionPrice[i] != -1)
							{
								index = i;
							}
						}
						System.out.println(hraci[index].getName() + " ziskal stredisko z aukce za " + auctionPrice[index]);
						hraci[index].removeMoney(auctionPrice[index]);
						this.setOwnPlayerID(hraci[index].getId());
						//TRANSMISSION!!!
						Table.transmission("p!" + index + "!" + auctionPrice[index] + "!");
						Table.transmission("b!"+ index + "!" + hrac.getPosition() + "!");
						//TRANSMISSION!!!
						done = false;
					}
					//TRANSMISSION!!!
					Table.transmission("a!e!");
					//TRANSMISSION!!!
				}
			}
		}
		else
		{
			int index = -1;
			for (int i = 0; i < hraci.length; i++)
			{
				if(hraci[i].getId() == this.getOwnPlayerID())
				{
					index = i;
				}
			}
			int ind = -1;
			for (int i = 0; i < hraci.length; i++)
			{
				if(hraci[i].getId() == hrac.getId())
				{
					ind = i;
				}
			}
			if(index == -1)
			{
				System.out.println("Pozemek vlastni jiny hrac nez ten ktery je v seznamu hracu.");
				System.out.println("Pozemek id hrace: " + this.getOwnPlayerID());
				return;
			}
			System.out.println("Pozemek vlastni " + hraci[index].getName());
			
			int count = 0;
			for (int i = 0; i < Table.gameTable.length; i++)
			{
				if(Table.gameTable[i] instanceof Utility)
				{
					if(((Utility)Table.gameTable[i]).getOwnPlayerID() == hraci[index].getId())
					{
						count++;
					}					
				}
			}
			System.out.println("count: " + count);
			System.out.println("Hod kostkou[1]:");
			//int kostka1 = rand.nextInt(5) + 1;
			int kostka1 = Assets.sc.nextInt();
			//int kostka2 = rand.nextInt(5) + 1;
			System.out.println("Hod kostkou[2]:");
			int kostka2 = Assets.sc.nextInt();
			System.out.println(hrac.getName() + " hodil " + kostka1 + " + " + kostka2);
			System.out.println(hraci[index].getName() + " vlastni " + count + " stredisek");
			System.out.println(hrac.getName() + " zaplati " + hraci[index].getName() + " " + (kostka1 + kostka2)*multi[count - 1]);
			hraci[index].addMoney((kostka1 + kostka2)*multi[count - 1]);
			hrac.removeMoney((kostka1 + kostka2)*multi[count - 1]);
			//TRANSMISSION!!!
			Table.transmission("p!"+ ind + "!" + (kostka1 + kostka2)*multi[count - 1] + "!");
			Table.transmission("g!"+ index + "!" + (kostka1 + kostka2)*multi[count - 1] + "!");
			//TRANSMISSION!!!
		}
	}
	
	public int getOwnPlayerID()
	{
		return ownedPlayerID;
	}

	public void setOwnPlayerID(int id)
	{
		this.ownedPlayerID = id;
	}
}
