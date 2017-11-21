package com.mygdx.seme;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.badlogic.gdx.Gdx;

public class Table extends Thread
{
	static String startGame = "4!Michal?0?1500?0?0?0!Jirka?0?1500?0?0?0!Petr?0?1500?0?0?0!Zdenek?0?1500?0?0?0!0!";
	static int hodStejnych = 0;
	static boolean changeOfPlayers = false;
	static boolean anotherRun = false;
	static Player [] hraci;
	static int startPozice;
	public static Card [] gameTable = new Card[40];
	
	public static String [] chestCards = 
			new String[]{"+200", "+75", "-50", "o", "i", "a10", "a50", "+20",
					 "+100", "-100", "-50", "+25", "h40;115", "+10", "+100", 
					 "+50", "+100"};

	
	public static String [] chanceCards = 
			new String[]{"l0", "l24", "u", "r", "l11", "+50", "jo",
					"k3", "ji", "h25;100", "t15", "l5", "l39", "c50", 
					"+150", "+100"};

	
	public static boolean packet = false;
	
	public static ArrayList<String> soket = new ArrayList<String>();
	
	Table(String jmeno)
	{
		super(jmeno);
	}
	
	public static synchronized void transmission(String text)
	{
		System.out.println("TRANSMISSION SERVER " + text);
		//TRANSMISSION!!!
		GameScreen.gameInput.add(text);
		GameScreen.pack = true;
		//TRANSMISSION!!!
	}
	
	public static synchronized String recognizeInput()
	{
		String input = soket.get(0);
		soket.remove(0);
		if(soket.size() == 0)
		{
			packet = false;
		}
		return input;
		//DODELAT ROZLISEZNI VSTUPU A SPRAVNEHO ROZPARSOVANI A LOGICKYCH CHYB!!!
	}
	
	public static void gameRules(int index, Player [] hraci)
	{
		System.out.println("Hrac " + hraci[index].getName() + " je na tahu!");
		System.out.println("Nachazi se na pozici: " + hraci[index].getPosition());
		if(hodStejnych == 3)
		{
			hraci[index].setPosition(10);
			hraci[index].jail = true;
			System.out.println(hraci[index].getName() + " jde do vezeni!");
			hodStejnych = 0;
		}
		else
		{
			if(!hraci[index].jail)
			{
				while(!Table.packet)
				{
					System.out.flush();
				}
				recognizeInput();
				System.out.println("Hod kostkou[1]:");
				int kostka1 = Assets.rand.nextInt(5) + 1;
				//int kostka1 = Assets.sc.nextInt();
				int kostka2 = Assets.rand.nextInt(5) + 1;
				System.out.println("Hod kostkou[2]:");
				//int kostka2 = Assets.sc.nextInt();
				int hod = kostka1 + kostka2;
				System.out.println(hraci[index].getName() + " hodil " + kostka1 + " " + kostka2);
					//TRANSMISSION!!!
					transmission("s!" + kostka1 + "!" + kostka2 + "!");
					//TRANSMISSION!!!
				if(hraci[index].getPosition() + hod > 39)
				{
					hraci[index].addMoney(200);
					System.out.println(hraci[index].getName() + " ziskal za projezd startem 200 penez");
					hod = (hraci[index].getPosition() + hod) - 40;
					hraci[index].setPosition(hod);
				}
				else
				{
					hraci[index].setPosition(hraci[index].getPosition() + hod);			
				}
				System.out.println(hraci[index].getName() + " ma nastaveno jail na " + hraci[index].jail);
				System.out.println("hrac: " + hraci[index].getName() + ", money: " + hraci[index].getMoney() + ", pozice: " + hraci[index].getPosition() + ", nazev: " + gameTable[hraci[index].getPosition()].getNazev());
				gameTable[hraci[index].getPosition()].action(hraci[index], hraci);
				if(kostka1 == kostka2 && !hraci[index].jail)
				{
					hodStejnych++;
					anotherRun = true;
				}
				else
				{
					hodStejnych = 0;
				}
			}
			else
			{
				System.out.println(hraci[index].getName() + " ma nastaveno jail na " + hraci[index].jail);
				System.out.println("hrac: " + hraci[index].getName() + ", money: " + hraci[index].getMoney() + ", pozice: " + hraci[index].getPosition() + ", nazev: " + gameTable[hraci[index].getPosition()].getNazev());
				gameTable[hraci[index].getPosition()].action(hraci[index], hraci);
			}
			if(gameTable[hraci[index].getPosition()] instanceof Property)
			{
				if(((Property)gameTable[hraci[index].getPosition()]).getOwnPlayerID() == hraci[index].getId())
				{
					addHouses(hraci[index]);					
				}
			}
			if(hraci[index].getMoney() <= 0)
			{
				Player [] newHraci = new Player[hraci.length - 1];
				int j = 0;
				for (int i = 0; i < newHraci.length; i++)
				{
					if(i != index)
					{
						newHraci[i] = hraci[j];						
					}
					else
					{
						newHraci[i] = hraci[++j];
					}
					j++;
				}
				Table.hraci = newHraci;
				changeOfPlayers = true;
				//TRANSMISSION!!!
				transmission("l!" + index + "!");
				//TRANSMISSION!!!
			}
			if(!anotherRun)
			{
				//TRANSMISSION!!!
				transmission("e!");
				//TRANSMISSION!!!				
			}
			else
			{
				//TRANSMISSION!!!
				transmission("n!");
				//TRANSMISSION!!!
			}
		}
	}
	
