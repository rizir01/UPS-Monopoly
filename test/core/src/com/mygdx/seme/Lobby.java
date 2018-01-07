package com.mygdx.seme;

import java.util.Arrays;

public class Lobby
{
	private String [] hraci;
	
	private boolean [] ready;
	
	private int pocetHracu;
	
	String lobbyName;
	
	public Lobby(String name)
	{
		lobbyName = name;
		pocetHracu = 0;
		hraci = new String[4];
		ready = new boolean[4];
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
		if(index < 0 && index >= 4)
		{
			System.out.println("Chyba, index neni ve stanovenych mezich <0,4)!");
			return;
		}
		hraci[index] = null;
		ready[index] = false;
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
					ready[i] = false;
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
	
	public void addReady(String jmeno)
	{
		boolean nenasel = true;
		for (int i = 0; i < hraci.length; i++)
		{
			if(hraci[i] != null)
			{
				if(hraci[i].equals(jmeno))
				{
					nenasel = false;
					ready[i] = true;
					break;
				}				
			}
		}
		if(nenasel)
		{
			System.out.println("Chyba, v seznamu neni hrac s nazvem " + jmeno + "!");
			return;
		}
	}
	
	public void removeReady(int index)
	{
		if(index < 0 && index >= 4)
		{
			System.out.println("Chyba, index neni ve stanovenych mezich <0,4)!");
			return;
		}
		ready[index] = false;
	}
	
	public void removeReady(String jmeno)
	{
		boolean nenasel = true;
		for (int i = 0; i < hraci.length; i++)
		{
			if(hraci[i] != null)
			{
				if(hraci[i].equals(jmeno))
				{
					nenasel = false;
					ready[i] = false;
					break;
				}				
			}
		}
		if(nenasel)
		{
			System.out.println("Chyba, v seznamu neni hrac s nazvem " + jmeno + "!");
			return;
		}
	}
	
	public int getPocetHrau()
	{
		return pocetHracu;
	}
	
	public void setPocetHracu(int hraci)
	{
		pocetHracu = hraci;
	}
	
	public String [] getHraci()
	{
		return hraci;
	}
	
	public boolean[] getReady()
	{
		return ready;
	}

	public void setReady(boolean[] ready)
	{
		this.ready = ready;
	}
}
