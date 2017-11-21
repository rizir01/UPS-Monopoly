package com.mygdx.seme;

public class Player
{
	private int money;
	private int position;
	private Card [] list;
	private String name;
	private int id;
	private static int countID = 1;
	public boolean jail;
	boolean jailFree;
	
	public Player(String name)
	{
		this.name = name;
		id = countID++;
		money = 0;
		list = new Card[0];
		position = 0;
		jail = false;
		jailFree = false;
	}
	
	public Player(String name, int money)
	{
		this.name = name;
		id = countID++;
		this.money = money;
		list = new Card[0];
		position = 0;
		jail = false;
		jailFree = false;
	}

	public int getMoney() {
		return money;
	}

	public void addMoney(int money)
	{
		this.money += money;
	}
	
	public void removeMoney(int money)
	{
		this.money -= money;
	}

	public Card[] getList() {
		return list;
	}

	public void setList(Card[] list) {
		this.list = list;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int getPosition() {
		return position;
	}

	public void setPosition(int position)
	{
		this.position = position;
	}

	public String toString()
	{
		return getName() + ", " + getMoney();
	}
}