	public static int [] isOwningDistrict(int pozice, Player hrac)
	{
		int grp = ((Property)gameTable[pozice]).getGroup();
		int grpPocet = ((Property)gameTable[pozice]).getGroupCount();
		int [] indexy = new int[grpPocet];
		for (int i = 0; i < indexy.length; i++)
		{
			indexy[i] = -1; 
		}
		indexy[0] = pozice;
		int j = 0;
		for (int i = 0; i < gameTable.length; i++)
		{
			if(gameTable[i] instanceof Property && i != pozice)
			{
				if(((Property)gameTable[i]).getGroup() == grp)
				{
					j++;
					if(((Property)gameTable[i]).getOwnPlayerID() == hrac.getId())
					{
						indexy[j] = i; 
					}
					if(j + 1 >= grpPocet)
					{
						break;
					}
				}
			}
		}
		boolean chyba = false;
		for (int i = 0; i < indexy.length; i++)
		{
			if(indexy[i] == -1)
			{
				chyba = true;
			}
		}
		if(chyba)
		{
			return null;
		}
		else
		{
			return indexy;
		}
	}
	
	//DODELAT - neni kompletni
	public static void addHouses(Player hrac)
		{
			int pozice = hrac.getPosition();
			int [] indexy = isOwningDistrict(pozice, hrac);
			if(indexy == null)
			{
				System.out.println(hrac.getName() + " nevlastni vsechnny pozmeky z daneho okresku!");
			}
			else
			{
				int grpPocet = ((Property)gameTable[pozice]).getGroupCount();
				Arrays.sort(indexy);
				String tmp;
				boolean hotov = true;
				System.out.println(hrac.getName() + " se nachazi na pozemku " + gameTable[pozice].getNazev());
				System.out.println("Cena jednoho domu je " + ((Property)gameTable[pozice]).getHouseCost() + "$ a cena hotelu je " + ((Property)gameTable[pozice]).getHotelCost() + "$");
				System.out.println("Zadejte <1> <2> <3> podle toho, ke kteremu budete chtit pridat jeden dum");
				System.out.println("Pro koupi hotelu musite nejdrive vlastnit 5 domu na jednom pozemku!");
				System.out.println("Pro ukonceni nakupu domu/hotelu, zadejte klavesu <k>");
				while(hotov)
				{
					tmp = Assets.sc.nextLine();
					tmp = tmp.toLowerCase();
					if(!tmp.equals(""))
					{
						if(tmp.charAt(0) == 'k')
						{
							hotov = false;
						}
						else
						{
							byte add = 1;
							int ind = -1;
							switch(tmp.charAt(0))
							{
							case '1':ind = 0;
									 break;
							case '2':ind = 1;
									 break;
							case '3':if(grpPocet == 3)
									 {
										ind = 2;
									 }
								     break;
							}
							if(ind != -1)
							{
								if(((Property)gameTable[indexy[ind]]).getHouseCount() == 5 && !((Property)gameTable[indexy[ind]]).isHotel())
								{
									if(hrac.getMoney() - ((Property)gameTable[indexy[ind]]).getHotelCost() >= 0)
									{
										((Property)gameTable[indexy[ind]]).setHotel(true);
										hrac.removeMoney(((Property)gameTable[indexy[ind]]).getHotelCost());
									}
									else
									{
										System.out.println("Nelze provest koupi! - Nedostatek penez");
									}
								}
								else
								{
									if(hrac.getMoney() - ((Property)gameTable[indexy[ind]]).getHouseCost() >= 0)
									{
										((Property)gameTable[indexy[ind]]).addHouseCount(add);
										hrac.removeMoney(((Property)gameTable[indexy[ind]]).getHouseCost());
									}
									else
									{
										System.out.println("Nelze provest koupi! - Nedostatek penez");
									}															
								}
							}
							else
							{
								System.out.println("Zadana hodnota neodpovida predipsu!");
							}
						}
					}
				}			
			}
		}
	
