package com.mygdx.seme.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.seme.Monopoly;

public class DesktopLauncher
{
	public static void main(String[] arg)
	{
		if(arg.length != 2)
		{
			System.out.println("Nedostatek parametru pro zjisteni serveru!");
			System.exit(5);
		}
		
		String ht = arg[0];
		int pt = -1;
		try
		{
			pt = Integer.parseInt(arg[1]);
		}
		catch(NumberFormatException nfe)
		{
			System.out.println("Parameter pro port neni cislo!");
			System.exit(5);
		}
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Monopoly";
		config.width = 960;
		config.height = 720;
		config.resizable = false;
		new LwjglApplication(new Monopoly(ht, pt), config);
	}
}
