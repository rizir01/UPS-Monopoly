package com.mygdx.seme;

import java.io.IOException;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;

public class EndScreen implements Screen
{
	Game game;
	BitmapFont font;
	static OrthographicCamera camera;
	SpriteBatch batch;
	ShapeRenderer sr;
	Vector3 touch;
	boolean hide;
	
	static int buttonClickDelay;
	
	static int width = Gdx.graphics.getWidth();
	static int height = Gdx.graphics.getHeight();
	
	public EndScreen(Monopoly mono)
	{
		this.game = mono;
		font = new BitmapFont(Gdx.files.internal("font/Stelar-Regular-final.fnt"), false);
		font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font.getData().setScale(0.25f, 0.25f);
		font.setColor(Color.BLACK);
		camera = new OrthographicCamera();
		camera.setToOrtho(true, width, height);
		batch = new SpriteBatch();
		sr = new ShapeRenderer();
        touch = new Vector3();
	}
	
	
	@Override
	public void show()
	{
		hide = false;
		buttonClickDelay = 25;
	}

	@Override
	public void render(float delta)
	{
		if(buttonClickDelay > 0)
		{
			buttonClickDelay--;
		}
		
		//reset canvas
		Gdx.gl.glClearColor(1F, 1F, 1F, 1F);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
		camera.update();
		
		renderEndgameStats();
	}
	
	public void renderEndgameStats()
	{
		//Zde bude vykresleny vsechny informace posbirane behem hry
		//Kolik tahu bylo odehrano, kdo v jakem kole zemrel
		//Kdo samozdrejme vyhral a vsechny informace o hracich a
		//jejich vysledcich(penize, pocet max. nasbiranych majetku,
		//vybranych penez, darovanych penez, kolikrat byl ve vezeni
		//atd., co me jen napadne
		
		batch.begin();
			font.getData().setScale(0.25f);
			font.draw(batch, "Vyhral hrac " + GameScreen.winName + "!", 300, height - 100);
		batch.end();
		
		renderButtons();
	}
	
	public void renderButtons()
	{
		drawButton(50, 555, 300, 100, "LEAVE");
		drawButton(610, 555, 300, 100, "DISCON");
		
		if(buttonClickDelay == 0)
		{
			if(isObjectTouched(touch, 50, 555, 300, 100))
			{
				System.out.println("LEAVE");
				Monopoly.LoginScreen.tc.sendMessageToServer("$return!0#");
				buttonClickDelay = 25;
			}
			else if(isObjectTouched(touch, 610, 555, 300, 100))
			{
				//System.out.println("DISCON");
				//Discon se posle z funkce hide(); !
				hide = false;
				game.setScreen(Monopoly.LoginScreen);
				buttonClickDelay = 25;
			}
		}
	}
	
	public void drawButton(float x, float y, float widthX, float widthY, String text)
	{
		float spaceTextX = 5;
		float spaceTextY = (widthY / 2) - 15;//15 je pro velikost pisma
		sr.begin(ShapeType.Filled);
			sr.setColor(Color.LIGHT_GRAY);
			sr.rect(x, height - y, widthX, -widthY);//Normalne widthH vykresluje arc nahoru, ne dolu!
		sr.end();
		sr.begin(ShapeType.Line);
			sr.setColor(Color.BLACK);
			sr.rect(x, height - y, widthX, -widthY);
		sr.end();
		batch.begin();
			font.draw(batch, text, x + spaceTextX, (height - y) - spaceTextY);
		batch.end();
	}

	public static boolean isObjectTouched(Vector3 touch, float x, float y, float rozmerX, float rozmerY)
	{
		if(Gdx.input.isTouched())
		{
			touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touch);
			if(touch.x > x && touch.x < x + rozmerX)
			{ 
				if (touch.y > y && touch.y < y + rozmerY)
				{
					return true;
				}
			}
			return false;
		}
		return false;
	}
	
	@Override
	public void resize(int width, int height)
	{

	}

	@Override
	public void pause()
	{

	}

	@Override
	public void resume()
	{

	}

	@Override
	public void hide()
	{
		if(!hide)
		{
			Monopoly.LoginScreen.tc.sendMessageToServer("$discon!0#");
			try
			{
				Monopoly.LoginScreen.rc.join(10);
				Monopoly.LoginScreen.tc.bf.close();
				Monopoly.LoginScreen.tc.bw.close();
				Monopoly.LoginScreen.tc.socket.close();
				Monopoly.LoginScreen.tc.join(10);
			}
			catch (InterruptedException e)
			{
				System.out.println("ON CLOSE INTER EXCEPTION");
				e.printStackTrace();
			}
			catch (IOException e)
			{
				System.out.println("ON CLOSE IOEXCEPTION");
				e.printStackTrace();
			}
			LobbyScreen.lobbies = null;
			buttonClickDelay = 25;		
		}
	}

	@Override
	public void dispose()
	{
		batch.dispose();
		font.dispose();
		sr.dispose();
	}

}
