package com.mygdx.seme;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Assets
{
	static Texture texture_logo;
	static Texture [] texture_dice_faces;
	static Sprite logo;
	static Sprite [] dice;
	static String startGame = "4!Michal?0?1500?0?0?0!Jirka?0?1500?0?0?0!Petr?0?1500?0?0?0!Zdenek?0?1500?0?0?0!0!";
	
	static Random rand = new Random();
	
	public static Scanner sc = new Scanner(System.in);
	
	public static void load()
	{
		texture_logo = new Texture(Gdx.files.internal("monopoly_logo.png"));
		logo = new Sprite(texture_logo);
		logo.flip(false, true);
		texture_dice_faces = new Texture[6];
		dice = new Sprite[6];
		for (int i = 0; i < 6; i++)
		{
			texture_dice_faces[i] = new Texture(Gdx.files.internal("Dice/Face0" + (i+1) + "DownSampled.png"));
			dice[i] = new Sprite(texture_dice_faces[i]);
			dice[i].flip(false, true);
		}
		setUpTable();
		setGameStatusFull(startGame);
	}
	
	/**
	 * Funkce, ktera rozdeli String na tolik casti, kolik je tam
	 * znaku, ktere se zadavaji jako parametr a preda vysledne rozdeleni
	 * jako pole <Stribng>
	 * 
	 * @param		str		vstupni retezec
	 * @param 		znak	oddelovaci znak
	 * @return				pole retezcu
	 */
	public static String [] separeter(String str, char znak)
	{
		String [] result = new String[0];
		boolean setStart = true;
		int ind = 0;
		int indexS = 0;
		for (int i = 0; i < str.length(); i++)
		{
			if(str.charAt(i) == znak && !setStart)
			{
				String [] tmp = new String[result.length + 1];
				for (int j = 0; j < result.length; j++)
				{
					tmp[j] = result[j];
				}
				result = tmp;
				result[ind++] = str.substring(indexS, i);
				setStart = true;
				indexS = i;
			}
			else if(i + 1 == str.length() && !setStart)
			{
				String [] tmp = new String[result.length + 1];
				for (int j = 0; j < result.length; j++)
				{
					tmp[j] = result[j];
				}
				result = tmp;
				result[ind++] = str.substring(indexS, i+1);
				setStart = true;
				indexS = i;
			}
			else
			{
				if(setStart && str.charAt(i) != znak)
				{
					indexS = i;
					setStart = false;
				}
			}
		}
		return result;
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
				String [] array = separeter(line, '|');
				switch(array[0].charAt(0))
				{
				case 'S':GameScreen.screenTable[i++] = new Start("Start");
						 break;
				case 'C':GameScreen.screenTable[i++] = new CommunityChest("Chest");
				  		 break;
				case 'P':String [] array2 = separeter(array[3], ';');
						 int [] pole = new int[6];
						 for (int j = 0; j < pole.length; j++)
						 {
							 pole[j] = Integer.parseInt(array2[j]);
						 }
						 GameScreen.screenTable[i++] = new Property(array[1], Integer.parseInt(array[2]), pole, Integer.parseInt(array[4]), Integer.parseInt(array[5]), Integer.parseInt(array[6]));
					     break;
				case 'J':GameScreen.screenTable[i++] = new Jail("Jail");
				         break;
				case 'G':GameScreen.screenTable[i++] = new GoToJail("GoToJail");
				         break;
				case 'L':GameScreen.screenTable[i++] = new ParkingLot("ParkingLot");
				         break;
				case 'H':GameScreen.screenTable[i++] = new Chance("Chance");
				         break;
				case 'R':GameScreen.screenTable[i++] = new Railroad(array[1]);
				         break;
				case 'U':GameScreen.screenTable[i++] = new Utility(array[1]);
		  			     break;
				case 'T':GameScreen.screenTable[i++] = new Tax("Tax", Integer.parseInt(array[1]));
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
	
	/**
	 * Michal?0?1500?0?0?0
	 * a)Nazev hrace, b)Umisteni na herni plose(index do pole)
	 * c)Pocatecni stav penez,d)Celkovy pocet budov
	 * d1)pole indexu budov na hernim platnu
	 * d2)k prislusnemu predchozimu parameteru, pocet vylepseni
	 * ke konkretni vlastnene budove(6 == hotel, jinak pocet malych
	 * vylepseni)
	 * e)Startovni pozice vsech hracu na herni plose
	 * @param		input		vstupni retezec s parametry hry
	 */
	public static void setGameStatusFull(final String input)
	{
		String [] vstup = Assets.separeter(input, '!');
		int pocetHracu = Integer.parseInt(vstup[0]);
		GameScreen.screenHraci = new Player[pocetHracu];
		int intB, pocetD;
		for (int i = 0; i < pocetHracu; i++)
		{
			String [] hracInfo = Assets.separeter(vstup[i+1], '?');
			
			//INFO O HRACICH
			//System.out.println(Arrays.toString(hracInfo));
			
			
			String jmeno = hracInfo[0];
			int hracMoney = Integer.parseInt(hracInfo[2]);
			GameScreen.screenHraci[i] = new Player(jmeno, hracMoney);
			GameScreen.screenHraci[i].setPosition(Integer.parseInt(hracInfo[1]));
			int id = GameScreen.screenHraci[i].getId();
			//System.out.println(GameScreen.screenHraci[i].getName());
			//System.out.println(GameScreen.screenHraci[i].getId());
			//System.out.println(GameScreen.screenHraci[i].getMoney());
			//System.out.println(GameScreen.screenHraci[i].getPosition());
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
					if(GameScreen.screenTable[intB] instanceof Property)
					{
						((Property)GameScreen.screenTable[intB]).setOwnPlayerID(id);					
					}
					else if(GameScreen.screenTable[intB] instanceof Railroad)
					{
						((Railroad)GameScreen.screenTable[intB]).setOwnPlayerID(id);
					}
					else
					{
						((Utility)GameScreen.screenTable[intB]).setOwnPlayerID(id);
					}
					if(pocetD != 0)
					{
						if(pocetD == 6)
						{
							((Property)GameScreen.screenTable[intB]).setHotel(true);
							//System.out.println(((Property)GameScreen.screenTable[intB]).isHotel());
						}
						else
						{
							((Property)GameScreen.screenTable[intB]).setHouseCount((byte)pocetD);
							//System.out.println(((Property)GameScreen.screenTable[intB]).getHouseCount());
						}
					}
				}
			}
		}
		GameScreen.pozice = Integer.parseInt(vstup[vstup.length-1]);
	}
}
