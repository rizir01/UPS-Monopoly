package com.mygdx.seme;


public class Start extends Card
{

	public Start(String name)
	{
		super(name);
	}
	
	public void action(Player hrac, Player [] hraci)
	{
		hrac.addMoney(200);
		System.out.println(hrac.getName() + " ziskal za prijezd na start 200 penez");
	}

}
