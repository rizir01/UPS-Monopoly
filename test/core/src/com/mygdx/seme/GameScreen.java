package com.mygdx.seme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

public class GameScreen implements Screen, InputProcessor
{
	
	Monopoly game;
	static OrthographicCamera camera;
	SpriteBatch batch;
	BitmapFont font;
	ShapeRenderer sr;
	Vector3 touch;
	Random rand;
	boolean hide;
	
	static int hodStejnych = 0;
	static boolean [] changeOfPlayers = new boolean[1];
	
	static int width = Gdx.graphics.getWidth();
	static int height = Gdx.graphics.getHeight();
	static float board = 0.75f;//0.5625f 1280; 720 pixelu
	static float info = 1 - board;
	static float boardWidth = width * board;
	static float boardHeight;
	static float infoheight = boardHeight = height;
	static float ratio = boardWidth/boardHeight;
	static float infoWidth = width * info;
	static float widthOfPile = 0.121212f;
	static float insidePile = 1 - (widthOfPile * 2);
	static float widthOfInsidePile = insidePile / 9;
	static float heightOfInsidePile = (1 - (widthOfPile * 2))/ 9;
	
	static Color [] barvy = new Color[]{Color.WHITE, Color.BROWN, Color.CYAN, Color.PURPLE, Color.ORANGE, Color.RED, Color.YELLOW, Color.FOREST, Color.BLUE, Color.PINK};
	static int [][] poziceBarev = new int [][]{{5, 0, 5, 5, 0, 6, 6, 0, 6, 0}, {2, 2, 0, 2, 0, 0, 1, 0, 1, 0}, {4, 4, 0, 4, 0, 3, 3, 0, 3, 0}, {7, 7, 0, 7, 0, 0, 8, 0, 8, 0}};
	
	public static Card [] screenTable = new Card[40];
	public static Player [] screenHraci;
	public static int [] skipHraci;
	public static boolean startRound = true;
	public static int pozice;
	public static boolean notClick = false;
	public static int delay = 0;
	
	//PLAYER STATS
	public static int statsDelay = 0;
	public static int [] moneyDelay;
	public static int [][] lastThrow;
	
	//CHEST&CHANCE&TAX
	public static boolean chestChance = false;
	public static int chestChancePozice;
	public static int cCTPlayer;
	public static int cCTtype;
	public static String drop;
	
	//AUKCE
	public static boolean aukce = false;
	public static int aukcePozice;
	public static int aukceMax;
	public static int [] bids;
	public static int folds;
	public static int bid;
	
	//WAITING ROOM
	public static boolean waiting = true;
	public static int [] waited;
	/**Index klienta v teto hre, pro usnadneni zobrazeni informaci pro kazdeho zvlast*/
	public static int intKlient;
	
	public static String winName;
	
	
	public static boolean pack = false;
	public static ArrayList<String> gameInput = new ArrayList<String>();
	
	static Table vlakno;
	
	public GameScreen(Monopoly game)
	{
		this.game = game;
		rand = new Random();
		font = new BitmapFont(Gdx.files.internal("font/Stelar-Regular-final.fnt"), false);
		font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font.getData().setScale(0.25f, 0.25f);
		font.setColor(Color.BLACK);
		camera = new OrthographicCamera();
		camera.setToOrtho(true, width, height);
		batch = new SpriteBatch();
		sr = new ShapeRenderer();
        sr.setColor(Color.BLUE);
        touch = new Vector3();
        Gdx.input.setInputProcessor(this);
	}
	
	@Override
	public void show()
	{
		//MOMENTALNE VYPNUTA SIMULACE SERVERU
		//PRO TEST REALNEHO SERVERU
		//vlakno = new Table("Druhe");
		//vlakno.start();
		//Monopoly.LoginScreen.sendToThread("GUI", "$loaded!0#");
		Monopoly.LoginScreen.tc.sendMessageToServer("$loaded!0#");
		hide = false;
		waiting = true;
		/*
		moneyDelay = new int[screenHraci.length];
		lastThrow = new int[screenHraci.length][2];
		for (int i = 0; i < lastThrow.length; i++)
		{
			lastThrow[i][0] = -1;
			lastThrow[i][1] = -1;
		}
		identifyPlayer();
		*/
	}

	@Override
	public void render(float delta)
	{
		//System.out.println("START RENDERING");
		//reset canvas
		Gdx.gl.glClearColor(1F, 1F, 1F, 1F);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
		camera.update();
		if(delay != 0)
		{
			delay--;
		}
		if(statsDelay != 0)
		{
			statsDelay--;
		}
		
		//Cekani na vsechny hrace az budou ready
		if(waiting)
		{
			renderWaitingRoom(delta);
		}
		else
		{
			//zpracovani inputu z druheho vlakna
			recognizeInputAndUpdate();
			
			//Vykresleni policek
			renderBoard(delta);
			
			//Vykresleni textu v polickach
			renderTextBoard(delta);
			
			//Vykresleni pozice hracu a vlastnictvi pozemku
			renderPlayerOnTable(delta);
			
			//Vykresleni informaci kazdeho hrace na prave
			//strane herniho platna
			renderPlayerStats(delta);
			
			renderButtons(delta);
			
			//Vykresleni plneho zobrazeni informaci od danem
			//poli uprostred platna hraci plochy vcetne vsech
			//interakci s uzivatelem
			if(chestChance)
			{
				renderChestChanceInfo(delta);
			}
			else
			{
				if(startRound)
				{
					renderStartTurn(delta);
				}
				else if(aukce)
				{
					renderAukceInfo(delta);
				}
				else
				{
					renderFullInfo(delta);
				}			
			}
		}	
	}
	
	public void renderBoard(float delta)
	{
		sr.setColor(Color.BLACK);
		sr.setProjectionMatrix(camera.combined);
		sr.begin(ShapeType.Filled);
			//Hrany policek
			float x1, x2, y1, y2;
			x1 = boardWidth * widthOfPile;
			float deltaX, deltaY = 0;
			int z = 0;
			for (int i = 0; i < 2; i++, deltaY += boardHeight * (1 - widthOfPile * ratio))
			{	
				y1 = deltaY;
				y2 = (boardHeight * widthOfPile * ratio) + deltaY;
				deltaX = 0;
				int b = 0;
				for (int j = 0; j < 10; j++, deltaX += boardWidth * widthOfInsidePile)
				{					
					sr.setColor(barvy[poziceBarev[z][b++]]);
					sr.rectLine(x1 + deltaX + (widthOfInsidePile * boardWidth)/2, y1, x1 + deltaX + (widthOfInsidePile * boardWidth)/2, y2, widthOfInsidePile * boardWidth);
					sr.setColor(Color.BLACK);
					sr.rectLine(x1 + deltaX, y1, x1 + deltaX, y2, 3);
				}
				z++;
			}
			deltaX = 0;
			deltaY = 0;
			y1 = 0;
			for (int i = 0; i < 2; i++, deltaX += boardWidth * (1 - widthOfPile))
			{	
				x1 = deltaX;
				x2 = (boardWidth * widthOfPile) + deltaX;
				deltaY = boardHeight * widthOfPile * ratio;
				int b = 0;
				for (int j = 0; j < 10; j++, deltaY += boardHeight * heightOfInsidePile)
				{
					sr.setColor(barvy[poziceBarev[z][b++]]);
					sr.rectLine(x1, y1 + deltaY + (heightOfInsidePile * boardHeight)/2, x2, y1 + deltaY+ (heightOfInsidePile * boardHeight)/2, heightOfInsidePile * boardHeight);
					sr.setColor(Color.BLACK);
					sr.rectLine(x1, y1 + deltaY, x2, y1 + deltaY, 3);
				}
				z++;
			}
			//Okraj
			sr.rectLine(1, 1, boardWidth, 1, 3);
			sr.rectLine(boardWidth, 1, boardWidth, (boardHeight - 1), 3);
			sr.rectLine(boardWidth, boardHeight-1, 1, boardHeight-1, 3);
			sr.rectLine(1, boardHeight-1, 1, 1, 3);
			//Vnitrni okraj
			sr.rectLine(boardWidth * widthOfPile, (boardHeight * widthOfPile) * ratio, boardWidth * (1 - widthOfPile), (boardHeight * widthOfPile) * ratio, 3);
			sr.rectLine(boardWidth * (1 - widthOfPile), boardHeight * widthOfPile  * ratio, boardWidth * (1 - widthOfPile), boardHeight * (1 - widthOfPile * ratio), 3);
			sr.rectLine(boardWidth * widthOfPile, boardHeight * (1 - widthOfPile * ratio), boardWidth * (1 - widthOfPile), boardHeight * (1 - widthOfPile * ratio), 3);
			sr.rectLine(boardWidth * widthOfPile, boardHeight * widthOfPile * ratio, boardWidth * widthOfPile, boardHeight * (1 - widthOfPile * ratio), 3);
		sr.end();
	}
	
