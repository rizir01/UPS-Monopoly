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
	boolean hide;
	
	//Create Lobby parameters
	String lobbyName;
	boolean selectedLobbyName;
	int kurzorDelay = 0;
	
	//Slide lobby parameters
	float slideX = 30;//pozice pozcatku lobbyin
	float slideY = 30;
	float slideBarX = 550;
	float slideBarY = 30;
	float slideBarWidth = 30;
	float slideBarHeight;
	
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
		hide = false;
		Gdx.input.setInputProcessor(this);
		selectedLobbyName = false;
		lobbyName = "";
		refreshDelay = 1;
		buttonClickDelay = 25;
	}
	
	public void processDelays()
	{
		if(refreshDelay <= 0)
		{
			refreshDelay = 10000;
			//Monopoly.LoginScreen.sendToThread("GUI", "$refresh!0#");
			Monopoly.LoginScreen.tc.sendMessageToServer("$refresh!0#");
		}
		else
		{
			refreshDelay--;
		}
		if(buttonClickDelay > 0)
		{
			buttonClickDelay--;
		}
		
		if(kurzorDelay <= - 35)
		{
			kurzorDelay = 35;
		}
		else
		{
			kurzorDelay--;
		}
		
		//Nastaveni casomiry
		if(countDown)
		{
			if(System.currentTimeMillis() - timeC >= 15000)
			{
				//Monopoly.LoginScreen.sendToThread("GUI", "$game!done!#");
				//System.out.println("Posilam zpravu o tom, ze jdu do hry");
				Monopoly.LoginScreen.tc.stavHrace = 3;
				hide = true;
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
		float y = 90;
		if(drawAllInfo)
		{
			drawText(x, height - 30, LobbyScreen.lobbies[LobbyScreen.selectedLobby].lobbyName.toUpperCase(), Color.BLUE);
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
			x = slideX;
			y = slideY;
			if(lobbies != null)
			{
				for(int i = 0; i < lobbies.length; i++)
				{
					if(selectedLobby == i)
					{
						renderLobbyInfo(x, y, 500, 100, lobbies[i].lobbyName, lobbies[i].getPocetHracuRefresh(), Color.RED);					
						drawButton(x + 400, y + 10, 80, 75, "JOIN");
					}
					else
					{
						renderLobbyInfo(x, y, 500, 100, lobbies[i].lobbyName, lobbies[i].getPocetHracuRefresh(), Color.GRAY);
					}
					y += 100 + 30; 
				}
			}
			drawButton(860, 620, 75, 75, "REF.");
			
			//Vyrenderovat vpravo formular pro vytvoreni nove
			//lobby a nasledne moznost poslat serveru jeji hodnoty
			renderCreateLobby();
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
	
	public void renderCreateLobby()
	{
		float spaceX = 20f;
		sr.begin(ShapeType.Filled);
			sr.setColor(Color.LIGHT_GRAY);
			sr.rect(575, height - 30, 350, -155);
			sr.setColor(Color.WHITE);
			sr.rect(575 + spaceX, height - 110, 310, -50);
		sr.end();
		batch.begin();
			font.draw(batch, "Lobby name:", 575 + spaceX, height - 60);
			if(selectedLobbyName)
			{
				if(kurzorDelay > 0)
				{
					font.draw(batch, lobbyName + "|", 575 + spaceX, height - 125);				
				}
				else
				{
					font.draw(batch, lobbyName, 575 + spaceX, height - 125);
				}				
			}
			else
			{
				font.draw(batch, lobbyName, 575 + spaceX, height - 125);
			}
		batch.end();
		drawButton(687.5f, 200, 125, 50, "CREATE");
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
			boolean praveNasel = false;
			if(lobbies != null && !drawAllInfo )//&& selectedLobby == -1
			{
				float x = 30;
				float y = 30;
				boolean nenasel = true;
				for(int i = 0; i < lobbies.length; i++)
				{
					if(isObjectTouched(touch, x, y, 500, 100))
					{
						if(selectedLobby != i)
						{
							praveNasel = true;							
						}
						selectedLobby = i;
						nenasel = false;
						selectedLobbyName = false;
						buttonClickDelay = 25;
						break;
					}
					y += 100 + 30;
				}
				if(nenasel)
				{
					selectedLobby = -1;
					selectedLobbyName = false;
				}
				//return;
			}
			
			//Kontrola jestli nejake z tlacitek nebylo spusteno
			if(isObjectTouched(touch, 30, height - 105, 200, 75))//DISCONNECT
			{
				//Monopoly.LoginScreen.sendToThread("GUI", "$discon!0#");
				selectedLobbyName = false;
				hide = false;
				game.setScreen(Monopoly.LoginScreen);
				
			}
			if(selectedLobby != -1 && !praveNasel && !drawAllInfo)//JOIN
			{
				if(isObjectTouched(touch, 30 + 400, (30 + (130*selectedLobby)) + 10, 80, 75))
				{
					//Monopoly.LoginScreen.sendToThread("GUI", "$join!" + selectedLobby + "#");
					Monopoly.LoginScreen.tc.sendMessageToServer("$join!" + selectedLobby + "#");
					selectedLobbyName = false;
					buttonClickDelay = 25;
				}				
			}
			if(!drawAllInfo && isObjectTouched(touch, 595, 110, 310, 50))//Oblast pro vykresleni nazvu lobby
			{
				selectedLobbyName = true;
				buttonClickDelay = 25;
			}
			if(!drawAllInfo && isObjectTouched(touch, 687.5f, 200, 125, 50))//Create lobby
			{
				if(lobbyName.length() < 1 || lobbyName.length() > 8)
				{
					System.out.println("Nazev lobby musi byt mezi <1,8>");
				}
				else
				{
					Monopoly.LoginScreen.tc.sendMessageToServer("$create!" + lobbyName + "#");					
				}
				buttonClickDelay = 25;
			}
			if(!drawAllInfo && isObjectTouched(touch, 860, 620, 75, 75))
			{
				refreshDelay = 10000;
				Monopoly.LoginScreen.tc.sendMessageToServer("$refresh!0#");
				buttonClickDelay = 25;
			}
			
			if(drawAllInfo)//Jestli jsem v konkretni lobby
			{
				if(isObjectTouched(touch, 260, height - 105, 200, 75))//LEAVE
				{
					//Monopoly.LoginScreen.sendToThread("GUI", "$leave!0#");
					Monopoly.LoginScreen.tc.sendMessageToServer("$leave!0#");
					Monopoly.LobbyScreen.lobbies[selectedLobby].removePlayer(LoginScreen.login);
					drawAllInfo = false;
					//selectedLobby = -1;
					lobbyName = "";
					buttonClickDelay = 25;
					refreshDelay = 5;
				}
				else if(isObjectTouched(touch, 730, height - 105, 200, 75))//READY
				{
					if(ready)
					{
						//Monopoly.LoginScreen.sendToThread("GUI", "$ready!0#");
						Monopoly.LoginScreen.tc.sendMessageToServer("$ready!0#");
						lobbies[selectedLobby].removeReady(Monopoly.LoginScreen.login);
					}
					else
					{
						//Monopoly.LoginScreen.sendToThread("GUI", "$ready!1#");
						Monopoly.LoginScreen.tc.sendMessageToServer("$ready!1#");
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
			drawAllInfo = false;
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
		if(selectedLobbyName)
		{
			if((int)character == 8)//BACKSPACE
			{
				int l;
				l = lobbyName.length();
				if(l >= 1)
				{
					lobbyName = lobbyName.substring(0, l - 1);					
				}
			}
			else
			{
				if(((int)character >= 97 && (int)character <= 122) || ((int)character >= 48 && (int)character <= 57))
				{
					lobbyName = lobbyName + character;			
				}
			}			
		}
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
