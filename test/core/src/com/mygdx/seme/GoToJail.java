package com.mygdx.seme;

public class GoToJail extends Card
{

	public GoToJail(String name)
	{
		super(name);
	}
	
	public void action(Player hrac, Player [] hraci)
	{
		hrac.setPosition(10);
		hrac.jail = true;
		System.out.println(hrac.getName() + " jde do vezeni!");
		//transmission JAIL j!i!
	}
	
}


