package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class MyGdxGame implements ApplicationListener {

    public static final String TITLE = "Bubble Game";
    public static final int WIDTH = 800;
    public static final int HEIGHT = 480;
    private final int BUBBLE_SPEED = 200;
    private static final int MOUSE_SIZE = 3;

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;

    private Texture dropImage;
    private Texture backgroundTexture;
    private Sound dropSound;
    private Music rainMusic;

    private Array<Rectangle> bubbles;
    private long lastDropTime;
    private int score;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        backgroundTexture = new Texture("background.jpg");
        dropImage = new Texture("droplet.png");

        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        rainMusic.setLooping(true);
        rainMusic.play();

        batch = new SpriteBatch();
        font = new BitmapFont();

        score = 0;

        bubbles = new Array<Rectangle>();
        spawnBubble();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, 800, 480);

        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        font.draw(batch, "Score: " + score, 700, 100); // player score

        for (Rectangle Bubble : bubbles) {
            batch.draw(dropImage, Bubble.x, Bubble.y);// draws bubbles
        }
        batch.end();

        // bubble rate droper
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000) {
            spawnBubble();
        }

        // bubble spawner and scorer for players
        for (Rectangle bubble : bubbles) {// iter.hasNext()
            bubble.y -= 200 * Gdx.graphics.getDeltaTime();
            if (bubble.y + 64 < 0) {
                bubbles.removeValue(bubble, true);
            }

            if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
                Vector3 touchPos = new Vector3();
                touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(touchPos);

                if (bubble.overlaps(new Rectangle(touchPos.x, touchPos.y, MOUSE_SIZE, MOUSE_SIZE))) {
                    dropSound.play();
                    bubbles.removeValue(bubble, true);
                    score--;
                }

                if (bubble.y - 64 < 0) {
                    score--;
                    bubbles.removeValue(bubble, true);
                }
            }
        }
    }

    // creates bubble
    private void spawnBubble() {
        Rectangle Bubble = new Rectangle();
        Bubble.x = MathUtils.random(0, 800 - 64);
        Bubble.y = 480;
        Bubble.width = 64;
        Bubble.height = 64;
        bubbles.add(Bubble);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        dropImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
    }
}
