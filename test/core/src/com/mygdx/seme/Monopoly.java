package com.mygdx.seme;

import com.badlogic.gdx.Game;

public class Monopoly extends Game
{
	public static GameScreen GameScreen;
	public static LoginScreen LoginScreen;
	public static LobbyScreen LobbyScreen;

	@Override
	public void create()
	{
		Assets.load();//Nacte textury, mapu hry a pocatecni stav hry(hrace a jejich majetek)
		//Server server = new Server();
		//server.start();
		GameScreen = new GameScreen(this);
		LobbyScreen = new LobbyScreen(this);
		LoginScreen = new LoginScreen(this);
		setScreen(LoginScreen);
		//setScreen(GameScreen);
	}
}
