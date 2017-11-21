package com.mygdx.seme;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.badlogic.gdx.Gdx;

public class Server extends Thread
{
	public static ServerSocket buffer;
	public static BufferedReader socketIn;
	public static String [][] ServerClientsList; 
	ProcessClient [] klienti = new ProcessClient[10];
	int indKlienti = 0;
	
	public static void vypisList()
	{
		for (int i = 0; i < ServerClientsList.length; i++)
		{
			for (int j = 0; j < ServerClientsList[i].length; j++)
			{
				System.out.print(ServerClientsList[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	@Override
	public void run()
	{
		loadServerClientsList();
		vypisList();
		try
		{
			buffer = new ServerSocket(12345);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("Server - ServerSocket inicializace selhala!");
		}
		boolean done = true;
		while(done)
		{
			try
			{
				System.out.println("Server - Cekam na klienta");
				Socket s = buffer.accept();
				System.out.println("Server - Client se pripojil");
				klienti[indKlienti] = new ProcessClient(s);
				klienti[indKlienti++].start();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				done = false;
			}
		}
	}
	
	
	public void loadServerClientsList()
	{
		BufferedReader bf = new BufferedReader(Gdx.files.internal("server_clients.txt").reader(10));
		try
		{
			bf.readLine();
			String input = bf.readLine();
			int pocetKlientu = Integer.parseInt(input);
			ServerClientsList = new String[pocetKlientu][4];
			for(int i = 0; i < pocetKlientu; i++) 
			{
				input = bf.readLine();
				String [] vstup = separeter(input, '!');
				for (int j = 0; j < 4; j++)
				{
					ServerClientsList[i][j] = vstup[j];
				} 
			}
		}
		catch(IOException e)
		{
			System.out.println("Nemuzu nacist server_clients.txt - porusena integrita!");
		}
		
	}
	
	public void saveServerClientsList()
	{
		
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

	public static String checkLoginPass(String login, String pass)
	{
		for (int i = 0; i < ServerClientsList.length; i++)
		{
			if(ServerClientsList[i][0].equals(login))
			{
				if(ServerClientsList[i][1].equals(pass))
				{
					return "ok!"+i;
				}
				else
				{
					return "pf";
				}
			}
		}
		return "lf";
	}
}
