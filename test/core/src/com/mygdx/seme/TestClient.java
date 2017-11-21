package com.mygdx.seme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TestClient extends Thread
{
	static Socket socket;
	
	static OutputStream bw;
	
	static BufferedReader bf;
	
	static Scanner sc = new Scanner(System.in);
	
	static TestClientRecieve receive = new TestClientRecieve();
	
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
	
	public int defineSituation(String instrukce)
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
				receive.start();
				Monopoly.LoginScreen.game.setScreen(Monopoly.LobbyScreen);
		        //chatWithServer();
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
			defineSituation(message);
			
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