	public int boardPlayerOwning(int index, int playerId)
	{
		if(screenTable[index] instanceof Property)
		{
			if(((Property)screenTable[index]).getOwnPlayerID() != -1)
			{
				playerId = ((Property)screenTable[index]).getOwnPlayerID();
			}
		}
		if(screenTable[index] instanceof Railroad)
		{
			if(((Railroad)screenTable[index]).getOwnPlayerID() != -1)
			{
				playerId = ((Railroad)screenTable[index]).getOwnPlayerID();
			}
		}
		if(screenTable[index] instanceof Utility)
		{
			if(((Utility)screenTable[index]).getOwnPlayerID() != -1)
			{
				playerId = ((Utility)screenTable[index]).getOwnPlayerID();
			}
		}
		int vys = -1;
		if(playerId != -1)
		{
			for (int i = 0; i < screenHraci.length; i++)
			{
				if(screenHraci[i].getId() == playerId)
				{
					vys = i;
				}
			}
		}
		return vys;
	}

	public void renderTextBoard(float delta)
	{
		batch.begin();
			int m = 0;
			float mx2, my2;
			mx2 = boardWidth * (1 - widthOfPile * 0.5f);
			my2 = boardHeight * widthOfPile * 0.5f;
			for (int j = 0; j < screenTable.length; j++, m++)
			{
				if(screenTable[m] instanceof Property)
				{
					renderLowDetailInfo(m);
				}
				else if(screenTable[m] instanceof Railroad)
				{
					renderLowDetailInfo(m);
				}
				else if(screenTable[m] instanceof Utility)
				{
					renderLowDetailInfo(m);
				}
				else if(screenTable[m] instanceof Start)
				{
					font.draw(batch, "GO", mx2 - 20, my2 + 10);
				}
				else if (screenTable[m] instanceof Jail)
				{
					font.draw(batch, "JAIL", 15, 57);
				}
				else if(screenTable[m] instanceof ParkingLot)
				{
					font.draw(batch, "PARK", 5, boardHeight - 25);
				}
				else if(screenTable[m] instanceof GoToJail)
				{
					font.getData().setScale(0.125f, 0.125f);
					font.draw(batch, "Go to Jail", mx2 - 37, boardHeight - 35);
					font.getData().setScale(0.25f, 0.25f);
				}
			}
		batch.end();
	}
	
	public void renderLowDetailInfo(int index)
	{
		int strana;
		String nazev = screenTable[index].getNazev();
		nazev = nazev.toUpperCase();
		float poziceX, poziceY;
		float allRes = 660;
		float resX = allRes / 11;
		float resY = resX;
		int [] poleVel = new int[5];
		for (int i = 1; i < poleVel.length; i++)
		{
			poleVel[i] = -1;
		}
		int indexInv = 1;
		for (int i = 0; i < nazev.length(); i++)
		{
			if(nazev.charAt(i) == ' ')
			{
				poleVel[indexInv++] = i;
			}
		}
		String newNazev = nazev.charAt(0) + "";
		for (int i = 1; i < poleVel.length; i++)
		{
			if(poleVel[i] == -1)
			{
				break;
			}
			newNazev = newNazev + nazev.charAt(poleVel[i] + 1);
		}
		if(index >= 0 && index <= 9)
		{
			strana = 0;
			poziceX = index;
			poziceY = 57;
		}
		else if(index >= 10 && index <= 19)
		{
			strana = 1;
			poziceX = 15;
			poziceY = index % 10;
		}
		else if(index >= 20 && index <= 29)
		{
			strana = 2;
			poziceX = 10 - (index % 10);
			poziceY = allRes;
		}
		else
		{
			strana = 3;
			poziceX = allRes;
			poziceY = 10 - (index % 10);
		}
		font.getData().setScale(0.125f, 0.125f);
		switch(strana)
		{
		case 0:font.draw(batch, newNazev, allRes - (resX * poziceX), poziceY);
		break;
		case 1:font.draw(batch, newNazev, poziceX, (resY * poziceY) + resY);
		break;
		case 2:font.draw(batch, newNazev,allRes - (resX * poziceX) - 15, poziceY);
		break;
		case 3:font.draw(batch, newNazev, poziceX, (resY * poziceY) + resY);
		break;
		}
		font.getData().setScale(0.25f, 0.25f);
	}
	
	public void renderPlayerOnTable(float delta)
	{
		int [] poziceHracu = new int[screenHraci.length];
		for (int i = 0; i < screenHraci.length; i++)
		{
			poziceHracu[i] = screenHraci[i].getPosition();
		}
		int playerId = -1;
		float x = 0, y = 0;
		float widthA = boardWidth * widthOfPile;
		float widthB = widthOfInsidePile * boardWidth;
		float heightA = widthA;
		float heightObd = 15;
		float widthEcl = 15;
		float widthPlayer = 25;
		int addSpace = 0, space = 30;
		for(int i = 0; i < screenTable.length; i++)
		{
			int playerIndex = boardPlayerOwning(i, playerId);
			sr.begin(ShapeType.Filled);
			sr.setColor(Color.LIGHT_GRAY);
			if(i <= 10)
			{
				x = (widthA + ((10 - i) * widthB)) - widthB;
				y = boardHeight - heightObd;
			}
			else if(i > 10 && i <= 20)
			{
				int ind = i % 10;
				if(i == 20)
				{
					ind = 10;
				}
				x = 0;
				y = boardHeight - heightA - (ind * widthB);
			}
			else if(i > 20 && i <= 30)
			{
				int ind = i % 10;
				if(i == 30)
				{
					ind = 10;
				}
				x = (widthA + (ind * widthB)) - widthB;
				y = 0;
			}
			else if(i > 30 && i < 40)
			{
				int ind = i % 10;
				x = boardWidth - heightObd;
				y = heightA + (ind * widthB) - widthB;
			}
			if(playerIndex != -1)
			{
				if(i <= 9)
				{
					sr.rect(x + 1.5f, y - 1.5f, widthB - 3.0f, heightObd - 1.0f);
					sr.setColor(barvy[(barvy.length - 2) - playerIndex]);
					sr.ellipse(x + widthB * 0.5f - widthEcl * 0.5f, y - 1.5f, widthEcl, widthEcl);
				}
				else if(i > 10 && i < 20)
				{
					sr.rect(x + 1.5f, y + 1.5f, heightObd - 1.0f, widthB - 3.0f);
					sr.setColor(barvy[(barvy.length - 2) - playerIndex]);
					sr.ellipse(x + 1.5f, y + widthB * 0.5f - widthEcl * 0.5f, widthEcl - 1.5f, widthEcl - 1.5f);
				}
				else if(i > 20 && i < 30)
				{
					sr.rect(x + 1.5f, y + 1.5f, widthB - 3.0f, heightObd - 1.0f);
					sr.setColor(barvy[(barvy.length - 2) - playerIndex]);
					sr.ellipse(x + widthB * 0.5f - widthEcl * 0.5f, y + 1.5f, widthEcl, widthEcl);
				}
				else if(i > 30 && i < 40)
				{
					sr.rect(x - 1.5f, y + 1.5f, heightObd + 1.0f, widthB - 3.0f);
					sr.setColor(barvy[(barvy.length - 2) - playerIndex]);
					sr.ellipse(x - 1.5f, y + widthB * 0.5f - widthEcl * 0.5f, widthEcl, widthEcl);
				}
			}
			int zmena = -1;
			for (int j = 0; j < poziceHracu.length; j++)
			{
				if(i == poziceHracu[j])
				{
					//sr.setColor(barvy[(barvy.length - 2) - j]);
					if(i <= 10)
					{
						if(zmena != 0)
						{
							addSpace = 0;
						}
						sr.setColor(Color.BLACK);
						sr.ellipse((x + widthB * 0.5f - widthPlayer * 0.5f) - 2, (boardHeight - widthA * 0.5f - widthPlayer * 0.5f - addSpace) - 2, widthPlayer + 4, widthPlayer + 4);
						sr.setColor(barvy[(barvy.length - 2) - j]);
						sr.ellipse(x + widthB * 0.5f - widthPlayer * 0.5f, boardHeight - widthA * 0.5f - widthPlayer * 0.5f - addSpace, widthPlayer, widthPlayer);
						zmena = 0;
					}
					else if(i > 10 && i <= 20)
					{
						if(zmena != 1)
						{
							addSpace = 0;
						}
						sr.setColor(Color.BLACK);
						sr.ellipse((widthA * 0.5f - widthPlayer * 0.5f + addSpace) - 2, (y + widthB * 0.5f - widthPlayer * 0.5f) - 2, widthPlayer + 4, widthPlayer + 4);
						sr.setColor(barvy[(barvy.length - 2) - j]);
						sr.ellipse(widthA * 0.5f - widthPlayer * 0.5f + addSpace, y + widthB * 0.5f - widthPlayer * 0.5f, widthPlayer, widthPlayer);
						zmena = 1;
					}
					else if(i > 20 && i <= 30)
					{
						if(zmena != 2)
						{
							addSpace = 0;
						}
						sr.setColor(Color.BLACK);
						sr.ellipse((x + widthB * 0.5f - widthPlayer * 0.5f) - 2, (y + widthB * 0.5f + addSpace) - 2, widthPlayer + 4, widthPlayer + 4);
						sr.setColor(barvy[(barvy.length - 2) - j]);
						sr.ellipse(x + widthB * 0.5f - widthPlayer * 0.5f, y + widthB * 0.5f + addSpace, widthPlayer, widthPlayer);
						zmena = 2;
					}
					else if(i > 30 && i < 40)
					{
						if(zmena != 3)
						{
							addSpace = 0;
						}
						sr.setColor(Color.BLACK);
						sr.ellipse((boardWidth - widthA * 0.5f - widthPlayer * 0.5f - addSpace) - 2, (y + widthB * 0.5f - widthPlayer * 0.5f) - 2, widthPlayer + 4, widthPlayer + 4);
						sr.setColor(barvy[(barvy.length - 2) - j]);
						sr.ellipse(boardWidth - widthA * 0.5f - widthPlayer * 0.5f - addSpace, y + widthB * 0.5f - widthPlayer * 0.5f, widthPlayer, widthPlayer);
						zmena = 3;
					}
					addSpace += space;						
				}						
			}
			sr.end();
		}
	}
	
