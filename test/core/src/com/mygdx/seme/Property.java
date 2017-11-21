package com.mygdx.seme;

import java.util.Arrays;

public class Property extends Card
{
	private int propCost;
	private int [] housesAndHotelRent;
	private int houseCost;
	private int hotelCost;
	private byte houseCount;
	private boolean hotel;
	private int group;
	private int groupCount;
	private int ownedPlayerID;
	
	public Property(String name)
	{
		super(name);
	}
	
	public Property(String name, int propCost, int [] housesAndHotelRent, int houseCost, int group, int groupCount)
	{
		super(name);
		this.propCost = propCost;
		this.housesAndHotelRent = housesAndHotelRent;
		this.houseCost = houseCost;
		this.hotelCost = houseCost * 5;
		this.group = group;
		this.groupCount = groupCount;
		ownedPlayerID = -1; 
	}
	
	//DODELAT
	public void action(Player hrac, Player [] hraci)
	{
		if(getOwnPlayerID() == -1)
		{
			System.out.println("Property " + this.getNazev() + " stoji " + this.getPropertyCost());
			System.out.println("<k> - Koupit pozemek!");
			System.out.println("<a> - aukcni sin!");
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
						if(hrac.getMoney() - this.getPropertyCost() < 0)
						{
							System.out.println("Nelze provest koupi! - Nedostatek penez");
						}
						else
						{
							hrac.removeMoney(this.getPropertyCost());
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
							Table.transmission("p!" + index + "!" + this.getPropertyCost() + "!");
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
									System.out.println("Aktualni nejvyssi cena pozemku je " + max + "$");
									System.out.println(hraci[i].getName() + " nabizi za pozemek " + auctionPrice[i] + "$");
									System.out.println("Hrac " + hraci[i].getName() + " ma " + hraci[i].getMoney());
									System.out.println("Pokud chcete prihodit, zadejte hodnotu , kterou jste odhotni za pozemek zaplatit.");
									System.out.println("Pokud uz nechcete prihodit, zadejte do koznole <K>");
									System.out.println(Arrays.toString(auctionPrice));
									System.out.println("peopleDone: " + peopleDone);
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
											int hod = Integer.parseInt(in);//POZOR - PRIDELAT KONTROLU JESTLI CISLO
											if(hod > hraci[i].getMoney())
											{
												System.out.println("Nelze provest prihozeni! - Nedostatek penez");
												//TRANSMISSION!!!
												Table.transmission("a!f!");
												//TRANSMISSION!!!
											}
											else
											{
												if(hod <= max)
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
								System.out.println("PEOPLE DONE!!");
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
						System.out.println(hraci[index].getName() + " ziskal pozemek z aukce za " + auctionPrice[index]);
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
			int multi = 1;
			for (int i = 0; i < Table.gameTable.length; i++)
			{
				if(Table.gameTable[i] instanceof Property)
				{
					if(((Property)Table.gameTable[i]).getGroup() == this.getGroup())
					{
						if(((Property)Table.gameTable[i]).getOwnPlayerID() == hraci[index].getId())
						{
							suma++;
						}
					}					
				}
			}
			if(suma == this.getGroupCount())
			{
				multi = 2;
			}
			System.out.println(hraci[index].getName() + " vlastni " + suma + " stejne pozemky z " + this.getGroupCount());
			int ind = -1;
			for (int i = 0; i < hraci.length; i++)
			{
				if(hraci[i].getId() == hrac.getId())
				{
					ind = i;
				}
			}
			if(hotel)
			{
				System.out.println(hrac.getName() + " zaplati " + hraci[index].getName() + " " + this.getHousesAndHotelRent()[5]);
				hraci[index].addMoney(this.getHousesAndHotelRent()[5]);
				hrac.removeMoney(this.getHousesAndHotelRent()[5]);
				//TRANSMISSION!!!
				Table.transmission("p!"+ ind + "!" + this.getHousesAndHotelRent()[5] + "!");
				Table.transmission("g!"+ index + "!" + this.getHousesAndHotelRent()[5] + "!");
				//TRANSMISSION!!!
			}
			else if(this.getHouseCount() > 0)
			{
				int houses = this.getHouseCount();
				System.out.println(hrac.getName() + " zaplati " + hraci[index].getName() + " " + this.getHousesAndHotelRent()[houses]);
				hraci[index].addMoney(this.getHousesAndHotelRent()[houses]);
				hrac.removeMoney(this.getHousesAndHotelRent()[houses]);
				//TRANSMISSION!!!
				Table.transmission("p!"+ ind + "!" + this.getHousesAndHotelRent()[houses] + "!");
				Table.transmission("g!"+ index + "!" + this.getHousesAndHotelRent()[houses] + "!");
				//TRANSMISSION!!!
			}
			else
			{
				System.out.println(hrac.getName() + " zaplati " + hraci[index].getName() + " " + this.getHousesAndHotelRent()[0] * multi);
				hraci[index].addMoney(this.getHousesAndHotelRent()[0] * multi);
				hrac.removeMoney(this.getHousesAndHotelRent()[0] * multi);
				//TRANSMISSION!!!
				Table.transmission("p!"+ ind + "!" + this.getHousesAndHotelRent()[0] * multi + "!");
				Table.transmission("g!"+ index + "!" + this.getHousesAndHotelRent()[0] * multi + "!");
				//TRANSMISSION!!!
			}
		}
	}
	
	public byte getHouseCount()
	{
		return houseCount;
	}
	
	public void setHouseCount(byte houseCount)
	{
		this.houseCount = houseCount;
	}

	public void addHouseCount(byte houseCount)
	{
		this.houseCount += houseCount;
	}
	
	public void removeHouseCount(byte houseCount)
	{
		this.houseCount -= houseCount;
	}

	public boolean isHotel()
	{
		return hotel;
	}

	public void setHotel(boolean hotel)
	{
		this.hotel = hotel;
	}

	public int[] getHousesAndHotelRent()
	{
		return housesAndHotelRent;
	}

	public void setHousesAndHotelRent(int[] housesAndHotelRent)
	{
		this.housesAndHotelRent = housesAndHotelRent;
	}
	
	public int getPropertyCost()
	{
		return propCost;
	}

	public void setPropertyCost(int Cost)
	{
		this.propCost = Cost;
	}
	
	public int getHouseCost()
	{
		return houseCost;
	}

	public void setHouseCost(int houseCost)
	{
		this.houseCost = houseCost;
	}

	public int getHotelCost()
	{
		return hotelCost;
	}

	public void setHotelCost(int hotelCost)
	{
		this.hotelCost = hotelCost;
	}

	public int getGroup()
	{
		return group;
	}

	public void setGroup(byte group)
	{
		this.group = group;
	}
	
	public int getOwnPlayerID()
	{
		return ownedPlayerID;
	}

	public void setOwnPlayerID(int id)
	{
		this.ownedPlayerID = id;
	}

	public int getGroupCount()
	{
		return groupCount;
	}

	public void setGroupCount(byte groupCount)
	{
		this.groupCount = groupCount;
	}

	public String toString()
	{
		return "<Class: " + getClass().getName() + ",id: " + getId() + ",nazev: " + getNazev() + ",houseCost: " + getHouseCost() +
				",hotelCost: " + getHotelCost() + ",group: " + getGroup() + ",groupCount" + getGroupCount() + ">";
	}
}
