package com.mygdx.seme;

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
import com.badlogic.gdx.math.Vector3;

public class LobbyScreen implements Screen, InputProcessor
{
	Game game;
	BitmapFont font;
	OrthographicCamera camera;
	SpriteBatch batch;
	ShapeRenderer sr;
	Vector3 touch;
	
	static int width = Gdx.graphics.getWidth();
	static int height = Gdx.graphics.getHeight();
	
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
        //Gdx.input.setInputProcessor(this);
	}
	
	@Override
	public void show()
	{
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render(float delta)
	{
		//reset canvas
		Gdx.gl.glClearColor(1F, 1F, 1F, 1F);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
		camera.update();
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
