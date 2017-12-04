package com.mygdx.seme;

import java.util.Arrays;

public class Lobby
{
	private String [] hraci;
	
	private int pocetHracu;
	
	String lobbyName;
	
	public Lobby(String name)
	{
		lobbyName = name;
		pocetHracu = 0;
		hraci = new String[4];
	}
	
	public void addPlayer(String jmeno)
	{
		System.out.println(Arrays.toString(hraci));
		boolean nenasel = true;
		for(int i = 0; i < hraci.length; i++)
		{
			if(hraci[i] == null)
			{
				nenasel = false;
				hraci[i] = jmeno;
				break;
			}
		}
		if(nenasel)
		{
			System.out.println("Chyba, seznam jiz plny hracu!");
			return;
		}
		pocetHracu++;
		System.out.println(Arrays.toString(hraci));
		System.out.println(pocetHracu);
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
		System.out.println(Arrays.toString(hraci));
		boolean nenasel = true;
		for(int i = 0; i < hraci.length; i++)
		{
			if(hraci[i] != null)
			{
				if(hraci[i].equals(jmeno))
				{
					nenasel = false;
					hraci[i] = null;
					break;
				}				
			}
		}
		if(nenasel)
		{
			System.out.println("Chyba, v seznamu neni hrac s nazvem " + jmeno + "!");
			return;
		}
		pocetHracu--;
		System.out.println(Arrays.toString(hraci));
		System.out.println(pocetHracu);
	}
	
	public int getPocetHrau()
	{
		return pocetHracu;
	}
	
	public void setPocetHracu(int hraci)
	{
		pocetHracu = hraci;
	}
}