	public void renderPlayerStats(float delta)
	{
		float widthInfo = width * info;
		float heightInfoA = height * 0.7f;
		//float heightInfoB = height * 0.3f;
		int playerCount = screenHraci.length;
		float heightStat = heightInfoA / playerCount;
		sr.setColor(Color.BLACK);
		sr.begin(ShapeType.Filled);
			float y = 0;
			for (int i = 0; i < playerCount; i++)
			{
				sr.setColor(Color.BLACK);
				sr.rect(boardWidth, y, widthInfo, heightStat);
				sr.setColor(Color.WHITE);
				sr.rect(boardWidth + 1.5f, y + 1.5f, widthInfo - 3.0f, heightStat - 3.0f);
				y += heightStat;
			}
			sr.setColor(Color.BLACK);
			sr.rectLine(boardWidth + widthInfo, y, boardWidth, y, 3);
		sr.end();
		batch.begin();
			y = 0;
			for (int i = 0; i < playerCount; i++)
			{
				font.getData().setScale(0.25f);
				font.setColor(barvy[(barvy.length - 2) - i]);
				font.draw(batch, screenHraci[i].getName(), boardWidth + 5, boardWidth - (y + 5));
				font.getData().setScale(0.18f);
				font.setColor(Color.BLACK);
				font.draw(batch, "$" + screenHraci[i].getMoney(), boardWidth + 5, boardWidth - (y + 40));
				int suma = 0;
				for (int j = 0; j < screenTable.length; j++)
				{
					if(screenTable[j].getOwnPlayerID() == screenHraci[i].getId())
					{
						suma++;
					}
				}
				font.draw(batch, suma + " pozemku", boardWidth + 5 + widthInfo * 0.4f, boardWidth - (y + 40));
				if(moneyDelay[i] != 0)
				{
					if(moneyDelay[i] > 0)
					{
						font.setColor(Color.GREEN);
						font.draw(batch, "+$" + moneyDelay[i], boardWidth + 5, boardWidth - (y + 80));
					}
					else
					{
						font.setColor(Color.RED);
						font.draw(batch, "-$" + Math.abs(moneyDelay[i]), boardWidth + 5, boardWidth - (y + 80));
					}
					if(statsDelay == 0)
					{
						moneyDelay[i] = 0;
					}
				}
				if(lastThrow[i][0] != -1 && lastThrow[i][1] != -1)
				{
					batch.draw(Assets.dice[lastThrow[i][0]], boardWidth + widthInfo * 0.5f, boardWidth - (y + 120), 40, 40);
					batch.draw(Assets.dice[lastThrow[i][1]], boardWidth + widthInfo * 0.5f + 45, boardWidth - (y + 120), 40, 40);
				}
				y += heightStat;
			}
		batch.end();
	}
	
	public void renderButtons(float delta)
	{
		drawButton(765, 555, 150, 50, "LEAVE");
		drawButton(765, 635, 150, 50, "DISCON");
		
		if(Gdx.input.isTouched() && delay == 0)
		{
			if(isObjectTouched(touch, 765, 555, 150, 50))
			{
				//System.out.println("LEAVE");
				Monopoly.LoginScreen.tc.sendMessageToServer("$leave!0#");
				hide = true;
				LobbyScreen.drawAllInfo = false;
				delaySet();
			}
			else if(isObjectTouched(touch, 765, 635, 150, 50))
			{
				System.out.println("DISCON");
				//Discon se posle z funkce hide();!
				hide = false;
				LobbyScreen.drawAllInfo = false;
				LobbyScreen.ready = false;
				game.setScreen(Monopoly.LoginScreen);
				delaySet();
			}
		}
	}
	
	public void drawButton(float x, float y, float widthX, float widthY, String text)
	{
		float spaceTextX = 5;
		float spaceTextY = (widthY / 2) - 15;//15 je pro velikost pisma
		sr.begin(ShapeType.Filled);
			sr.setColor(Color.LIGHT_GRAY);
			sr.rect(x, y, widthX, widthY);//Normalne widthH vykresluje arc nahoru, ne dolu!
		sr.end();
		sr.begin(ShapeType.Line);
			sr.setColor(Color.BLACK);
			sr.rect(x, y, widthX, widthY);
		sr.end();
		batch.begin();
			font.setColor(Color.BLACK);
			font.getData().setScale(0.25f);
			font.draw(batch, text, x + spaceTextX, (height - y) - spaceTextY);
		batch.end();
	}
	
	public void renderWaitingRoom(float delta)
	{
		float widthInfo = width * info;
		float playerDivides = widthInfo / waited.length;
		float playerSpace = playerDivides * 0.6f;
		float playerRect = playerDivides * 0.4f;
		
		float x,y;
		sr.begin(ShapeType.Line);
			x = playerSpace;
			y = (height * 0.5f) - (playerRect * 0.5f);
			for(int i = 0; i < waited.length; i++)
			{
				sr.setColor(Color.BLACK);
				sr.rect(x, y, playerRect, playerRect);
				x += playerRect + playerSpace;
			}
		sr.end();
		font.getData().setScale(0.25f);
		font.setColor(Color.BLACK);
		batch.begin();
			x = playerSpace;
			y = (height * 0.5f) - (playerRect * 0.5f);
			int j = 0;
			for(int i = 0; i < waited.length; i++)
			{
				System.out.println("waiting render i: " + i);
				System.out.println(Monopoly.LobbyScreen.currentLobby[i]);
				if(!Monopoly.LobbyScreen.currentLobby[i].equals("--empty--"))
				{
					font.draw(batch, Monopoly.LobbyScreen.currentLobby[i], x, y);
					if(waited[j] == 1)
					{
						font.draw(batch, Monopoly.LobbyScreen.currentLobby[i], x, y + playerRect + 20);
					}
					x += playerRect + playerSpace;
				}
			}
		batch.end();
	}
	
	public void renderStartTurn(float delta)
	{
		float holeInside = boardWidth * insidePile;
		float widthA = boardWidth * widthOfPile;
		float sizeOfInfo = 85;
		float edges = (100.0f - sizeOfInfo) * 0.5f;
		float widthEdges = holeInside * (edges * 0.01f);
		float widthInfo = holeInside * (sizeOfInfo * 0.01f);
		float widthButton = 200;
		float heightButton = 50;
		if(pozice == intKlient)
		{
			sr.begin(ShapeType.Filled);
			sr.setColor(Color.LIGHT_GRAY);
			sr.rect(widthA + widthEdges + widthInfo * 0.5f - widthButton * 0.5f - 3, widthA + widthEdges + widthInfo * 0.75f - 3, widthButton + 6, heightButton + 6);
			sr.setColor(Color.WHITE);
			sr.rect(widthA + widthEdges + widthInfo * 0.5f - widthButton * 0.5f, widthA + widthEdges + widthInfo * 0.75f, widthButton, heightButton);
			sr.end();			
		}
		batch.begin();
			font.getData().setScale(0.25f);
			font.setColor(barvy[(barvy.length - 2) - pozice]);
			font.draw(batch, screenHraci[pozice].getName() + " je na tahu:", widthA + widthEdges + widthInfo * 0.18f, boardHeight - (widthA + widthEdges + widthInfo * 0.25f));
			font.setColor(Color.BLACK);
			if(pozice == intKlient)
			{
				font.draw(batch, "Start kola", widthA + widthEdges + widthInfo * 0.5f - widthButton * 0.5f + 20, boardHeight - (widthA + widthEdges + widthInfo * 0.75f + 15));				
			}
		batch.end();
		if(delay == 0 && pozice == intKlient)
		{
			if(isObjectTouched(touch, widthA + widthEdges + widthInfo * 0.5f - widthButton * 0.5f, widthA + widthEdges + widthInfo * 0.75f, widthButton, heightButton))
			{
				//Tlacitko ze hazim kostkou
				//Monopoly.LoginScreen.sendToThread("GUI", "$game!roll#");
				Monopoly.LoginScreen.tc.sendMessageToServer("$game!roll#");
				//transmission("s");
				delaySet();
				startRound = false;
			}			
		}
	}
	