	@Override
    public void run()
	{
		System.out.println("Vlákno " + getName() + " spuštìno");
		setUpTable();
		setGameStatusFull(startGame);
		while(hraci.length != 0)
		{
			for(int i = startPozice; i < hraci.length; i++)
			{
				gameRules(i, hraci);
				System.out.println();
				if(changeOfPlayers)
				{
					i--;
					changeOfPlayers = true;
					anotherRun = false;
				}
				if(anotherRun)
				{
					i--;
					anotherRun = false;
				}
			}
		}
		System.out.println("Vlákno " + getName() + " ukonèeno");
    }
	
	public static void setGameStatusFull(final String input)
	{
		String [] vstup = Assets.separeter(input, '!');
		int pocetHracu = Integer.parseInt(vstup[0]);
		hraci = new Player[pocetHracu];
		int intB, pocetD;
		for (int i = 0; i < pocetHracu; i++)
		{
			String [] hracInfo = Assets.separeter(vstup[i+1], '?');
			System.out.println(Arrays.toString(hracInfo));
			String jmeno = hracInfo[0];
			int hracMoney = Integer.parseInt(hracInfo[2]);
			hraci[i] = new Player(jmeno, hracMoney);
			hraci[i].setPosition(Integer.parseInt(hracInfo[1]));
			int id = hraci[i].getId();
			//System.out.println(hraci[i].getName());
			//System.out.println(hraci[i].getId());
			//System.out.println(hraci[i].getMoney());
			//System.out.println(hraci[i].getPosition());
			int pocetBudov = Integer.parseInt(hracInfo[3]);
			if(pocetBudov != 0)
			{
				String [] indBudovy = Assets.separeter(hracInfo[4], ',');
				//System.out.println(Arrays.toString(indBudovy));
				String [] pocetDomu = Assets.separeter(hracInfo[5], ',');
				//System.out.println(Arrays.toString(pocetDomu));
				for (int j = 0; j < pocetBudov; j++)
				{
					intB = Integer.parseInt(indBudovy[j]);
					pocetD = Integer.parseInt(pocetDomu[j]);
					if(gameTable[intB] instanceof Property)
					{
						((Property)gameTable[intB]).setOwnPlayerID(id);					
					}
					else if(gameTable[intB] instanceof Railroad)
					{
						((Railroad)gameTable[intB]).setOwnPlayerID(id);
					}
					else
					{
						((Utility)gameTable[intB]).setOwnPlayerID(id);
					}
					if(pocetD != 0)
					{
						if(pocetD == 6)
						{
							((Property)gameTable[intB]).setHotel(true);
							//System.out.println(((Property)gameTable[intB]).isHotel());
						}
						else
						{
							((Property)gameTable[intB]).setHouseCount((byte)pocetD);
							//System.out.println(((Property)gameTable[intB]).getHouseCount());
						}
					}
				}
			}
		}
		startPozice = Integer.parseInt(vstup[vstup.length-1]);
		//System.out.println("startPozice: " + startPozice);
	}
	
	public static void setUpTable()
	{
		try
		{
			BufferedReader bf = Gdx.files.internal("CardsInfo.txt").reader(10);			
			bf.readLine();
			String line;
			int i = 0;
			while((line = bf.readLine()) != null)
			{
				String [] array = Assets.separeter(line, '|');
				switch(array[0].charAt(0))
				{
				case 'S':Table.gameTable[i++] = new Start("Start");
						 break;
				case 'C':Table.gameTable[i++] = new CommunityChest("Chest");
				  		 break;
				case 'P':String [] array2 = Assets.separeter(array[3], ';');
						 int [] pole = new int[6];
						 for (int j = 0; j < pole.length; j++)
						 {
							 pole[j] = Integer.parseInt(array2[j]);
						 }
						 Table.gameTable[i++] = new Property(array[1], Integer.parseInt(array[2]), pole, Integer.parseInt(array[4]), Integer.parseInt(array[5]), Integer.parseInt(array[6]));
					     break;
				case 'J':Table.gameTable[i++] = new Jail("Jail");
				         break;
				case 'G':Table.gameTable[i++] = new GoToJail("GoToJail");
				         break;
				case 'L':Table.gameTable[i++] = new ParkingLot("ParkingLot");
				         break;
				case 'H':Table.gameTable[i++] = new Chance("Chance");
				         break;
				case 'R':Table.gameTable[i++] = new Railroad(array[1]);
				         break;
				case 'U':Table.gameTable[i++] = new Utility(array[1]);
		  			     break;
				case 'T':Table.gameTable[i++] = new Tax("Tax", Integer.parseInt(array[1]));
					     break;
				}
			}
			bf.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
