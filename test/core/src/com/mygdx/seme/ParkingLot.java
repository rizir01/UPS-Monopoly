package com.mygdx.seme;

public class ParkingLot extends Card
{

	public ParkingLot(String name)
	{
		super(name);
	}
	
	public void action(Player hrac, Player [] hraci)
	{
		System.out.println(hrac.getName() + " stoji na parkovisti");
	}
	
}
