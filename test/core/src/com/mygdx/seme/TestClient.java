package com.mygdx.seme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.plaf.synth.SynthSpinnerUI;

public class TestClient extends Thread
{
	/**
	 * Definuje, v jakem stavu se hrac nachazi,
	 * tedy jestli je v lobby, ve hre atd.
	 */
	int stavHrace = 1;
	
	Message [] buffer;
	
	static Socket socket;
	
	static OutputStream bw;
	
	static BufferedReader bf;
	
	static Scanner sc = new Scanner(System.in);
	
	TestClientRecieve receive;
	
	public TestClient(Message [] buffer)
	{
		this.buffer = buffer;
	}
	
	public synchronized String [] getData()
	{
		//System.out.println("READING in index: " + LoginScreen.indBuff);
		String [] vys = buffer[0].getAll();
		//System.out.println("TC " + Arrays.toString(buffer));
		for (int i = 0; i < buffer.length - 1; i++)
		{
			buffer[i].setAll(buffer[i+1].getAll());			
		}
		//System.out.println("TC "+ Arrays.toString(buffer));
		LoginScreen.indBuff--;
		return vys;
	}
	
	public void chatWithServer()
	{
		String zprava = "";
		receive.start();		
		System.out.println("---Start of chat---");
		while(true)
		{
			boolean done = true;
			while(done)
			{
				zprava = sc.nextLine();
				if(zprava.length() > 50)
				{
					System.out.println("Zprava je moc dlouha!(Max. 50)");
				}
				else
				{
					done = false;
				}
			}
			
			zprava = "$" + zprava + "#\n";
			
			try
			{
				bw.write(zprava.getBytes());
				if(zprava.equals("$!cc!#\n"))
				{
					receive.done = false;
					break;
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("----End of chat----");
	}

	public void lobbyState()
	{
		while(true)
		{
			try
			{
				String [] message = new String[2];
				System.out.println("CEKAM NA PRIJMUTI");
				System.out.println("READING before index: " + LoginScreen.indBuff);
				synchronized(buffer[LoginScreen.indBuff])
				{
					//System.out.println("READING before index: " + LoginScreen.indBuff);
					buffer[LoginScreen.indBuff].wait();
					//System.out.println("READING before getData() index: " + LoginScreen.indBuff);
					message = getData();
					//System.out.println(message[0] + ", " + message[1]);
					//System.out.println("READING after index: " + LoginScreen.indBuff);
				}
				String send = "";
				switch(stavHrace)
				{
				case 2:if(message[0].equals("GUI"))
					   {
						   send = message[1];
						   bw.write(send.getBytes());
						   System.out.println("GUI---"+ message[1] + "---");
					   }
					   else if(message[0].equals("SERVER"))
					   {
						   System.out.println("SERVER---"+ message[1] + "---");
						   defineSituationLobbyServer(message[1]);
					   }
					   else
					   {
						   System.out.println("Chyba parametry pro rozliseni prijmuti a poslani zpravy!");
					   }
					   break;
				default:System.out.println("Chyba, stav hrace spatne definovany!");
				        break;
				}
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
				System.out.println("Error na uspani vlakno pro cekani na prijem zpravy!");
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public int defineSituationLobbyServer(String instrukce)
	{
		int indV = instrukce.indexOf('!');
		if(indV == -1)//neni vykricnik ve zprava
		{
			System.out.println("Prijmuta zprava neobsahuje parsovaci znak '!'");
			return 1;
		}
		String front = instrukce.substring(0, indV);
		String back = instrukce.substring(indV + 1);
		
		if(front.equals("refresh"))
		{
			String [] pole = Assets.separeter(back, '!');
			String [] nazvyLobbyin = Assets.separeter(pole[1], ',');
			String [] lobbyLidi = Assets.separeter(pole[2], ',');
			int pocetLobbyin = Integer.parseInt(pole[0]);//Pocet lobbyin
			if(LobbyScreen.lobbies == null)
			{
				LobbyScreen.lobbies = new Lobby[pocetLobbyin];
				for(int i = 0; i < pole.length; i++)
				{
					LobbyScreen.lobbies[i] = new Lobby(nazvyLobbyin[i]);//Nazev konkretni lobby
					LobbyScreen.lobbies[i].pocetHracu = Integer.parseInt(lobbyLidi[i]);//Pocet hracu v konkretni lobby
				}
			}
			else
			{
				Lobby [] noveLobby = new Lobby[pocetLobbyin];
				for(int i = 0; i < noveLobby.length; i++)
				{
					noveLobby[i] = new Lobby(nazvyLobbyin[i]);//Nazev konkretni lobby
					noveLobby[i].pocetHracu = Integer.parseInt(lobbyLidi[i]);//Pocet hracu v konkretni lobby 
				}
				LobbyScreen.lobbies = noveLobby;
			}
			return 0;
		}
		else if(front.equals("create"))
		{
			String [] input = Assets.separeter(back, '!');
			if(input[0].equals("accept"))
			{
				//Vytvorit lobby, o kterou jsem si puvodne zazadal, prevzit hodnoty nekde z GUI				
			}
			else if(input[0].equals("decline"))
			{
				int cis = Integer.parseInt(input[1]);
				switch(cis)
				{
				case 1://Nelze vytvorit lobby, jelikoz nazev se shoduje s jiz vytvorenou lobby!
					   break;
				default://Nejaka jina chyba nedefinovana zde!
						break;
				}
			}
			else
			{
				System.out.println("Spatne poslany nejaky jiny parametr " + back + "!");
				return 1;
			}
			return 0;
		}
		else if(front.equals("join"))
		{
			String [] input = Assets.separeter(back, '!');
			if(input[0].equals("accept"))
			{
				//Pripojit se do lobby, o kterou jsem si predtim zazadal, prevzit hodnoty				
			}
			else if(input[0].equals("decline"))
			{
				int cis = Integer.parseInt(input[1]);
				switch(cis)
				{
				case 1://Index hrace, nebo [server hodnota] lobby neni ve stanovenych mezich!
					   break;
				case 2://Na zadanem indexu [server hodnota] neni zadny hrac!
					   break;
				case 3://Neni jiz misto v lobby
					   break;
				case 4://Chyba, hrac nebyl nalezen v seznamu hracu!
					   break;
				default://Nejaka jina chyba nedefinovana zde!
						break;
				}
			}
			else
			{
				System.out.println("Spatne poslany nejaky jiny parametr " + back + "!");
				return 1;
			}
			return 0;
		}
		else
		{
			System.out.println("Neznama zprava " + instrukce + "!");
			return 0;
		}
	}
	
	public int defineSituationLogin(String instrukce)
	{
		int indV = instrukce.indexOf('!');
		if(indV == -1)//neni vykricnik ve zprava
		{
			System.out.println("Prijmuta zprava neobsahuje parsovaci znak '!'");
			System.exit(1);
		}
		String front = instrukce.substring(0, indV);
		String back = instrukce.substring(indV + 1);
		
		if(front.equals("login"))
		{
			indV = back.indexOf('!');
			if(indV == -1)//neni vykricnik ve zprava
			{
				System.out.println("Prijmuta zprava po 'login' neobsahuje parsovaci znak '!'");
				System.exit(1);
			}
			
			String back2 = back.substring(0, indV);
			String back3 = back.substring(indV + 1);
			if(back2.equals("accept"))
			{
				System.out.println("Byl jste uspesne prihlasen!");
				new Thread(Monopoly.LoginScreen.rc, "GET FROM SERVER").start();
				stavHrace = 2;
				Monopoly.LoginScreen.game.setScreen(Monopoly.LobbyScreen);
				return 1;
			}
			else if(back2.equals("decline"))
			{
				char zn = back3.charAt(0);
				switch(zn)
				{
				case '1':System.out.println("Login/Heslo je spatne zapsano nebo neexistuje!");
						 return 1;
				case '2':System.out.println("Zaslana zprava neobsahuje paramater 'login'!");
					 	 System.exit(1);
					 	 break;
				case '3':System.out.println("Zadany uzivatel je uz prihlasen!\nZadejte udaje znovu.");
						 return 1;
				default:System.out.println("Zadana zprava neobsahuje validni parametr!");
						break;
				}
			}
			else
			{
				System.out.println("Zadana zprava neobsahuje validni parametr!");
			}
		}
		else
		{
			System.out.println("Zadana zprava neobsahuje validni parametr!");
		}
		return 0;
	}
	
	@Override
	public void run()
	{
		/*
		System.out.print("Zadejte IP serveru:");
		String ip = sc.nextLine();
		
		System.out.print("Zadejte port:");
		int port = 0;
		try
		{
			port = Integer.parseInt(sc.nextLine());			
		}
		catch(NumberFormatException n)
		{
			System.out.println("Spatne zadany parametr pro port na server!");
			System.out.println("EXIT");
			System.exit(1);
		}
		*/
		
		try
		{
			socket = new Socket("localhost" , 8192);
			InetAddress adresa = socket.getInetAddress();
			System.out.println("Pripojuju se na : "+adresa.getHostAddress()+" se jmenem : "+adresa.getHostName());
			
			//POSLANI, zacatek zpravy $ a konec #
			bw = socket.getOutputStream();
			bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String z = "$login!" + LoginScreen.login + "!" + LoginScreen.pass + "#\n";
			System.out.println("Posilam zpravu serveru: " + z);
			bw.write(z.getBytes());
			
			//PRIJEM
			String message = (String)bf.readLine();
			int ren = defineSituationLogin(message);
			if(ren == 1)
			{
				lobbyState();
			}
			
			bw.close();
			bf.close();
			receive.join();
			System.out.println("ZAVIRAM IN/OUT a ted i socket samotny");
			socket.close();
		}
		catch(UnknownHostException uhe)
		{
			System.out.println("Zadany server neexistuje!");
		}
		catch(ConnectException ce)
		{
			System.out.println("Server je nedostupny, zkuste to prosim pozdeji!");
		}
		catch(InterruptedException ie)
		{
			ie.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		System.out.println("EXIT");
	}
}
