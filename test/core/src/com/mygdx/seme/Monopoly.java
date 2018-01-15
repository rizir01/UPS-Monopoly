package com.mygdx.seme;

import com.badlogic.gdx.Game;

public class Monopoly extends Game
{
	public static GameScreen GameScreen;
	public static LoginScreen LoginScreen;
	public static LobbyScreen LobbyScreen;
	public static EndScreen EndScreen;
	
	public static String host;//IP adresa serveru
	public static int port;//Port serveru
	
	public Monopoly(String ht, int pt)
	{
		host = ht;
		port = pt;
	}

	@Override
	public void create()
	{
		Assets.load();//Nacte textury, mapu hry a pocatecni stav hry(hrace a jejich majetek)
		GameScreen = new GameScreen(this);
		LobbyScreen = new LobbyScreen(this);
		LoginScreen = new LoginScreen(this);
		EndScreen = new EndScreen(this);
		setScreen(LoginScreen);
	}
}
