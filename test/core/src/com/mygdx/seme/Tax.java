package com.mygdx.seme;

public class Tax extends Card
{

	private int pay;
	
	public Tax(String name)
	{
		super(name);
	}
	
	public Tax(String name, int pay)
	{
		super(name);
		this.pay = pay;
	}
	
	public void action(Player hrac, Player [] hraci)
	{
		hrac.removeMoney(pay);
		int index = -1;
		for (int i = 0; i < hraci.length; i++)
		{
			if(hraci[i].getId() == hrac.getId())
			{
				index = i;
			}
		}
		//TRANSMISSION!!!
		 Table.transmission("p!" + index + "!" + pay +  "!");
		 //TRANSMISSION!!!
		System.out.println(hrac.getName() + " zaplatil bance " + pay + " za placeni taxi");
	}
	
	public int getPay()
	{
		return pay;
	}
	
	public void setPay(int pay)
	{
		this.pay = pay;
	}
	
}
