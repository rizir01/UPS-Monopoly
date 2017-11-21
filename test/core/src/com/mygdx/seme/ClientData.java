package com.mygdx.seme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientData extends Thread
{
	
	static String input;
	static Socket soket;
	
	public ClientData()
	{
		try
		{
			soket = new Socket("localhost", 12345);
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
			System.out.println("Neznamy host!!");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("Input/Outpu failed!!");
		}
	}
	
	@Override
	public void run()
	{
		BufferedReader in;
		PrintWriter out;
		try
		{
			in = new BufferedReader(
					new InputStreamReader(soket.getInputStream()));
			
			out = new PrintWriter(soket.getOutputStream(), true);
			//Client poslal zpravu servru
			out.println(LoginScreen.login + "!" + LoginScreen.pass + "!");
			System.out.println("ClientData - Client poslal data o prihlaseni");
			String input = in.readLine();
			System.out.println("ClientData - Client dostal od serveru:" + input);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.out.println("Chyba behem prijmani a odesilani zprav u klienta");
		}
	}
}
