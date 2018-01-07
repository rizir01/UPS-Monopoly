package com.mygdx.seme;

import java.io.IOException;

import com.badlogic.gdx.Game;
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

public class LobbyScreen implements Screen, InputProcessor
{	
	Game game;
	BitmapFont font;
	static OrthographicCamera camera;
	SpriteBatch batch;
	ShapeRenderer sr;
	Vector3 touch;
	
	static int width = Gdx.graphics.getWidth();
	static int height = Gdx.graphics.getHeight();
	
	static Lobby [] lobbies;
	
	static int refreshDelay = 10000;
	
	static int buttonClickDelay = 60;
	
	static int selectedLobby = -1;//Index vybrane lobby
	
	static boolean ready = false;//Jestli jsem "JA" ready
	
	static boolean drawAllInfo = false;
	
	String [] currentLobby;
	
	static boolean countDown = false;
	
	static long timeC;
	
	public LobbyScreen(Monopoly mono)
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
		Gdx.input.setInputProcessor(this);
		refreshDelay = 1;
		buttonClickDelay = 25;
	}
	
	public void processDelays()
	{
		if(refreshDelay <= 0)
		{
			refreshDelay = 10000;
			Monopoly.LoginScreen.sendToThread("GUI", "$refresh!0#");
		}
		else
		{
			refreshDelay--;
		}
		if(buttonClickDelay > 0)
		{
			buttonClickDelay--;
		}
		
		//Nastaveni casomiry
		if(countDown)
		{
			if(System.currentTimeMillis() - timeC >= 15000)
			{
				//Monopoly.LoginScreen.sendToThread("GUI", "$game!done!#");
				//System.out.println("Posilam zpravu o tom, ze jdu do hry");
				Monopoly.LoginScreen.tc.stavHrace = 3;
				game.setScreen(Monopoly.GameScreen);
				countDown = false;
			}
		}
	}

	@Override
	public void render(float delta)
	{
		//reset canvas
		Gdx.gl.glClearColor(1F, 1F, 1F, 1F);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
		camera.update();
		
		//Zpracovani vsech delay promennych plus
		//Countdown zalezitosti pro presmerovani do hry
		processDelays();
		
		//vykresleni vsech moznych komponent
		drawAllComponents();
		
		/*Kontrola ovladacich prvku, jestli nad nimi nebyla
		 * provedena nejaka akce
		 */
		generalUpdate();
		
	}
	
	public void drawAllComponents()
	{
		float x = 30;
		float y = 30;
		if(drawAllInfo)
		{
			for(int i = 0; i < 4; i++)
			{
				if(currentLobby[i] == null)
				{
					drawPlayerReadyInfo(x, y, 500, 100, "--empty--", false);
				}
				else
				{
					drawPlayerReadyInfo(x, y, 500, 100, currentLobby[i], false);
					if(lobbies[selectedLobby].getReady()[i])
					{
						drawText(x + 530, height - (y + 35), "READY", Color.GREEN);
					}
				}
				y += 100 + 30; 
			}
			drawButton(260, height - 105, 200, 75, "LEAVE");
			drawButton(730, height - 105, 200, 75, "READY");
			if(countDown)
			{
				drawText(580, 85, "" + (int)(15 -(System.currentTimeMillis() - timeC) * 0.001), Color.BLUE);
			}
		}
		else
		{
			if(lobbies != null)
			{
				for(int i = 0; i < lobbies.length; i++)
				{
					if(selectedLobby == i)
					{
						renderLobbyInfo(x, y, 500, 100, lobbies[i].lobbyName, lobbies[i].getPocetHrau(), Color.RED);					
						drawButton(x + 400, y + 10, 80, 75, "JOIN");
					}
					else
					{
						renderLobbyInfo(x, y, 500, 100, lobbies[i].lobbyName, lobbies[i].getPocetHrau(), Color.GRAY);
					}
					y += 100 + 30; 
				}
			}		
		}	
		drawButton(30, height - 105, 200, 75, "DISCONNECT");
	}
	
	public void drawText(float x, float y, String text, Color color)
	{
		batch.begin();
			font.setColor(color);
			font.draw(batch, text, x, y);
		batch.end();
		font.setColor(Color.BLACK);
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
	
	public void drawPlayerReadyInfo(float x, float y, float widthX, float widthY, String jmeno, boolean ready)
	{
		float spaceTextX = 5;
		float spaceTextY = (widthY / 2) - 15;//15 je pro velikost pisma
		sr.begin(ShapeType.Line);
			sr.setColor(Color.GRAY);	
			sr.rect(x, height - y, widthX, -widthY);//Normalne widthH vykresluje arc nahoru, ne dolu!
			batch.begin();
				font.draw(batch, jmeno, x + spaceTextX, (height - y) - spaceTextY);
				if(ready)
				{
					font.draw(batch, "READY", x + 450, (height - y) - spaceTextY);
				}
			batch.end();
		sr.end();
	}
	
	/**
	 * Vykresleni zakladnich informaci o nactenych lobbynach
	 * z serveru.
	 * 
	 * @param		x			x-ove umisteni okenka
	 * @param 		y			y-ove umisteni okenka
	 * @param 		widthX		sirka velikosti okenka pro info
	 * @param 		widthY		vyska velikosti okenka pro info
	 * @param 		name		nazev lobby
	 * @param 		pocet		pocet lidi v lobby
	 * @param		c			Barva ohraniceni
	 */
	public void renderLobbyInfo(float x, float y, float widthX, float widthY, String name, int pocet, Color c)
	{
		//sr - pocet vlevo nahore
		//batch - vlevo dole
		float spaceTextX = 20;
		float spaceTextY = 35;
		sr.begin(ShapeType.Line);
			sr.setColor(c);
			sr.rect(x, height - y, widthX, -widthY);//Normalne widthH vykresluje arc nahoru, ne dolu!
			batch.begin();
				font.draw(batch, name, x + spaceTextX, (height - y) - spaceTextY);
				font.draw(batch, pocet + "/4", x + 10 * spaceTextX,  (height - y) - spaceTextY);
			batch.end();
		sr.end();
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

	public void generalUpdate()
	{
		if(Gdx.input.isTouched() && buttonClickDelay == 0)//Pro tlacitka kde je potreba delay!!
		{
			//Kontrola stisknuti nejake lobby a vraceni indexu lobby do hodnoty
			if(lobbies != null && !drawAllInfo && selectedLobby == -1)
			{
				float x = 30;
				float y = 30;
				boolean nenasel = true;
				for(int i = 0; i < lobbies.length; i++)
				{
					if(isObjectTouched(touch, x, y, 500, 100))
					{
						selectedLobby = i;
						nenasel = false;
						buttonClickDelay = 25;
						break;
					}
					y += 100 + 30;
				}
				if(nenasel)
				{
					selectedLobby = -1;					
				}
				return;
			}
			
			//Kontrola jestli nejake z tlacitek nebylo spusteno
			if(isObjectTouched(touch, 30, height - 105, 200, 75))//DISCONNECT
			{
				Monopoly.LoginScreen.sendToThread("GUI", "$discon!0#");
				try
				{
					Monopoly.LoginScreen.rc.join(1);
					Monopoly.LoginScreen.tc.bf.close();
					Monopoly.LoginScreen.tc.bw.close();
					Monopoly.LoginScreen.tc.socket.close();
					Monopoly.LoginScreen.tc.join(1);
				}
				catch (InterruptedException e)
				{
					System.out.println("INTER EXCEPTION");
					e.printStackTrace();
				}
				catch (IOException e)
				{
					System.out.println("IOEXCEPTION");
					e.printStackTrace();
				}
				LobbyScreen.lobbies = null;
				drawAllInfo = false;
				buttonClickDelay = 25;
				game.setScreen(Monopoly.LoginScreen);
				
			}
			if(selectedLobby != -1 && !drawAllInfo)//JOIN
			{
				if(isObjectTouched(touch, 30 + 400, (30 + (130*selectedLobby)) + 10, 80, 75))
				{
					Monopoly.LoginScreen.sendToThread("GUI", "$join!" + selectedLobby + "#");
					buttonClickDelay = 25;
				}				
			}
			
			if(drawAllInfo)//Jestli jsem v konkretni lobby
			{
				if(isObjectTouched(touch, 260, height - 105, 200, 75))//LEAVE
				{
					Monopoly.LoginScreen.sendToThread("GUI", "$leave!0#");
					Monopoly.LobbyScreen.lobbies[selectedLobby].removePlayer(LoginScreen.login);
					drawAllInfo = false;
					//selectedLobby = -1;
					buttonClickDelay = 25;
					refreshDelay = 5;
				}
				else if(isObjectTouched(touch, 730, height - 105, 200, 75))//READY
				{
					if(ready)
					{
						Monopoly.LoginScreen.sendToThread("GUI", "$ready!0#");
						lobbies[selectedLobby].removeReady(Monopoly.LoginScreen.login);
					}
					else
					{
						Monopoly.LoginScreen.sendToThread("GUI", "$ready!1#");						
						lobbies[selectedLobby].addReady(Monopoly.LoginScreen.login);
					}
					ready = !ready;
					buttonClickDelay = 25;
				}
			}
		}
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
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY)
	{
		return false;
	}

	@Override
	public boolean scrolled(int amount)
	{
		return false;
	}

}