	public void renderFullInfo(float delta)
	{
		Card karta = screenTable[screenHraci[pozice].getPosition()];
		int type = -1;
		if(karta instanceof Property)
		{
			type = 0;
		}
		else if(karta instanceof Railroad)
		{
			type = 1;
		}
		else if(karta instanceof Utility)
		{
			type = 2;
		}
		float holeInside = boardWidth * insidePile;
		float widthA = boardWidth * widthOfPile;
		float sizeOfInfo = 85;
		float sizeOfInfoEdges = 5;
		float sizeOfInfoA = 40;
		float sizeOfInfoB = 60;
		float edges = (100.0f - sizeOfInfo) * 0.5f;
		float widthEdges = holeInside * (edges * 0.01f);
		float widthInfo = holeInside * (sizeOfInfo * 0.01f);
		float widthInfoEdges = widthInfo * sizeOfInfoEdges * 0.01f;
		float widthInfoA = (widthInfo - (3 * widthInfoEdges)) * (sizeOfInfoA * 0.01f);
		float widthInfoB = (widthInfo - (3 * widthInfoEdges)) * (sizeOfInfoB * 0.01f);
		float spaceInfoA = widthInfo * 0.35f;
		float spaceInfoB = widthInfo * 0.25f;
		float heightInfoA = widthInfo * 0.55f;
		float heightInfoB = widthInfo * 0.65f;
		float heightButton = widthInfo * 0.075f;
		sr.begin(ShapeType.Filled);	
			if(type != -1)//!!MOZNA CHYBA
			{
				sr.setColor(Color.LIGHT_GRAY);
				sr.rect(widthA + widthEdges, widthA + widthEdges, widthInfo, widthInfo);
				sr.setColor(Color.WHITE);
				sr.rect(widthA + widthEdges + widthInfoEdges, widthA + widthEdges + spaceInfoA, widthInfoA, heightInfoA);
				sr.rect(widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + spaceInfoB, widthInfoB, heightInfoB);
				if(type == 0)
				{
					int mod1 = screenHraci[pozice].getPosition() / 10;
					int mod2;
					if(mod1 == 0)
					{
						mod1 = 1;
						mod2 = poziceBarev[mod1].length - (screenHraci[pozice].getPosition() % 10);						
					}
					else if(mod1 == 1)
					{
						mod1 = 2;
						mod2 = poziceBarev[mod1].length - screenHraci[pozice].getPosition() % 10;
					}
					else if(mod1 == 2)
					{
						mod1 = 0;
						mod2 = screenHraci[pozice].getPosition() % 10;
					}
					else
					{
						mod1 = 3;
						mod2 = screenHraci[pozice].getPosition() % 10;
					}
					int barva = poziceBarev[mod1][mod2 - 1];
					sr.setColor(barvy[barva]);
					sr.rect(widthA + widthEdges + widthInfoEdges, widthA + widthEdges + spaceInfoA, widthInfoA, 60);
					sr.rect(widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + spaceInfoB, widthInfoB, 60);				
					
				}
				if(karta.getOwnPlayerID() == -1 && pozice == intKlient)
				{
					sr.setColor(Color.WHITE);
					sr.rect(widthA + widthEdges + widthInfoEdges, widthA + widthEdges + spaceInfoA + heightInfoA + 7, widthInfoA, heightButton);
					sr.rect(widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + spaceInfoA + heightInfoA + 7, widthInfoB, heightButton);					
				}
			}
		sr.end();
		batch.begin();
			font.setColor(Color.BLACK);
			font.getData().setScale(0.25f);
			if(type == 0)
			{
				if(((Property)karta).getOwnPlayerID() == -1)
				{
					font.draw(batch, "Naprodej",  widthA + widthEdges + (widthInfo * 0.5f) - 60, widthA + widthEdges + widthInfo - 20);
					font.draw(batch, ((Property)karta).getPropertyCost() + "$",  widthA + widthEdges + (widthInfo * 0.5f) - 25, widthA + widthEdges + widthInfo - 60);
					if(pozice == intKlient)
					{
						font.draw(batch, "Koupit", widthA + widthEdges + widthInfoEdges, boardHeight - (widthA + widthEdges + spaceInfoA + heightInfoA + 11));
						font.draw(batch, "Aukce", widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, boardHeight - (widthA + widthEdges + spaceInfoA + heightInfoA + 11));						
					}
				}
				font.getData().setScale(0.14f);
				//INFO A
				font.draw(batch, karta.getNazev(),  widthA + widthEdges + widthInfoEdges, widthA + widthEdges + heightInfoA * 0.75f);
				font.draw(batch, "Cena $" + ((Property)karta).getPropertyCost(),  widthA + widthEdges + widthInfoEdges + 35, widthA + widthEdges + heightInfoA * 0.25f);
				//INFO B
				font.draw(batch, karta.getNazev(),  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * 1.05f);
				font.draw(batch, "Najem $" + ((Property)karta).getHousesAndHotelRent()[0],  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * 0.9f);
				float ubytek = 0.1f;
				for (int i = 1; i < ((Property)karta).getHousesAndHotelRent().length - 1; i++)
				{
					font.draw(batch, "S " + i + " domy/em	   $" + ((Property)karta).getHousesAndHotelRent()[i],  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (0.9f - ubytek));
					ubytek += 0.1f;
				}
				font.draw(batch, "S hotelem $" + ((Property)karta).getHousesAndHotelRent()[5],  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA + 45, widthA + widthEdges + heightInfoB * (0.9f - ubytek));
				ubytek += 0.1f;
				font.draw(batch, "Cena hotelu/domu $" + ((Property)karta).getHouseCost(),  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (0.9f - ubytek));
			}
			else if(type == 1)
			{
				if(((Railroad)karta).getOwnPlayerID() == -1)
				{
					font.draw(batch, "Naprodej",  widthA + widthEdges + (widthInfo * 0.5f) - 60, widthA + widthEdges + widthInfo - 20);
					font.draw(batch, "200$",  widthA + widthEdges + (widthInfo * 0.5f) - 25, widthA + widthEdges + widthInfo - 60);
					if(pozice == intKlient)
					{
						font.draw(batch, "Koupit", widthA + widthEdges + widthInfoEdges, boardHeight - (widthA + widthEdges + spaceInfoA + heightInfoA + 11));
						font.draw(batch, "Aukce", widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, boardHeight - (widthA + widthEdges + spaceInfoA + heightInfoA + 11));						
					}
				}
				font.getData().setScale(0.14f);
				//INFO A
				font.draw(batch, "Railroad",  widthA + widthEdges + widthInfoEdges, widthA + widthEdges + heightInfoA * 0.75f);
				font.draw(batch, karta.getNazev(),  widthA + widthEdges + widthInfoEdges, widthA + widthEdges + heightInfoA * 0.25f);
				//INFO B
				font.draw(batch, karta.getNazev(),  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * 1.05f);
				font.draw(batch, "Najem                   $25",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (0.8f));
				font.draw(batch, "Pokud 2 Stanice  $50",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (0.7f));
				font.draw(batch, "Pokud 3 Stanice $100",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (0.6f));
				font.draw(batch, "Pokud 4 Stanice $200",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (0.5f));
			}
			else if(type == 2)
			{
				if(((Utility)karta).getOwnPlayerID() == -1)
				{
					font.draw(batch, "Naprodej",  widthA + widthEdges + (widthInfo * 0.5f) - 60, widthA + widthEdges + widthInfo - 20);
					font.draw(batch, "150$",  widthA + widthEdges + (widthInfo * 0.5f) - 25, widthA + widthEdges + widthInfo - 60);
					if(pozice == intKlient)
					{
						font.draw(batch, "Koupit", widthA + widthEdges + widthInfoEdges, boardHeight - (widthA + widthEdges + spaceInfoA + heightInfoA + 11));
						font.draw(batch, "Aukce", widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, boardHeight - (widthA + widthEdges + spaceInfoA + heightInfoA + 11));						
					}
				}
				font.getData().setScale(0.14f);
				//INFO A
				font.draw(batch, karta.getNazev(),  widthA + widthEdges + widthInfoEdges, widthA + widthEdges + heightInfoA * 0.75f);
				font.draw(batch, "Cena $150",  widthA + widthEdges + widthInfoEdges + 35, widthA + widthEdges + heightInfoA * 0.25f);
				//INFO B
				font.draw(batch, karta.getNazev(),  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * 1.05f);
				font.draw(batch, "Pokud mate jednu 'Sluzbu'",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (0.8f));
				font.draw(batch, "najem bude 4 nasobek",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (0.7f));
				font.draw(batch, "hodu na kostce.",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (0.6f));
				font.draw(batch, "Pokud mate obe 'Sluzby'",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (0.5f));
				font.draw(batch, "najem bude 10 nasobek",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (0.4f));
				font.draw(batch, "hodu na kostce.",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (0.3f));
			}
			if(karta.getOwnPlayerID() == -1)
			{
				if(!notClick && delay == 0 && pozice == intKlient)
				{
					if(isObjectTouched(touch, widthA + widthEdges + widthInfoEdges, widthA + widthEdges + spaceInfoA + heightInfoA + 7, widthInfoA, heightButton))
					{
						//Zprava, ze chci pozemek koupit
						//Monopoly.LoginScreen.sendToThread("GUI", "$game!buy#");
						Monopoly.LoginScreen.tc.sendMessageToServer("$game!buy#");
						//transmission("k");
						notClick = true;
						delaySet();
					}
					else if(isObjectTouched(touch, widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + spaceInfoA + heightInfoA + 7, widthInfoB, heightButton))
					{
						//Zprava, ze chci o pozemek hrat v aukci
						//Monopoly.LoginScreen.sendToThread("GUI", "$game!auction#");
						Monopoly.LoginScreen.tc.sendMessageToServer("$game!auction#");
						//transmission("a");
						aukce = true;
						aukcePozice = pozice;
						bids = new int[screenHraci.length];
						folds = 0;
						aukceMax = 0;
						bid = aukceMax;
						delaySet();
					}
				}
			}
		batch.end();
	}
	
