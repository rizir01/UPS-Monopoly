package com.mygdx.seme;

public class Card
{
	private static int countID = 1;
	private String nazev;
	private int id;
	private int ownedPlayerID;
	
	public Card(String name)
	{
		nazev = name;
		id = countID++;
		ownedPlayerID = -1;
	}
	
	public void action(Player hrac, Player [] hraci)
	{
		System.out.println("Card - action");
	}
	
	public String getNazev()
	{
		return nazev;
	}

	public void setNazev(String nazev)
	{
		this.nazev = nazev;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public int getOwnPlayerID()
	{
		return ownedPlayerID;
	}

	public void setOwnPlayerID(int id)
	{
		this.ownedPlayerID = id;
	}

	public String toString()
	{
		return "<Class: " + getClass().getName() + ",id: " + getId() + ",nazev: " + getNazev() + ">";
	}
}
