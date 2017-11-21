package com.mygdx.seme;

public class Railroad extends Card
{
	static int [] rent = new int[]{25, 50, 100, 200};
	private int ownedPlayerID;
	
	public Railroad(String name)
	{
		super(name);
		ownedPlayerID = -1;
	}
	
	//DODELAT
	public void action(Player hrac, Player [] hraci)
	{
		if(getOwnPlayerID() == -1)
		{
			System.out.println("Railroad " + this.getNazev() + " stoji " + 200);
			System.out.println("<k> - Koupit pozemek!");
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
									System.out.println("Aktualni nejvyssi cena stanice je " + max + "$");
									System.out.println(hraci[i].getName() + " nabizi za stanici " + auctionPrice[i] + "$");
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
						System.out.println(hraci[index].getName() + " ziskal stanici z aukce za " + auctionPrice[index]);
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
			if(index == -1)
			{
				System.out.println("Pozemek vlastni jiny hrac nez ten ktery je v seznamu hracu.");
				System.out.println("Pozemek id hrace: " + this.getOwnPlayerID());
				return;
			}
			System.out.println("Pozemek vlastni " + hraci[index].getName());
			
			int suma = 0;
			for (int i = 0; i < Table.gameTable.length; i++)
			{
				if(Table.gameTable[i] instanceof Railroad)
				{
					if(((Railroad)Table.gameTable[i]).getOwnPlayerID() == hraci[index].getId())
					{
						suma++;
					}					
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
			System.out.println(hraci[index].getName() + " vlastni " + suma + " stanic");
			System.out.println(hrac.getName() + " zaplati " + hraci[index].getName() + " " + rent[suma - 1]);
			hraci[index].addMoney(rent[suma - 1]);
			hrac.removeMoney(rent[suma - 1]);
			//TRANSMISSION!!!
			Table.transmission("p!"+ ind + "!" + rent[suma - 1] + "!");
			Table.transmission("g!"+ index + "!" + rent[suma - 1] + "!");
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