	public void renderAukceInfo(float delta)
	{
		Card karta = screenTable[screenHraci[pozice].getPosition()];
		int type = -1;
		if(karta instanceof Property)
		{
			type = 0;
		}
		else if(karta instanceof Railroad)
		{
			type = 1;
		}
		else if(karta instanceof Utility)
		{
			type = 2;
		}
		float holeInside = boardWidth * insidePile;
		float widthA = boardWidth * widthOfPile;
		float sizeOfInfo = 85;
		float sizeOfInfoEdges = 5;
		float sizeOfInfoA = 40;
		float sizeOfInfoB = 60;
		float edges = (100.0f - sizeOfInfo) * 0.5f;
		float widthEdges = holeInside * (edges * 0.01f);
		float widthInfo = holeInside * (sizeOfInfo * 0.01f);
		float widthInfoEdges = widthInfo * sizeOfInfoEdges * 0.01f;
		float widthInfoA = (widthInfo - (3 * widthInfoEdges)) * (sizeOfInfoA * 0.01f);
		float widthInfoB = (widthInfo - (3 * widthInfoEdges)) * (sizeOfInfoB * 0.01f);
		float heightInfoA = widthInfo * 0.55f;
		float heightInfoB = widthInfo * 0.65f;
		float widthButton = widthInfo * 0.3f;
		float heightButton = widthInfo * 0.075f;
		sr.begin(ShapeType.Filled);
			sr.setColor(Color.LIGHT_GRAY);
			sr.rect(widthA + widthEdges, widthA + widthEdges, widthInfo, widthInfo);
			sr.setColor(Color.WHITE);
			sr.rect(widthA + widthEdges + widthInfoEdges, widthA + widthEdges + widthInfoEdges + heightInfoB - heightInfoA, widthInfoA, heightInfoA);
			sr.rect(widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + widthInfoEdges, widthInfoB, heightInfoB);
			sr.rect(widthA + widthEdges + 2 * widthInfoEdges + widthInfoA + 100, widthA + widthEdges + widthInfoEdges + heightInfoB + 50, widthButton, heightButton);
			sr.rect(widthA + widthEdges + 2 * widthInfoEdges + widthInfoA + 100, widthA + widthEdges + widthInfoEdges + heightInfoB + 50 + heightButton + 7, widthButton, heightButton);
			sr.rect(widthA + widthEdges + widthInfoEdges, widthA + widthEdges + widthInfoEdges + heightInfoB + 50, widthInfoA, 2*heightButton + 7);
			sr.rect(widthA + widthEdges + widthInfoEdges + widthInfoA + 7, widthA + widthEdges + widthInfoEdges + heightInfoB + 50, widthButton * 0.33f, heightButton);
			sr.rect(widthA + widthEdges + widthInfoEdges + widthInfoA + 7, widthA + widthEdges + widthInfoEdges + heightInfoB + 57 + heightButton, widthButton * 0.33f, heightButton);
			if(type == 0)
			{
				sr.setColor(Color.RED);
				sr.rect(widthA + widthEdges + widthInfoEdges, widthA + widthEdges + + widthInfoEdges + heightInfoB - heightInfoA, widthInfoA, 60);
				sr.rect(widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + widthInfoEdges, widthInfoB, 60);				
				
			}
		sr.end();
		batch.begin();
			font.setColor(Color.BLACK);
			if(type == 0)
			{
				font.getData().setScale(0.14f);
				//INFO A
				font.draw(batch, karta.getNazev(),  widthA + widthEdges + widthInfoEdges, widthA + widthEdges + widthInfoEdges + heightInfoA);
				font.draw(batch, "Cena $" + ((Property)karta).getPropertyCost(),  widthA + widthEdges + widthInfoEdges + 35, widthA + widthEdges + widthInfoEdges + heightInfoA * 0.52f);
				//INFO B
				font.draw(batch, karta.getNazev(),  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * 1.35f);
				font.draw(batch, "Najem $" + ((Property)karta).getHousesAndHotelRent()[0],  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * 1.2f);
				float ubytek = 0.1f;
				for (int i = 1; i < ((Property)karta).getHousesAndHotelRent().length - 1; i++)
				{
					font.draw(batch, "S " + i + " domy/em	   $" + ((Property)karta).getHousesAndHotelRent()[i],  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (1.2f - ubytek));
					ubytek += 0.1f;
				}
				font.draw(batch, "S hotelem $" + ((Property)karta).getHousesAndHotelRent()[5],  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA + 45, widthA + widthEdges + heightInfoB * (1.2f - ubytek));
				ubytek += 0.1f;
				font.draw(batch, "Cena hotelu/domu $" + ((Property)karta).getHouseCost(),  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (1.2f - ubytek));
			}
			else if(type == 1)
			{
				font.getData().setScale(0.14f);
				//INFO A
				font.draw(batch, "Railroad",  widthA + widthEdges + widthInfoEdges, widthA + widthEdges + widthInfoEdges + heightInfoA + 50);
				font.draw(batch, karta.getNazev(),  widthA + widthEdges + widthInfoEdges, widthA + widthEdges + widthInfoEdges + heightInfoA * 0.52f + 50);
				//INFO B
				font.draw(batch, karta.getNazev(),  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * 1.35f);
				font.draw(batch, "Najem                   $25",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (1.1f));
				font.draw(batch, "Pokud 2 Stanice  $50",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (1.0f));
				font.draw(batch, "Pokud 3 Stanice $100",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (0.9f));
				font.draw(batch, "Pokud 4 Stanice $200",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (0.8f));
			}
			else if(type == 2)
			{
				font.getData().setScale(0.14f);
				//INFO A
				font.draw(batch, karta.getNazev(),  widthA + widthEdges + widthInfoEdges, widthA + widthEdges + widthInfoEdges + heightInfoA);
				font.draw(batch, "Cena $150",  widthA + widthEdges + widthInfoEdges + 35, widthA + widthEdges + widthInfoEdges + heightInfoA * 0.52f);
				//INFO B
				font.draw(batch, karta.getNazev(),  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * 1.35f);
				font.draw(batch, "Pokud mate jednu 'Sluzbu'",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (1.1f));
				font.draw(batch, "najem bude 4 nasobek",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (1.0f));
				font.draw(batch, "hodu na kostce.",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (0.9f));
				font.draw(batch, "Pokud mate obe 'Sluzby'",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (0.8f));
				font.draw(batch, "najem bude 10 nasobek",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (0.7f));
				font.draw(batch, "hodu na kostce.",  widthA + widthEdges + 2 * widthInfoEdges + widthInfoA, widthA + widthEdges + heightInfoB * (0.6f));
			}
			//MOZNOSTI AUKCE
			font.getData().setScale(0.18f);
			font.draw(batch, screenHraci[aukcePozice].getName() + " prihazuje:",  widthA + widthEdges + widthInfoEdges, widthA + widthEdges + widthInfoEdges + 100);
			font.getData().setScale(0.25f);
			font.draw(batch, "Aukce",  widthA + widthEdges + 2 *widthInfoEdges + widthInfoA + 125, widthA + widthEdges + widthInfoEdges + 100);
			font.draw(batch, "+", widthA + widthEdges + widthInfoEdges + widthInfoA + 22, widthA + widthEdges + widthInfoEdges + heightButton * 1.8f);
			font.draw(batch, "-", widthA + widthEdges + widthInfoEdges + widthInfoA + 22, widthA + widthEdges + widthInfoEdges + heightButton * 0.6f);
			font.draw(batch, "Prihodit", widthA + widthEdges + 2 * widthInfoEdges + widthInfoA + 100, widthA + widthEdges + widthInfoEdges + 60);
			font.draw(batch, "Slozit", widthA + widthEdges + 2 * widthInfoEdges + widthInfoA + 100, widthA + widthEdges + widthInfoEdges + 53 - heightButton);
			if(!notClick && delay == 0)
			{
				if(isObjectTouched(touch, widthA + widthEdges + widthInfoEdges + widthInfoA + 7, widthA + widthEdges + widthInfoEdges + heightInfoB + 50, widthButton * 0.33f, heightButton))
				{
					if(bid < screenHraci[aukcePozice].getMoney())
					{
						bid++;						
					}
				}
				else if(isObjectTouched(touch, widthA + widthEdges + widthInfoEdges + widthInfoA + 7, widthA + widthEdges + widthInfoEdges + heightInfoB + 57 + heightButton, widthButton * 0.33f, heightButton))
				{
					if(bid > aukceMax)
					{
						bid--;						
					}
				}
				if(isObjectTouched(touch, widthA + widthEdges + 2 * widthInfoEdges + widthInfoA + 100, widthA + widthEdges + widthInfoEdges + heightInfoB + 50, widthButton, heightButton))
				{
					//Zprava, ze prihazuji na pozemek <bid>
					//Monopoly.LoginScreen.sendToThread("GUI", "$aukce!add!" + bid + "#");
					System.out.println("KLIKAM PRIDAT V AUKCI");
					Monopoly.LoginScreen.tc.sendMessageToServer("$aukce!add!" + bid + "#");
					//transmission(bid + "");
					notClick = true;
					delaySet();
				}
				else if(isObjectTouched(touch, widthA + widthEdges + 2 * widthInfoEdges + widthInfoA + 100, widthA + widthEdges + widthInfoEdges + heightInfoB + 50 + heightButton + 7, widthButton, heightButton))
				{
					//Zprava, ze ukoncuji ucast na aukci
					//Monopoly.LoginScreen.sendToThread("GUI", "$aukce!end!0#");
					Monopoly.LoginScreen.tc.sendMessageToServer("$aukce!end!0#");
					//transmission("k");
					notClick = true;
					delaySet();
				}
			}
			font.draw(batch, "$" + bid, widthA + widthEdges + widthInfoEdges, widthA + widthEdges + widthInfoEdges + heightButton + 5);
		batch.end();
	}
	
