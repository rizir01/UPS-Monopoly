package com.mygdx.seme;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
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

public class LoginScreen implements Screen, InputProcessor
{
	Message [] bufferSend;
	static int indBuff = 0;
	
	TestClient tc;
	TestClientRecieve rc;
	
	Monopoly game;
	static OrthographicCamera camera;
	SpriteBatch batch;
	BitmapFont font;
	ShapeRenderer sr;
	Vector3 touch;
	Random rand;
	
	static int width = Gdx.graphics.getWidth();
	static int height = Gdx.graphics.getHeight();
	static String login = "";
	static String pass = "";
	static int kurzorDelay = 0;
	static byte kurzor = 0;
	static int clickDelay = 0;
	static boolean notClick = false;
	static boolean register = false;
	
	public LoginScreen(Monopoly mono)
	{
		bufferSend = new Message[5];
		for(int i = 0; i < bufferSend.length; i++)
		{
			bufferSend[i] = new Message(null, null);
		}
		
		this.game = mono;
		rand = new Random();
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
	
	public void sendToThread(String type, String data)
	{
		synchronized(bufferSend[indBuff])
		{
			bufferSend[indBuff].setMessage(type, data);
			bufferSend[indBuff].notify();
			indBuff++;
		}
	}
	
	@Override
	public void show()
	{
		Gdx.input.setInputProcessor(this);
		tc = new TestClient(bufferSend);
		rc = new TestClientRecieve(bufferSend);
	}
	
	@Override
	public void render(float delta)
	{
		//reset canvas
		Gdx.gl.glClearColor(1F, 1F, 1F, 1F);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
		camera.update();
		processDelays();
		
		//Vykresleni login screenu
		renderBoard(delta);
	}
	
	public void processDelays()
	{
		if(kurzorDelay <= - 35)
		{
			kurzorDelay = 35;
		}
		else
		{
			kurzorDelay--;
		}
		if(clickDelay != 0)
		{
			clickDelay--;
		}
	}
	
	public void renderBoard(float delta)
	{
		float spaceX = 0.25f;
		float spaceY = 0.25f;
		float widthSpace = width * spaceX;
		float heightSpace = height * spaceY;
		float widthA = width * 2 * spaceX;
		float heightA = height * 2 * spaceY;
		float widthEdgeX = widthA * 0.05f;
		float heightEdgeY = heightA * 0.05f;
		float widthWrite = widthA * 0.6f;
		float heightWrite = heightA * 0.15f;
		float widthButton = widthA * 0.4f;
		float widthEdBut = (widthA * 0.2f) /  3;
		float heightButton = heightA * 0.12f;
		sr.begin(ShapeType.Filled);
			sr.setColor(Color.LIGHT_GRAY);
			sr.rect(widthSpace, heightSpace, widthA, heightA);
			sr.setColor(Color.WHITE);
			float y = heightSpace + heightA - heightWrite - 3 * heightEdgeY;
			sr.rect(widthSpace + widthA * 0.5f - widthWrite * 0.5f, y, widthWrite, heightWrite);
			sr.rect(widthSpace + widthA * 0.5f - widthWrite * 0.5f, y - 65 - heightWrite, widthWrite, heightWrite);
			sr.rect(widthSpace + widthEdBut, heightSpace + heightEdgeY, widthButton, heightButton);
			sr.rect(widthSpace + 2 * widthEdBut + widthButton, heightSpace + heightEdgeY, widthButton, heightButton);
		sr.end();
		batch.begin();
			String hvezdicky = "";
			for (int i = 0; i < pass.length(); i++)
			{
				hvezdicky += "*";
			}
			if(kurzorDelay > 0)
			{
				if(kurzor == 0)
				{
					font.draw(batch, login + "|", widthSpace + widthA * 0.5f - widthWrite * 0.5f, y + 40);
					font.draw(batch, hvezdicky, widthSpace + widthA * 0.5f - widthWrite * 0.5f, y - 22 - heightWrite);
				}
				else
				{
					font.draw(batch, login, widthSpace + widthA * 0.5f - widthWrite * 0.5f, y + 40);
					font.draw(batch, hvezdicky + "|", widthSpace + widthA * 0.5f - widthWrite * 0.5f, y - 22 - heightWrite);									
				}
			}
			else
			{
				font.draw(batch, login, widthSpace + widthA * 0.5f - widthWrite * 0.5f, y + 40);
				font.draw(batch, hvezdicky, widthSpace + widthA * 0.5f - widthWrite * 0.5f, y - 22 - heightWrite);
			}
			font.draw(batch, "Login", widthSpace + widthEdBut + 55, heightSpace + heightEdgeY + heightButton - 7);
			font.draw(batch, "Register", widthSpace + 2 * widthEdBut + widthButton + 30, heightSpace + heightEdgeY + heightButton - 7);
		batch.end();
		y = heightSpace + 3 * heightEdgeY;
		if(clickDelay == 0)
		{
			if(isObjectTouched(touch, widthSpace + widthA * 0.5f - widthWrite * 0.5f, y, widthWrite, heightWrite))
			{
				kurzor = 0;
				clickDelay = 50;
			}
			if(isObjectTouched(touch, widthSpace + widthA * 0.5f - widthWrite * 0.5f, y + 65 + heightWrite, widthWrite, heightWrite))
			{
				kurzor = 1;
				clickDelay = 50;
			}
			if(isObjectTouched(touch, widthSpace + widthEdBut, heightSpace + heightA - heightEdgeY - heightButton, widthButton, heightButton))
			{
				//LOGIN CLIENTA
				new Thread(tc, "CLIENT CONNECTION AND SENDING").start();
				clickDelay = 50;
			}
			if(isObjectTouched(touch, widthSpace + 2 * widthEdBut + widthButton, heightSpace + heightA - heightEdgeY - heightButton, widthButton, heightButton))
			{
				//REGISTER CLIENTA, momentalne preskoceni rovnou do hry, ktera je singleplayer
				System.out.println("GUI Register");
				game.setScreen(Monopoly.GameScreen);
				clickDelay = 50;
			}
		}
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

	}
	
	@Override
	public void dispose()
	{
		batch.dispose();
		font.dispose();
		sr.dispose();
	}
	
	@Override
	public boolean keyDown(int keycode)
	{
		return false;
	}
	
	@Override
	public boolean keyUp(int keycode)
	{
		return false;
	}
	
	@Override
	public boolean keyTyped(char character)
	{
		if((int)character == 8)//BACKSPACE
		{
			int l;
			if(kurzor == 0)
			{
				l = login.length();
				if(l >= 1)
				{
					login = login.substring(0, l - 1);					
				}
			}
			else
			{
				l = pass.length();
				if(l >= 1)
				{
					pass = pass.substring(0, l - 1);					
				}
			}
		}
		else if((int)character == 9)//TAB
		{
			if(kurzor == 0)
			{
				kurzor = 1;
			}
			else
			{
				kurzor = 0;
			}
		}
		else if((int)character == 13)//ENTER
		{
			new Thread(tc, "CLIENT CONNECTION AND SENDING").start();
			clickDelay = 50;
		}
		else
		{
			if(((int)character >= 97 && (int)character <= 122) || ((int)character >= 48 && (int)character <= 57))
			{
				if(kurzor == 0)
				{
					login = login + character;
				}
				else
				{
					pass = pass + character;
				}				
			}
		}
		return false;
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
