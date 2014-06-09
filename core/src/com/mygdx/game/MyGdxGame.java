package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class MyGdxGame implements ApplicationListener {

    public static final String TITLE = "Bubble Game";
    public static final int WIDTH = 800;
    public static final int HEIGHT = 480;
    public static final int BUBBLE_SPEED = 200;
    private static final int MOUSE_SIZE = 3;

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;

    private Texture bubbleImage;
    private Texture backgroundTexture;
    private Sound dropSound;
    private Music rainMusic;

    private BodyDef newBubble;
    private World world;

    private Array<Bubble> bubbles;
    private long lastDropTime;
    private int score;

    @Override
    public void create() {
        World world = new World(new Vector2(0, -10), true);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        backgroundTexture = new Texture("background.jpg");
        bubbleImage = new Texture("bubble.png");

        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        rainMusic.setLooping(true);
        rainMusic.play();

        batch = new SpriteBatch();
        font = new BitmapFont();

        score = 0;

        bubbles = new Array<Bubble>();
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

        for (Bubble bubble : bubbles) {
            batch.draw(bubbleImage, bubble.x, bubble.y, bubble.size, bubble.size);// draws bubbles
        }
        batch.end();

        // bubble rate droper
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000) {
            spawnBubble();
        }

        //Bubble Movement
        for (Bubble bubble : bubbles) {
            bubble.move();
            if (!inBounds(bubble)) {
                score--;
                bubbles.removeValue(bubble, true);
            }
        }

        //click handling
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            for (Bubble bubble : bubbles) {
                if (bubble.overlaps(new Rectangle(touchPos.x, touchPos.y, MOUSE_SIZE, MOUSE_SIZE))) {
                    score++;
                    bubbles.removeValue(bubble, true);
                }
            }
        }
    }

    private boolean inBounds(Rectangle r) {
        return r.x >= 0
                && r.y >= 0
                && r.x + (.5 * r.width) <= WIDTH
                && r.y + (.5 * r.height) <= HEIGHT;
    }

    private void spawnBubble() {
        //Bubble bubble = new Bubble();
        //Bubble bubble = new Bubble(Direction.DOWN);

        BodyDef bubble = new BodyDef();
        bubble.type = BodyDef.BodyType.DynamicBody;
        Body bubbleBody = world.createBody(bubble);

        bubble.position.set(200, 200);

//        float mod = MathUtils.random(0.2f, 2f);
//        bubble.speedModifier = mod;
//        bubble.setSizeByModifier(mod);
        //bubbles.add(bubble);
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
        bubbleImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
    }
}