	public void renderChestChanceInfo(float delta)
	{
		float holeInside = boardWidth * insidePile;
		float sizeOfInfo = 85;
		float sizeOfInfoEdges = 5;
		float spaceUp = 10;
		float edges = (100.0f - sizeOfInfo) * 0.5f;
		float widthA = boardWidth * widthOfPile;
		float widthEdges = holeInside * (edges * 0.01f);
		float widthInfo = holeInside * (sizeOfInfo * 0.01f);
		float widthInfoEdges = widthInfo * sizeOfInfoEdges * 0.01f;
		float widthInfoC = (widthInfo - (2 * widthInfoEdges));
		float heightSpaceUp = widthInfo * spaceUp * 0.01f;
		float heightInfoC = widthInfo * 0.65f;
		float widthButton = widthInfo * 0.25f;
		float heightButton = widthInfo * 0.1f;
		sr.begin(ShapeType.Filled);
			sr.setColor(Color.LIGHT_GRAY);
			sr.rect(widthA + widthEdges, widthA + widthEdges, widthInfo, widthInfo);
			sr.setColor(Color.GOLDENROD);
			sr.rect(widthA + widthEdges + widthInfoEdges, widthA + widthEdges + widthInfoEdges + heightSpaceUp, widthInfoC, heightInfoC);
			sr.setColor(Color.WHITE);
			sr.rect(widthA + widthEdges + widthInfoEdges + widthInfoC * 0.5f - widthButton * 0.5f, widthA + widthEdges + widthInfoEdges + heightSpaceUp + heightInfoC + 15, widthButton, heightButton);
		sr.end();
		sr.end();
		batch.begin();
			font.setColor(Color.BLACK);
			font.getData().setScale(0.25f);
			font.draw(batch, screenHraci[cCTPlayer].getName() + " si lizl:", widthA + widthEdges + widthInfoEdges, boardWidth - (widthA + widthEdges + heightSpaceUp * 0.5f));
			if(cCTtype == 0)
			{
				font.draw(batch, "Community Chest", widthA + widthEdges + widthInfoEdges + 70, boardWidth - (widthA + widthEdges + widthInfoEdges + heightSpaceUp));
			}
			if(cCTtype == 1)
			{
				font.draw(batch, "Chance", widthA + widthEdges + widthInfoEdges + 150, boardWidth - (widthA + widthEdges + widthInfoEdges + heightSpaceUp + 10));
			}
			if(cCTtype == 2)
			{
				font.draw(batch, "Community Chest", widthA + widthEdges + widthInfoEdges + 50, boardWidth - (widthA + widthEdges + widthInfoEdges + heightSpaceUp));
			}
			font.getData().setScale(0.20f);
			String [] vys = chestChanceTaxText(drop);
			int posun = 35;
			for (int i = 0; i < vys.length; i++)
			{
				font.draw(batch, vys[i], widthA + widthEdges + widthInfoEdges, boardWidth - (widthA + widthEdges + widthInfoEdges + heightSpaceUp + 125 + (i * posun)));				
			}
			font.getData().setScale(0.25f);
			font.draw(batch, "OK", widthA + widthEdges + widthInfoEdges + widthInfoC * 0.5f - 18, boardWidth - (widthA + widthEdges + widthInfoEdges + heightSpaceUp + heightInfoC + 24));
		batch.end();
		if(!notClick && delay == 0)
		{
			if(isObjectTouched(touch, widthA + widthEdges + widthInfoEdges + widthInfoC * 0.5f - widthButton * 0.5f, widthA + widthEdges + widthInfoEdges + heightSpaceUp + heightInfoC + 15, widthButton, heightButton))
			{
				chestChance = false;
				delaySet();
			}
		}
	}
	
	public static String [] chestChanceTaxText(String input)
	{
		String [] vys = new String[]{"CHYBA " + cCTtype + " " + input};
		switch(cCTtype)
		{
		case 0://chest
			   switch(input.charAt(0))
			   {
			   case '+':return new String[]{screenHraci[cCTPlayer].getName() + " ziskal od banky " + input.substring(1) + "$"};
			   case '-':return new String[]{screenHraci[cCTPlayer].getName() + " musi zaplatit","bance " + input.substring(1) + "$"};
			   case 'o':return new String[]{screenHraci[cCTPlayer].getName() + " ziskal kartu utek z vezeni,",
					   "tato karta bude pouzita, pokud","se hrac ocitne ve vezeni.","Po pouziti tuto kartu hrac ztraci!"};
			   case 'i':return new String[]{screenHraci[cCTPlayer].getName() + " odchazi do vezeni!"};
			   case 'a':return new String[]{"Kazdy hrac musi zaplatit " + screenHraci[cCTPlayer].getName(),"                         " + input.substring(1) + "$"};
			   case 'h':String [] pom = Assets.separeter(input.substring(1), ';');
				        return new String[]{screenHraci[cCTPlayer].getName() + " musi zaplatit bance za","kazdy svuj hotel("+ pom[0] +"$)"
			   				 + " a kazdy","svuj dum("+ pom[1] +"$)"};
			   }
			   break;
		case 1://chance
			   switch(input.charAt(0))
			   {
			   case '+':return new String[]{screenHraci[cCTPlayer].getName() + " ziskal od banky " + input.substring(1) + "$"};
			   case '-':return new String[]{screenHraci[cCTPlayer].getName() + " musi zaplatit","bance " + input.substring(1) + "$"};
			   case 'c':return new String[]{screenHraci[cCTPlayer].getName() + " musi zaplatit kazdemu","hraci " + input.substring(1) + "$"};
			   case 'l':return new String[]{screenHraci[cCTPlayer].getName() + " se presunul na pozici",screenTable[screenHraci[cCTPlayer].getPosition()].getNazev()};
			   case 'k':return new String[]{screenHraci[cCTPlayer].getName() + " se posouva o ", input.substring(1) + " misto/a zpet"};
			   case 'j':if(input.charAt(1) == 'i')
			   			{
				   		    return new String[]{screenHraci[cCTPlayer].getName() + " odchazi do vezeni!"};
			   			}
			   			else if(input.charAt(1) == 'o')
			   			{
			   				return new String[]{screenHraci[cCTPlayer].getName() + " ziskal kartu utek z vezeni,",
			 					   "tato karta bude pouzita, pokud","se hrac ocitne ve vezeni.","Po pouziti tuto kartu hrac ztraci!"};
			   			}
			   			else
			   			{
			   				return new String[]{ "CHYBA " + input};
			   			}
			   case 't':return new String[]{screenHraci[cCTPlayer].getName() + " zaplatil bance dane","v hodnote " + input.substring(1) + "$"};
			   case 'u':return new String[]{screenHraci[cCTPlayer].getName() + " se presunul na nejblizsi","sluzbu " + screenTable[screenHraci[cCTPlayer].getPosition()].getNazev()};
			   case 'r':return new String[]{screenHraci[cCTPlayer].getName() + " se presunul na nejblizsi","nadrazi " + screenTable[screenHraci[cCTPlayer].getPosition()].getNazev()};
			   case 'h':String [] pom = Assets.separeter(input.substring(1), ';');
				        return new String[]{screenHraci[cCTPlayer].getName() + " musi zaplatit bance za","kazdy svuj hotel("+ pom[0] +"$)"
			   				 + " a kazdy","svuj dum("+ pom[1] +"$)"};
			   }
			   break;
		case 2:break;
		}
		return vys;
	}
	
