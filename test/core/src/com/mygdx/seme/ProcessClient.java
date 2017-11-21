package com.mygdx.seme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ProcessClient extends Thread
{
	public Socket s;
	int id;
	
	public ProcessClient(Socket s)
	{
		this.s = s;
	}
	
	public void run()
	{
		
		BufferedReader in;
		PrintWriter out;
		try
		{
			in = new BufferedReader(
					new InputStreamReader(s.getInputStream()));
			out = new PrintWriter(s.getOutputStream(), true);
			String input = in.readLine();
			String [] vstup = Server.separeter(input, '!');
			input = Server.checkLoginPass(vstup[0], vstup[1]);
			if(input.substring(0, 2).equals("ok"));
			{
				System.out.println(input);
				int ind = Integer.parseInt(input.substring(3));
				this.id = Integer.parseInt(Server.ServerClientsList[ind][3]);
				System.out.println("ProcessClient - Uzivatel prihlasen " + id);
			}
			if(input.equals("lf"))
			{
				System.out.println("ProcessClient - Prihasovaci jmeno je spatne!!");
			}
			if(input.equals("pf"))
			{
				System.out.println("ProcessClient - Heslo neni spravne!!");
			}
			out.write("done\n");
		}
		catch(NumberFormatException nfe)
		{
			nfe.printStackTrace();
			System.out.println("V Process Client v parasovani na cislo doslo k chybe!");
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.out.println("I/O error prenosu ProcessClient " + id);
		}
	}
	
	
}
