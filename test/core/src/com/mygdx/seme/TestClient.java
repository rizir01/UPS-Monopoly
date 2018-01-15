package com.mygdx.seme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

public class TestClient extends Thread
{
	/**
	 * Definuje, v jakem stavu se hrac nachazi,
	 * tedy jestli je v lobby, ve hre atd.
	 * 2 - lobby
	 * 3 - hra
	 * 
	 */
	int stavHrace = 1;
	
	TestClientRecieve tcr;
	
	//0-login, 1-register pri spusteni vlakna
	int typeLogin;
	
	static Socket socket;
	
	static OutputStream bw;
	
	static BufferedReader bf;
	
	static Scanner sc = new Scanner(System.in);
	
	public TestClient(TestClientRecieve tcr)
	{
		this.tcr = tcr;
	}
	
	public synchronized void sendMessageToServer(String mess)
	{
		try
		{
			System.out.println("Posilam zpravu SERVERU: " + mess);
			bw.write(mess.getBytes());
		}catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void lobbyState()
	{
		while(true)
		{
			try
			{
				String message = tcr.getMessage();
				String send = "";
				
				System.out.println("extr. zp. : " + message + " ve stavu " + stavHrace);
				switch(stavHrace)
				{
				case 2:if(message.charAt(0) == 'G')
						{
							send = message.substring(2);
							bw.write(send.getBytes());
							System.out.println("GUI---"+ send + "---");
						}
						else if(message.charAt(0) == 'S')
						{
							String internal = message.substring(2);
							System.out.println("SERVER---"+ internal + "---");
							defineSituationLobbyServer(internal);
						}
						else
						{
							System.out.println("Chyba parametry pro rozliseni prijmuti a poslani zpravy!(Stav 2 hrace - Lobby)");
						}
						break;
				case 3:if(message.charAt(0) == 'G')
						{
							send = message.substring(2);
							bw.write(send.getBytes());
							System.out.println("GUI---"+ send + "---");
						}
						else if(message.charAt(0) == 'S')
						{
							String internal = message.substring(2);
							System.out.println("SERVER---"+ internal + "---");
							defineSituationGame(internal);
						}
						else
						{
							System.out.println("Chyba parametry pro rozliseni prijmuti a poslani zpravy(Stav 3 hrace - Hra)!");
						}
						break;
				case 4:if(message.charAt(0) == 'G')
						{
							send = message.substring(2);
							bw.write(send.getBytes());
							System.out.println("GUI---"+ send + "---");
						}
						else if(message.charAt(0) == 'S')
						{
							String internal = message.substring(2);
							System.out.println("SERVER---"+ internal + "---");
							defineSituationEnd(internal);
						}
						else
						{
							System.out.println("Chyba parametry pro rozliseni prijmuti a poslani zpravy(Stav 3 hrace - Hra)!");
						}
						break;
				default:System.out.println("Chyba, stav hrace spatne definovany!");
						break;
				}
			}
			catch(SocketException se)
			{
				//Padnul server nebo neni dosazitelny
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
	
	public int defineSituationEnd(String instrukce)
	{
		int indV = instrukce.indexOf('!');
		if(indV == -1)//neni vykricnik ve zprava
		{
			System.out.println("Prijmuta zprava neobsahuje parsovaci znak '!'");
			return 1;
		}
		String front = instrukce.substring(0, indV);
		String back = instrukce.substring(indV + 1);
		
		if(front.equals("return"))
		{
			String [] pole = Assets.separeter(back, '!');
			if(pole[0].equals("accept"))
			{
				stavHrace = 2;
				Monopoly.EndScreen.hide = true;
				LobbyScreen.drawAllInfo = true;
				Monopoly.EndScreen.game.setScreen(Monopoly.LobbyScreen);
				return 0;
			}
			else if(pole[0].equals("decline"))
			{				
				System.out.println("Cislo chyby u return je " + pole[1]);
				return 0;
			}
			else
			{
				System.out.println("Spatne poslany nejaky jiny parametr " + back + "!");
				return 1;
			}
		}
		else if(front.equals("discon"))
		{
			String [] pole = Assets.separeter(back, '!');
			if(pole[0].equals("accept"))
			{
				Monopoly.EndScreen.hide = false;
				Monopoly.EndScreen.game.setScreen(Monopoly.LoginScreen);
				return 0;
			}
			else if(pole[0].equals("decline"))
			{				
				System.out.println("Cislo chyby u return je " + pole[1]);
				return 0;
			}
			else
			{
				System.out.println("Spatne poslany nejaky jiny parametr " + back + "!");
				return 1;
			}
		}
		else
		{
			System.out.println("Neznama zprava " + instrukce);
			return 0;
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
			String [] lockLob = Assets.separeter(pole[3], ',');
			int pocetLobbyin = Integer.parseInt(pole[0]);//Pocet lobbyin
			int l;
			if(LobbyScreen.lobbies == null)
			{
				LobbyScreen.lobbies = new Lobby[pocetLobbyin];
				for(int i = 0; i < pocetLobbyin; i++)
				{
					try
					{
						l = Integer.parseInt(lockLob[i]);						
					}
					catch(NumberFormatException nfe)
					{
						System.out.println("Predane paramtery pro lock lobby jsou spatne zadane \n"
								+ Arrays.toString(lockLob));
						return 1;
					}
					LobbyScreen.lobbies[i] = new Lobby(nazvyLobbyin[i], l);//Nazev konkretni lobby
					LobbyScreen.lobbies[i].setPocetHracu(Integer.parseInt(lobbyLidi[i]));//Pocet hracu v konkretni lobby
				}
			}
			else
			{
				Lobby [] noveLobby = new Lobby[pocetLobbyin];
				for(int i = 0; i < noveLobby.length; i++)
				{
					try
					{
						l = Integer.parseInt(lockLob[i]);						
					}
					catch(NumberFormatException nfe)
					{
						System.out.println("Predane paramtery pro lock lobby jsou spatne zadane \n"
								+ Arrays.toString(lockLob));
						return 1;
					}
					noveLobby[i] = new Lobby(nazvyLobbyin[i], l);//Nazev konkretni lobby
					noveLobby[i].setPocetHracu(Integer.parseInt(lobbyLidi[i]));//Pocet hracu v konkretni lobby 
				}
				LobbyScreen.lobbies = noveLobby;
			}
			float delkaLobbyin = LobbyScreen.lobbies.length * 130;
			float pomer = 520 / delkaLobbyin;
			if(pomer < 1)
			{
				LobbyScreen.slideBarHeight = 520 * pomer;				
			}
			return 0;
		}
		else if(front.equals("create"))
		{
			String [] input = Assets.separeter(back, '!');
			if(input[0].equals("accept"))
			{
				//Vytvorit lobby, o kterou jsem si puvodne zazadal, prevzit hodnoty nekde z GUI
				int pocetLobbyin = LobbyScreen.lobbies.length;
				Lobby [] noveLobby = new Lobby[pocetLobbyin + 1];
				for(int i = 0; i < pocetLobbyin; i++)
				{
					noveLobby[i] = LobbyScreen.lobbies[i];
				}
				noveLobby[pocetLobbyin] = new Lobby(Monopoly.LobbyScreen.lobbyName, 0);
				LobbyScreen.lobbies = noveLobby;
				
				//Priradit do nove vytvorene lobby hrace, ktery zavolal create
				int pocetL = 1;
				LobbyScreen.selectedLobby = pocetLobbyin;
				Monopoly.LobbyScreen.currentLobby = new String[4];
				for(int i = 0; i < pocetL; i++)
				{
					LobbyScreen.lobbies[pocetLobbyin].addPlayer(LoginScreen.login);
					Monopoly.LobbyScreen.currentLobby[i] = LoginScreen.login;
				}
				
				float delkaLobbyin = LobbyScreen.lobbies.length * 130;
				float pomer = 520 / delkaLobbyin;
				System.out.println(pomer);
				if(pomer < 1)
				{
					LobbyScreen.slideBarHeight = 520 * pomer;				
				}
				
				LobbyScreen.drawAllInfo = true;
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
				String [] names = Assets.separeter(input[2], ',');
				//Pripojit se do lobby, o kterou jsem si predtim zazadal, prevzit hodnoty
				int pocetL = Integer.parseInt(input[1]);
				Monopoly.LobbyScreen.currentLobby = new String[4];
				for (int i = 0; i < pocetL; i++)
				{
					LobbyScreen.lobbies[LobbyScreen.selectedLobby].addPlayer(names[i]);
					Monopoly.LobbyScreen.currentLobby[i] = names[i];
				}
				LobbyScreen.drawAllInfo = true;
			}
			else if(input[0].equals("decline"))
			{
				int cis = Integer.parseInt(input[1]);
				switch(cis)
				{
				case 1:System.out.println("Index hrace, nebo [server hodnota] lobby neni ve stanovenych mezich!");
					   break;
				case 2:System.out.println("Na zadanem indexu [server hodnota] neni zadny hrac!");
					   break;
				case 3:System.out.println("Neni jiz misto v lobby");
					   break;
				case 4:System.out.println("Chyba, hrac nebyl nalezen v seznamu hracu!");
					   break;
				default:System.out.println("Nejaka jina chyba nedefinovana zde!");
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
		else if(front.equals("lobby"))
		{
			String [] input = Assets.separeter(back, '!');
			if(input[0].equals("add"))
			{
				LobbyScreen.lobbies[LobbyScreen.selectedLobby].addPlayer(input[1]);
				for(int i = 0; i < 4; i++)
				{
					if(Monopoly.LobbyScreen.currentLobby[i] == null)
					{
						Monopoly.LobbyScreen.currentLobby[i] = input[1];
						break;
					}
				}
			}
			else if(input[0].equals("rem"))
			{
				LobbyScreen.lobbies[LobbyScreen.selectedLobby].removePlayer(input[1]);
				for(int i = 0; i < 4; i++)
				{
					if(Monopoly.LobbyScreen.currentLobby[i].equals(input[1]))
					{
						Monopoly.LobbyScreen.currentLobby[i] = null;
						break;
					}
				}
			}
			else
			{
				System.out.println("Spatne poslany nejaky jiny parametr " + back + "!");
				return 1;
			}
			return 0;
		}
		else if(front.equals("ready"))
		{
			String [] input = Assets.separeter(back, '!');
			if(input[0].equals("add"))
			{
				LobbyScreen.lobbies[LobbyScreen.selectedLobby].addReady(input[1]);
			}
			else if(input[0].equals("rem"))
			{
				LobbyScreen.lobbies[LobbyScreen.selectedLobby].removeReady(input[1]);
			}
			else if(input[0].equals("accept"))
			{
				System.out.println("READY - probehlo uspesne");
			}
			else if(input[0].equals("error"))
			{
				System.out.println("Doslo k chybe prenosu cislo " + input[1]);
				return 1;
			}
			else
			{
				System.out.println("Spatne poslany nejaky jiny parametr " + back + "!");
				return 1;
			}
			return 0;
		}
		else if(front.equals("game"))
		{
			String [] input = Assets.separeter(back, '!');
			if(input[0].equals("start"))
			{
				//Nastavit casomiru
				LobbyScreen.countDown = true;
				LobbyScreen.timeC = System.currentTimeMillis();
				int pocH = LobbyScreen.lobbies[LobbyScreen.selectedLobby].getPocetHrau();
				System.out.println("Vytvarim waiting lobby s poctem hracu: " + pocH);
				//System.out.println("pocet hracu v lobby: " + pocH);
				GameScreen.waited = new int[pocH];
			}
			else if(input[0].equals("stop"))
			{
				//Zastavit casomiru a resetovat
				LobbyScreen.countDown = false;
				LobbyScreen.timeC = 0;
			}
			else
			{
				System.out.println("Spatne poslany nejaky jiny parametr " + back + "!");
				return 1;
			}
			return 0;
		}
		else if(front.equals("pre"))
		{
			String [] info = Assets.separeter(back, '|');
			String [] info2 = Assets.separeter(back, '!');
			if(info2[0].equals("add"))
			{
				try
				{
					int poz = Integer.parseInt(info2[1]);
					if(poz >= GameScreen.waited.length)
					{
						System.out.println("Zadany index je spatne zadany, moc velky pro waiting room!");
						return 1;
					}
					GameScreen.waited[poz] = 1;
				}
				catch(NumberFormatException nfe)
				{
					System.out.println("Zprava mela spatne zadany parameteru indexu pro\n"
							+ "nastaveni ve waiting room " + info[1]);
					return 1;
				}
			}
			else
			{
				System.out.println("Error - predana zprava neobsahuje zadny z parametru, ktere by\n"
						+ "program znal " + instrukce);
				return 1;
			}
			return 0;
		}
		else
		{
			System.out.println("Neznama zprava " + instrukce);
			return 0;
		}
	}
	
	public int defineSituationGame(String instrukce)
	{
		int indV = instrukce.indexOf('!');
		if(indV == -1)//neni vykricnik ve zprava
		{
			System.out.println("Prijmuta zprava neobsahuje parsovaci znak '!'");
			return 1;
		}
		String front = instrukce.substring(0, indV);
		String back = instrukce.substring(indV + 1);
		
		if(front.equals("game"))
		{
			String [] info = Assets.separeter(back, '!');
			if(info.length == 0)
			{
				System.out.println("Prijmuta zprava v oblasti informaci neobsahuje parsovaci znak '!'");
				return 1;
			}
			
			if(info[0].equals("start"))
			{
				String [] pom = Assets.separeter(info[1], ',');
				GameScreen.startRound = false;
				Table.transmission("s!" + pom[0] + "!" + pom[1] + "!");
			}
			else if(info[0].equals("end"))
			{
				Table.transmission("e!");
			}
			else if(info[0].equals("again"))
			{
				Table.transmission("n!");
			}
			else if(info[0].equals("aukce"))
			{
				System.out.println(Arrays.toString(info));
				if(info.length <= 1)
				{
					System.out.println("Prijmuta zprava v oblasti informaci neobsahuje parsovaci znak '!'" + info[1]);
					return 1;
				}
				
				if(info[1].equals("done"))
				{
					Table.transmission("a!k!");
				}
				else if(info[1].equals("fail"))
				{
					Table.transmission("a!f!");
				}
				else if(info[1].equals("max"))
				{
					Table.transmission("a!a!" + info[2] + "!");
				}
				else if(info[1].equals("next"))
				{
					Table.transmission("a!n!");
				}
				else if(info[1].equals("end"))
				{
					Table.transmission("a!e!");
				}
				else if(info[1].equals("start"))
				{
					try
					{
						//int poz1 = Integer.parseInt(info[2]);
						GameScreen.startRound = false;//!!
						GameScreen.aukce = true;
						GameScreen.aukcePozice = GameScreen.pozice;
						GameScreen.bids = new int[GameScreen.screenHraci.length];
						GameScreen.folds = 0;
						GameScreen.aukceMax = 0;
						GameScreen.bid = GameScreen.aukceMax;
					}
					catch(NumberFormatException nfe)
					{
						System.out.println("Error-ve zprave neni cislo u pozici aukce!");
						return 1;
					}
				}
				else
				{
					System.out.println("Error - predana zprava neobsahuje zadny z parametru, ktere by\n"
							+ "program znal " + instrukce);
					return 1;
				}
				
			}
			else if(info[0].equals("get"))
			{
				Table.transmission("g!" + info[1] + "!" + info[2] + "!");
			}
			else if(info[0].equals("pay"))
			{
				Table.transmission("p!" + info[1] + "!" + info[2] + "!");
			}
			else if(info[0].equals("buy"))
			{
				if(info[1].equals("fail"))
				{
					System.out.println("Nedostatek penez na ucte, vyberte moznost AUKCE!");
				}
				else
				{
					Table.transmission("b!" + info[1] + "!" + info[2] + "!");	
				}
			}
			else if(info[0].equals("chest"))
			{
				Table.transmission("d!" + info[1]);
			}
			else if(info[0].equals("chance"))
			{
				Table.transmission("c!" + info[1]);
			}
			else if(info[0].equals("gojail"))
			{
				Table.transmission("j!");
			}
			else if(info[0].equals("lose"))
			{
				Table.transmission("l!"+ info[1] + "!");
			}
			else if(info[0].equals("win"))
			{
				stavHrace = 4;
				GameScreen.winName = info[1];
				Monopoly.GameScreen.hide = true;
				Monopoly.LoginScreen.game.setScreen(Monopoly.EndScreen);
			}
			else
			{
				System.out.println("Error - predana zprava neobsahuje zadny z parametru, ktere by\n"
						+ "program znal " + instrukce);
				return 1;
			}
			return 0;
		}
		else if(front.equals("pre"))
		{
			String [] info = Assets.separeter(back, '|');
			String [] info2 = Assets.separeter(back, '!');
			if(info2[0].equals("add"))
			{
				try
				{
					int poz = Integer.parseInt(info2[1]);
					if(poz >= GameScreen.waited.length)
					{
						System.out.println("Zadany index je spatne zadany, moc velky pro waiting room!");
						return 1;
					}
					GameScreen.waited[poz] = 1;
				}
				catch(NumberFormatException nfe)
				{
					System.out.println("Zprava mela spatne zadany parameteru indexu pro\n"
							+ "nastaveni ve waiting room " + info[1] + "!");
					return 1;
				}
			}
			else if(info[0].equals("start"))
			{
				Assets.setGameStatusFull(info[1]);
				GameScreen.waiting = false;
			}
			else
			{
				System.out.println("Error - predana zprava neobsahuje zadny z parametru, ktere by\n"
						+ "program znal " + instrukce);
				return 1;
			}
			return 0;
		}
		else if(front.equals("leave"))
		{
			String [] info3 = Assets.separeter(back, '!');
			if(info3[0].equals("accept"))
			{
				Monopoly.GameScreen.hide = true;
				Monopoly.LoginScreen.game.setScreen(Monopoly.LobbyScreen);
			}
			else if(info3[0].equals("decline"))
			{
				System.out.println("Nelze opustit hru, z duvodu chyby na serveru!\n"
						+ "Pravdepodbne chyba v nalezeni hrace/lobby/hry!");
				return 1;
			}
			else
			{
				System.out.println("Pro leave parametr, nebyl uveden spravny podparameter - " + info3[0] + "!");
				return 1;
			}
			return 0;
		}
		else if(front.equals("left"))
		{
			String [] info4 = Assets.separeter(back, '!');
			for(int i = 0; i < GameScreen.screenHraci.length; i++)
			{
				if(GameScreen.screenHraci[i].getName().equals(info4[0]))
				{
					System.out.println("Hrac " + GameScreen.screenHraci[i].getName() + " opustil hru!");
					Table.transmission("l!"+ i + "!");
					break;
				}			
			}
			for (int i = 0; i < Monopoly.LobbyScreen.currentLobby.length; i++)
			{
				if(Monopoly.LobbyScreen.currentLobby[i] != null)
				{
					if(Monopoly.LobbyScreen.currentLobby[i].equals(info4[0]))
					{
						System.out.println("Smazan v currentLobby " + i);
						Monopoly.LobbyScreen.currentLobby[i] = null;
						break;
					}
				}
			}		
			System.out.println("Hrac s nazvem "+info4[0]+" odesel!");
			return 0;
		}
		else
		{
			System.out.println("Neznama zprava |" + instrukce + "|!");
			return 1;
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
						 break;
				case '2':System.out.println("Zaslana zprava neobsahuje paramater 'login'!");
					 	 break;
				case '3':System.out.println("Zadany uzivatel je uz prihlasen!\nZadejte udaje znovu.");
						 break;
				default:System.out.println("Zadana zprava neobsahuje validni parametr!");
						break;
				}
			}
			else
			{
				System.out.println("Zadana zprava neobsahuje validni parametr!");
			}
		}
		else if(front.equals("register"))
		{
			indV = back.indexOf('!');
			if(indV == -1)//neni vykricnik ve zprava
			{
				System.out.println("Prijmuta zprava po 'register' neobsahuje parsovaci znak '!'");
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
				case '1':System.out.println("Hrac s timto loginem jiz existuje!");
						 break;
				case '2':System.out.println("Zaslana zprava neobsahuje paramater 'register' nebo 'login!");
					 	 break;
				default:System.out.println("Zadana zprava neobsahuje validni parametr" + zn + "!");
						break;
				}
			}
			else
			{
				System.out.println("Zadana zprava neobsahuje validni parametr back2!");
			}
		}
		else
		{
			System.out.println("Zadana zprava neobsahuje validni parametr front!");
		}
		return 0;
	}
	
	@Override
	public void run()
	{
		try
		{
			socket = new Socket(Monopoly.host, Monopoly.port);
			InetAddress adresa = socket.getInetAddress();
			System.out.println("Pripojuju se na : "+adresa.getHostAddress()+" se jmenem : "+adresa.getHostName());
			
			//POSLANI, zacatek zpravy $ a konec #
			bw = socket.getOutputStream();
			bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			String typeS = "";
			if(typeLogin == 0)
			{
				typeS = "login";
			}
			else
			{
				typeS = "register";
			}
			String z = "$" + typeS + "!" + LoginScreen.login + "!" + LoginScreen.pass + "#";
			//System.out.println("Posilam zpravu serveru: " + z);
			bw.write(z.getBytes());
			
			//PRIJEM
			String message = (String)bf.readLine();
			System.out.println(message);
			int ren = defineSituationLogin(message);
			if(ren == 1)
			{
				lobbyState();
			}
			
			bw.close();
			bf.close();
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
		catch (IOException e)
		{
			e.printStackTrace();
		}
		System.out.println("EXIT");
	}

}