	public static synchronized void transmission(String text)
	{
		//System.out.println("TRANSMISSION CLIENT " + text);
		//TRANSMISSION!!!
		Table.soket.add(text);
		Table.packet = true;
		//TRANSMISSION!!!
	}
	
	public synchronized void recognizeInputAndUpdate()
	{
		if(pack)
		{
			/*
			for (int i = 0; i < gameInput.size(); i++)
			{
				System.out.println(gameInput.get(i));
			}
			*/
			String input = gameInput.get(0);
			gameInput.remove(0);
			if(gameInput.size() == 0)
			{
				pack = false;
			}
			//System.out.println("PRESEL PRES gameInpput inicializaci " + input);
			newStatus(input);
		}
	}
	
	public static void newStatus(String input)
	{
		System.out.println("Novy status " + input);
		String [] vstup = Assets.separeter(input, '!');
		//System.out.println("PRED SWITCHEM " + input);
		switch(vstup[0].charAt(0))
		{
		case 's':int hodA = 0, hodB = 0;
				try
				{
					hodA = Integer.parseInt(vstup[1]);
					hodB = Integer.parseInt(vstup[2]);
				}
				catch(NumberFormatException e)
				{
					System.out.println("Neni cislo!!");
					break;
				}
				//System.out.println("Hody jsou: " + hodA + " ; " + hodB);
				int poz;
				//System.out.println("Momentalni pozice " + poz);
				if(screenHraci[pozice].getPosition() + hodA + hodB > 39)
				{
					screenHraci[pozice].addMoney(200);
					poz = (screenHraci[pozice].getPosition() + hodA + hodB) - 40;
					screenHraci[pozice].setPosition(poz);
				}
				else
				{
					screenHraci[pozice].setPosition(screenHraci[pozice].getPosition() + hodA + hodB);			
				}
				lastThrow[pozice][0] = hodA - 1;
				lastThrow[pozice][1] = hodB - 1;
				notClick = false;
				break;
		case 'n':startRound = true;
			     break;
		case 'j':screenHraci[pozice].jail = true;
				 screenHraci[pozice].setPosition(10);
				 break;
		case 'e':pozice++;
				 if(pozice >= screenHraci.length)
				 {
					 pozice = 0;
				 }
				 boolean nasel = true;
				 while(nasel)
				 {
					 if(skipHraci[pozice] == 1)
					 {
						 pozice++;
						 if(pozice >= screenHraci.length)
						 {
							 pozice = 0;
						 }
						 continue;
					 }
					 else if(pozice >= screenHraci.length)
					 {
						 pozice = 0;
					 }
					 else
					 {
						 nasel = false;
					 }
				 }
				 startRound = true;
				 // System.out.println("Pozice zmena z " + (pozice - 1) + " na " + pozice);
				 break;
		case 'b':int indexProp, id, hracInd;
				try
				{
					hracInd = Integer.parseInt(vstup[1]);
					indexProp = Integer.parseInt(vstup[2]);
				}
				catch(NumberFormatException e)
				{
					System.out.println("!b!index!cislo! - Neni cislo!!");
					break;
				}
				//DODELAT KONTROLU, JESTLI JINY HRAC UZ NEVLASTNI POZEMEK
				if(screenTable[indexProp] instanceof Property)
				{
					id = screenHraci[hracInd].getId();
					((Property)screenTable[indexProp]).setOwnPlayerID(id);
				}
				else if(screenTable[indexProp] instanceof Railroad)
				{
					id = screenHraci[hracInd].getId();
					((Railroad)screenTable[indexProp]).setOwnPlayerID(id);
				}
				else if(GameScreen.screenTable[indexProp] instanceof Utility)
				{
					id = screenHraci[hracInd].getId();
					((Utility)screenTable[indexProp]).setOwnPlayerID(id);
				}
				else
				{
					System.out.println("!b!index!cislo! - Index do pole je spatny!!");
					break;
				}
			    break;
		case 'a':switch(vstup[1].charAt(0))
				{
				case 'k':bids[aukcePozice] = -1;
					     folds++;
					     break;
				case 'f':System.out.println("Nedostatek penez/mensi nez momentalni maximum!!");
					     notClick = false;
					     delaySet();
						 break;
				case 'n':aukcePozice++;
						 if(aukcePozice == screenHraci.length)
					     {
					    	 aukcePozice = 0;
					     }
					     notClick = false;
					     break;
				case 'a':int cis = Integer.parseInt(vstup[2]);
					     aukceMax = cis;
					     bid = aukceMax;
					     bids[aukcePozice] = aukceMax;
					     break;
				case 'e':aukce = false;
					     notClick = false;
					     delaySet();
					     break;
				}
			    break;
		case 'p':int ind = Integer.parseInt(vstup[1]);
				 int money = Integer.parseInt(vstup[2]);
				 //System.out.println("P index: " + ind + " money: " + money);
				 screenHraci[ind].removeMoney(money);
				 moneyDelay[ind] -= money;
				 statDelaySet();
			     break;
		case 'g':ind = Integer.parseInt(vstup[1]);
		         money = Integer.parseInt(vstup[2]);
		         //System.out.println("G index: " + ind + " money: " + money);
		         screenHraci[ind].addMoney(money);
		         moneyDelay[ind] += money;
				 statDelaySet();
			     break;
		case 'h':break;//addHouse
		case 'd':boolean zmenaPozice = false;//chest
				switch(vstup[1].charAt(0))
				{
				case '+':screenHraci[pozice].addMoney(Integer.parseInt(vstup[1].substring(1)));
						 moneyDelay[pozice] += Integer.parseInt(vstup[1].substring(1));
						 statDelaySet();
					     break;
				case '-':screenHraci[pozice].removeMoney(Integer.parseInt(vstup[1].substring(1)));
						 moneyDelay[pozice] -= Integer.parseInt(vstup[1].substring(1));
						 statDelaySet();
			     	     break;
				case 'o':screenHraci[pozice].jailFree = true;
				         break;
				case 'i':chestChancePozice = screenHraci[pozice].getPosition();
						 screenHraci[pozice].setPosition(10);
					     zmenaPozice = true;
					     screenHraci[pozice].jail = true;
						 System.out.println(screenHraci[pozice].getName() + " odchazi do vezeni");
						 break;
				case 'a':String in = vstup[1].substring(1);
				 		 money = Integer.parseInt(in);
						 for(int i = 0; i < screenHraci.length; i++)
						 {
							  if(screenHraci[i].getId() != screenHraci[pozice].getId())
							  {
								  screenHraci[pozice].addMoney(money);
								  screenHraci[i].removeMoney(money);
								  moneyDelay[pozice] += money;
								  moneyDelay[i] -= money;
							  }
						 }
						 statDelaySet();
						 break;
				case 'h':in = vstup[1].substring(1);
						 String [] pom = Assets.separeter(in, ';');
						 int hotel = 0, houses = 0;
						 for (int i = 0; i < screenTable.length; i++)
						 {
							 if(screenTable[i] instanceof Property)
							 {
								 if(((Property)screenTable[i]).getOwnPlayerID() == screenHraci[pozice].getId())
								 {
									 if(((Property)screenTable[i]).isHotel())
									 {
										 hotel++;
									 }
									 else
									 {
										 houses += ((Property)screenTable[i]).getHouseCount();
									 }
								 }						 
							 }
						 }
						 int cost1 = Integer.parseInt(pom[0]);
						 int cost2 = Integer.parseInt(pom[1]);
						 screenHraci[pozice].removeMoney(houses * cost1);
						 screenHraci[pozice].removeMoney(hotel * cost2);
						 moneyDelay[pozice] -= houses * cost1;
						 moneyDelay[pozice] -= hotel * cost2;
						 statDelaySet();
						 break;
				}
				chestChance = true;
				cCTPlayer = pozice;
				cCTtype = 0;
				if(!zmenaPozice)
				{
					chestChancePozice = pozice;					
				}
				drop = vstup[1];
				System.out.println("COMMUNITY CHEST - DONE");
			    break;
		case 'l':ind = Integer.parseInt(vstup[1]);
				Player [] newHraci = new Player[screenHraci.length - 1];
				int [] newSkip = new int[screenHraci.length - 1];
				int [] newMoneyDelay = new int[moneyDelay.length - 1];
				int [][] newThrow = new int[lastThrow.length - 1][2];
				int j = 0;
				for(int i = 0; i < newHraci.length; i++)
				{
					if(i != ind)
					{
						newHraci[i] = screenHraci[j];
						newSkip[i] = skipHraci[j];
						newMoneyDelay[i] = moneyDelay[j];
						newThrow[i][0] = lastThrow[j][0];
						newThrow[i][1] = lastThrow[j][1];
					}
					else
					{
						newHraci[i] = screenHraci[++j];
						newSkip[i] = skipHraci[j];
						newMoneyDelay[i] = moneyDelay[j];
						newThrow[i][0] = lastThrow[j][0];
						newThrow[i][1] = lastThrow[j][1];
					}
					j++;
				}
				screenHraci = newHraci;
				skipHraci = newSkip;
				moneyDelay = newMoneyDelay;
				lastThrow = newThrow;
				
				if(ind == intKlient)//Presmerovani indexu hrace po prohre
				{
					intKlient = -1;
				}
				else
				{
					for(int i = 0; i < screenHraci.length; i++)
					{
						if(screenHraci[i].getName().equals(LoginScreen.login))
						{
							intKlient = i;
						}
					}
				}
				//Nastaveni indexu hrace natahu
				boolean nasel2 = true;
				 while(nasel2)
				 {
					 if(skipHraci[pozice] == 1)
					 {
						 pozice++;
						 if(pozice >= screenHraci.length)
						 {
							 pozice = 0;
						 }
						 continue;
					 }
					 else if(pozice >= screenHraci.length)
					 {
						 pozice = 0;
					 }
					 else
					 {
						 nasel2 = false;
					 }
				 }
			    break;
		case 'c':zmenaPozice = false;
				switch(vstup[1].charAt(0))
				{
				case '+':screenHraci[pozice].addMoney(Integer.parseInt(vstup[1].substring(1)));
						 moneyDelay[pozice] += Integer.parseInt(vstup[1].substring(1));
						 statDelaySet();
					     break;
				case '-':screenHraci[pozice].removeMoney(Integer.parseInt(vstup[1].substring(1)));
						 moneyDelay[pozice] += Integer.parseInt(vstup[1].substring(1));
						 statDelaySet();
					     break;
				case 'c':money = Integer.parseInt(vstup[1].substring(1));
					     for(int i = 0; i < screenHraci.length; i++)
						 {
							  if(screenHraci[i].getId() != screenHraci[pozice].getId())
							  {
								  screenHraci[pozice].removeMoney(money);
								  screenHraci[i].addMoney(money);
								  moneyDelay[pozice] -= money;
								  moneyDelay[i] += money;
							  }
						 }
						 statDelaySet();
						 break;
				case 'l':String in = vstup[1].substring(1);
						 int targetDest = Integer.parseInt(in);
						 if(screenHraci[pozice].getPosition() > targetDest)
						 {
							 screenHraci[pozice].addMoney(200);
						 }
						 chestChancePozice = screenHraci[pozice].getPosition();
						 screenHraci[pozice].setPosition(targetDest);
						 zmenaPozice = true;
						 //screenTable[targetDest].action(screenHraci[pozice], screenHraci);
						 break;
				case 'j':if(vstup[1].charAt(1) == 'i')
						 {
							 screenHraci[pozice].jail = true;
							 chestChancePozice = screenHraci[pozice].getPosition();
							 screenHraci[pozice].setPosition(10);
							 zmenaPozice = true;
						 }
						 else
						 {
							 screenHraci[pozice].jailFree = true;
						 }
						 break;
				case 'k':in = vstup[1].substring(1);
				 		 targetDest = Integer.parseInt(in);
				 		 chestChancePozice = screenHraci[pozice].getPosition();
				 		 screenHraci[pozice].setPosition(screenHraci[pozice].getPosition() - targetDest);
				 		 zmenaPozice = true;
				 		 //screenTable[screenHraci[pozice].getPosition()].action(screenHraci[pozice], screenHraci);
						 break;
				case 'h':in = vstup[1].substring(1);
						 String [] pom = Assets.separeter(in, ';');
						 int hotel = 0, houses = 0;
						 for (int i = 0; i < screenTable.length; i++)
						 {
							 if(screenTable[i] instanceof Property)
							 {
								 if(((Property)screenTable[i]).getOwnPlayerID() == screenHraci[pozice].getId())
								 {
									 if(((Property)screenTable[i]).isHotel())
									 {
										 hotel++;
									 }
									 else
									 {
										 houses += ((Property)screenTable[i]).getHouseCount();
									 }
								 }						 
							 }
						 }
						 int cost1 = Integer.parseInt(pom[0]);
						 int cost2 = Integer.parseInt(pom[1]);
						 screenHraci[pozice].removeMoney(houses * cost1);
						 screenHraci[pozice].removeMoney(hotel * cost2);
						 moneyDelay[pozice] -= houses * cost1;
						 moneyDelay[pozice] -= hotel * cost2;
						 statDelaySet();
						 break;
				case 't':in = vstup[1].substring(1);
				 	     money = Integer.parseInt(in);
				 	     screenHraci[pozice].removeMoney(money);
				 	     moneyDelay[pozice] -= money;
						 statDelaySet();
				 	     break;
				case 'u':ind = screenHraci[pozice].getPosition();
						 for (int i = ind + 1; i < screenTable.length; i++)
						 {
							if(i + 1 == screenTable.length)
							{
								i = 0;
							}
							else if(screenTable[i] instanceof Utility)
							{
								ind = i;
								break;
							}
						 }
						 if(screenHraci[pozice].getPosition() > ind)
						 {
							 screenHraci[pozice].addMoney(200);
						 }
						 chestChancePozice = screenHraci[pozice].getPosition();
						 screenHraci[pozice].setPosition(ind);
						 zmenaPozice = true;
					     //Table.gameTable[ind].action(screenHraci[pozice], screenHraci);
					     break;
				case 'r':ind = screenHraci[pozice].getPosition();
						 for (int i = ind + 1; i < screenTable.length; i++)
						 {
							if(i + 1 == screenTable.length)
							{
								i = 0;
							}
							else if(screenTable[i] instanceof Railroad)
							{
								ind = i;
								break;
							}
						 }
						 if(screenHraci[pozice].getPosition() > ind)
						 {
							 screenHraci[pozice].addMoney(200);
						 }
						 chestChancePozice = screenHraci[pozice].getPosition();
						 screenHraci[pozice].setPosition(ind);
						 zmenaPozice = true;
					     //screenTable[ind].action(screenHraci[pozice], screenHraci);
					     break;
				}
				chestChance = true;
				if(!zmenaPozice)
				{
					chestChancePozice = pozice;					
				}
				cCTtype = 1;
				cCTPlayer = pozice;
				drop = vstup[1];
				System.out.println("CHEST - DONE");
				break;
		default:System.out.println("Zprava ma spatny vstupni tvar!!");
			    break;
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

	public static void identifyPlayer()
	{
		int ind = LobbyScreen.selectedLobby;
		String [] hr = LobbyScreen.lobbies[ind].getHraci();
		System.out.println(Arrays.toString(hr));
		for(int i = 0; i < hr.length; i++)
		{
			System.out.println(hr[i]);
			if(hr[i].equals(LoginScreen.login))
			{
				intKlient = i;
				System.out.println(hr[i] + " ma index " + i);
				return;
			}
		}
		intKlient = -1;
		System.out.println("Error, nenasel jsem stejny nazev pro toto jmeno " + LoginScreen.login);
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
				Monopoly.LoginScreen.rc.fell = false;
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
			delaySet();		
		}
	}

	@Override
	public void dispose()
	{
		batch.dispose();
		font.dispose();
		sr.dispose();
	}
	
	public static void delaySet()
	{
		delay = 75;
	}
	
	public static void statDelaySet()
	{
		statsDelay = 200;
	}
	
	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
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
	public boolean scrolled(int amount)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
