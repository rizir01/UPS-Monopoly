package com.mygdx.seme;

public class Jail extends Card
{

	public Jail(String name)
	{
		super(name);
	}
	
	public void action(Player hrac, Player [] hraci)
	{
		if(hrac.jailFree)
		{
			System.out.println(hrac.getName() +  " pouzil kartu, aby se dostal z vezeni");
			System.out.println(hrac.getName() + " se dostal z vezeni a haze koustkou");
			int index = -1;
			for (int i = 0; i < hraci.length; i++)
			{
				if(hraci[i] == hrac)
				{
					index = i;
					break;
				}
			}
			if(index == -1)
			{
				System.out.println("Hrac nebyl nalezen mezi hraci v konkretni hre");
				System.exit(0);
				Assets.sc.close();
			}
			Table.anotherRun = true;
		}
		else if(hrac.jail)
		{
			System.out.println(hrac.getName() +  " hazi kostky z vezeni");
			System.out.println("Hod kostkou[1]:");
			int kostka1 = Assets.rand.nextInt(5) + 1;
			//int kostka1 = Assets.sc.nextInt();
			int kostka2 = Assets.rand.nextInt(5) + 1;
			System.out.println("Hod kostkou[2]:");
			//int kostka2 = Assets.sc.nextInt();
			if(kostka1 == kostka2)
			{
				hrac.jail = false;
				System.out.println(hrac.getName() + " se dostal z vezeni a haze koustkou");
				int index = -1;
				for (int i = 0; i < hraci.length; i++)
				{
					if(hraci[i] == hrac)
					{
						index = i;
						break;
					}
				}
				if(index == -1)
				{
					System.out.println("Hrac nebyl nalezen mezi hraci v konkretni hre");
					System.exit(0);
					Assets.sc.close();
				}
				Table.anotherRun = true;
			}
			else
			{
				System.out.println(hrac.getName() + " je stale ve vezeni");
			}
		}
		else
		{
			System.out.println(hrac.getName() + " je ve vezeni");
			hrac.jail = true;
		}
	}

}
