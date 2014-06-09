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

    public static int BUCKET_SPEED = 500;// Completely useless
    // public static int BUCKET_SPEED2 = 500;
    Texture player1;
    Texture player2;
    Texture dropImage;
    Texture bucketImage;
    Texture backgroundTexture;
    Sound dropSound;
    Music rainMusic;
    OrthographicCamera camera;
    SpriteBatch batch;
    Rectangle player1Sprite;
    Rectangle player2Sprite;
    Array<Rectangle> Bubbles;
    long lastDropTime;
    private int scorePlayer1;
    private int scorePlayer2;
    private String ScorePlayer1 = "Score: ";
    private String ScorePlayer2 = "Score: ";
    BitmapFont yourBitmapFontName;

    @Override
    public void create() {

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        //player1 = new Texture("bucket.png");
        //player2 = new Texture("bucket.png");
        dropImage = new Texture("droplet.png");

        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        rainMusic.setLooping(true);
        rainMusic.play();

        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));

        batch = new SpriteBatch();
        player1Sprite = new Rectangle();
        player1Sprite.x = 800 / 4 - 64 / 2;
        player1Sprite.y = 20;
        player1Sprite.width = 1;
        player1Sprite.height = 1;

        backgroundTexture = new Texture("background.jpg");

        /*
         * player2Sprite = new Rectangle(); player2Sprite.x = 800 / 2 - 64 / 2;
         * player2Sprite.y = 20; player2Sprite.width = 60; player2Sprite.height
         * = 60;
         */
        scorePlayer1 = 0;
        scorePlayer2 = 0;

        ScorePlayer1 = "Score: 0";
        ScorePlayer2 = "Score: 0";
        yourBitmapFontName = new BitmapFont();

        Bubbles = new Array<Rectangle>();
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
        yourBitmapFontName.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        yourBitmapFontName.draw(batch, ScorePlayer1, 700, 100); // generates
        // score for
        // player1
        // yourBitmapFontName.draw(batch, ScorePlayer2, 25, 100); //generates
        // score for player 2
        // batch.draw(player1, player1Sprite.x, player1Sprite.y);//draws player1
        // batch.draw(player2, player2Sprite.x, player2Sprite.y);// draws
        // player2
        for (Rectangle Bubble : Bubbles) {
            batch.draw(dropImage, Bubble.x, Bubble.y);// draws bubbles
        }
        batch.end();
        /*
         * //player 1 controls if(Gdx.input.isKeyPressed(Keys.LEFT))
         * player1Sprite.x -=BUCKET_SPEED*Gdx.graphics.getDeltaTime();
         * if(Gdx.input.isKeyPressed(Keys.RIGHT)) player1Sprite.x
         * +=BUCKET_SPEED*Gdx.graphics.getDeltaTime();
         * if(Gdx.input.isKeyPressed(Keys.UP)) player1Sprite.y
         * +=BUCKET_SPEED*Gdx.graphics.getDeltaTime();
         * if(Gdx.input.isKeyPressed(Keys.DOWN)) player1Sprite.y
         * -=BUCKET_SPEED*Gdx.graphics.getDeltaTime();
         *
         * //player 2 controls if(Gdx.input.isKeyPressed(Keys.A))
         * player2Sprite.x -=BUCKET_SPEED2*Gdx.graphics.getDeltaTime();
         * if(Gdx.input.isKeyPressed(Keys.D)) player2Sprite.x
         * +=BUCKET_SPEED2*Gdx.graphics.getDeltaTime();
         * if(Gdx.input.isKeyPressed(Keys.W)) player2Sprite.y
         * +=BUCKET_SPEED2*Gdx.graphics.getDeltaTime();
         * if(Gdx.input.isKeyPressed(Keys.S)) player2Sprite.y
         * -=BUCKET_SPEED2*Gdx.graphics.getDeltaTime();
         */
        /*
         * //user input by clicking and using mousepad for player 1
         * if(Gdx.input.isButtonPressed(Buttons.LEFT)) { Vector3 touchPos = new
         * Vector3(); touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
         * camera.unproject(touchPos); player1Sprite.x = touchPos.x - 64 / 2;
         * player1Sprite.y = touchPos.y - 64 / 2; }
         */

        // player1boundary
        // bubble rate droper
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000) {
            spawnBubble();
        }

        // bubble spawner and scorer for players
        // Iterator<Rectangle> iter = Bubbles.iterator();
        for (Rectangle Bubble : Bubbles) {// iter.hasNext()
            // Rectangle Bubble = iter.next();
            Bubble.y -= 200 * Gdx.graphics.getDeltaTime();
            if (Bubble.y + 64 < 0) {
                Bubbles.removeValue(Bubble, true);
            }

            if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
                Vector3 touchPos = new Vector3();
                touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(touchPos);
                player1Sprite.x = touchPos.x - 64 / 2;
                player1Sprite.y = touchPos.y - 64 / 2;
                if (Bubble.overlaps(player1Sprite) &&
                        player1Sprite.y >= 450 - 64)// checks if player is
                // touching top border,
                // and has a bubble with
                // bucket
                {
                    dropSound.play();
                    Bubbles.removeValue(Bubble, true);
                    scorePlayer1--;
                    ScorePlayer1 = "score: " + scorePlayer1;
                } else if (Bubble.overlaps(player1Sprite)) {
                    dropSound.play();
                    Bubbles.removeValue(Bubble, true);
                    BUCKET_SPEED--;
                    scorePlayer1++;
                    //lastDropTime = lastDropTime * 1000000;
                    ScorePlayer1 = "score: " + scorePlayer1;
                }
            }

            /*
             * if(Bubble.overlaps(player2Sprite) && player2Sprite.y>=450-64 ) {
             * dropSound.play(); Bubbles.removeValue(Bubble, true); scorePlayer2--; ScorePlayer2 =
             * "score: " + scorePlayer2; }else
             * if(Bubble.overlaps(player2Sprite)){ dropSound.play(); Bubble =
             * null; BUCKET_SPEED2--; scorePlayer2++; ScorePlayer2 = "score: " +
             * scorePlayer2; }
             */
            if (Bubble.y - 64 < 0) {
                scorePlayer1--;
                ScorePlayer1 = "score: " + scorePlayer1;
                // Bubbles.removeValue(Bubble, true);
                Bubbles.removeValue(Bubble, true);
            }

        }
        if (player1Sprite.x < 0) {
            player1Sprite.x = 0;

        }
        if (player1Sprite.x > 730 - 64) {
            player1Sprite.x = 730 - 64;

        }

        if (player1Sprite.y < 0) {
            player1Sprite.y = 0;

        }
        if (player1Sprite.y >= 450 - 64) {
            player1Sprite.y = 450 - 64;

        }

        /*
         * //player2 boundary if (player2Sprite.x < 0){ player2Sprite.x = 0;
         * //scorePlayer2 = 0; //ScorePlayer2 = "score: " + scorePlayer2; } if
         * (player2Sprite.x > 730 - 64){ player2Sprite.x = 730 - 64;
         * //scorePlayer2 = 0; //ScorePlayer2 = "score: " + scorePlayer2; } if
         * (player2Sprite.y<0){ player2Sprite.y = 0; //scorePlayer2 = 0;
         * //ScorePlayer2 = "score: " + scorePlayer2; }
         * if(player2Sprite.y>=450-64){ player2Sprite.y = 450-64;
         *
         * }
         */
    }

    // creates bubble
    private void spawnBubble() {
        Rectangle Bubble = new Rectangle();
        Bubble.x = MathUtils.random(0, 800 - 64);
        Bubble.y = 480;
        Bubble.width = 64;
        Bubble.height = 64;
        Bubbles.add(Bubble);
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
        //bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();

    }

}
