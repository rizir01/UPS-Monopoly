package com.mygdx.seme;

public class Lobby
{
	String [] hraci;
	
	int pocetHracu;
	
	String lobbyName;
	
	public Lobby(String name)
	{
		lobbyName = name;
		pocetHracu = 0;
		hraci = new String[4];
	}
	
	public void addPlayer(String jmeno)
	{
		boolean nenasel = true;
		for(int i = 0; i < hraci.length; i++)
		{
			if(hraci[i] != null)
			{
				nenasel = false;
				hraci[i] = jmeno;
			}
		}
		if(nenasel)
		{
			System.out.println("Chyba, seznam jiz plny hracu!");
		}
		pocetHracu++;
	}
	
	public void removePlayer(int index)
	{
		if(index >= 4)
		{
			System.out.println("Chyba, index moc velky!");
		}
		hraci[index] = null;
		pocetHracu--;
	}
	
	public void removePlayer(String jmeno)
	{
		boolean nenasel = true;
		for(int i = 0; i < hraci.length; i++)
		{
			if(hraci[i].equals(jmeno))
			{
				nenasel = false;
				hraci[i] = null;
			}
		}
		if(nenasel)
		{
			System.out.println("Chyba, v seznamu neni hrac s nazvem " + jmeno + "!");
		}
		pocetHracu--;
	}
}
