package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
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

    private Sprite mainBubbleSprite;
    private Texture backgroundTexture;
    private Sound dropSound;
    private Music rainMusic;

    private World world;
    private Box2DDebugRenderer debugRenderer;

    private Array<Body> bubbles;
    private long lastDropTime;
    private int score;

    @Override
    public void create() {
        world = new World(new Vector2(0, -20), true);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        backgroundTexture = new Texture("background.jpg");
        mainBubbleSprite = new Sprite(new Texture("bubble.png"));

        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        rainMusic.setLooping(true);
        //rainMusic.play();

        batch = new SpriteBatch();
        font = new BitmapFont();

        score = 0;

        bubbles = new Array<Body>();
        spawnBubble();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

//        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, 800, 480);

        Array<Body> tmpBodys = new Array<Body>();
        //world.getBodies(bubbles);
        for (Body body : bubbles) {
            if (body.getUserData() != null && body.getUserData() instanceof Sprite) {
                Sprite sprite = (Sprite) body.getUserData();
                sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);
                sprite.setRotation(body.getAngle() * MathUtils.radiansToDegrees);
                sprite.draw(batch);
            }
            if (!inBounds(body.getPosition().x, body.getPosition().y)) {
                deleteBubble(body);
                score--;
            }
        }

        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        font.draw(batch, "Score: " + score, 700, 100); // player score

        /*for (Bubble bubble : bubbles) {
         batch.draw(bubbleImage, bubble.x, bubble.y, bubble.size, bubble.size);// draws bubbles
         }*/
        batch.end();

        // bubble rate droper
        if (TimeUtils.nanoTime() - lastDropTime > 10000000) {
            spawnBubble();
        }
        /*
         //Bubble Movement
         for (Bubble bubble : bubbles) {
         bubble.move();
         if (!inBounds(bubble)) {
         score--;
         bubbles.removeValue(bubble, true);
         }
         }*/

        //click handling
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            for (Body bubble : bubbles) {
                for (Fixture fixture : bubble.getFixtureList()) {
                    if (fixture.testPoint(touchPos.x, touchPos.y)) {
                        score++;
                        deleteBubble(bubble);
                    }
                }
            }
        }

        debugRenderer = new Box2DDebugRenderer();
        debugRenderer.render(world, camera.combined);

        world.step(1 / 60f, 6, 2);
    }

    private void deleteBubble(Body b) {
        bubbles.removeValue(b, true);
//        for (Fixture f : b.getFixtureList()) {
//            b.destroyFixture(f);
//        }
        world.destroyBody(b);
    }

    private boolean inBounds(float x, float y) {
        return x >= 0 &&
                y >= 0 &&
                x <= WIDTH &&
                y <= HEIGHT;
    }

    private void spawnBubble() {

        //Bubble bubble = new Bubble();
        //Bubble bubble = new Bubble(Direction.DOWN);
        // First we create a body definition
        BodyDef bodyDef = new BodyDef();
        // We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = BodyType.DynamicBody;
        // Set our body's starting position in the world
        bodyDef.position.set(400, 400);

        // Create our body in the world using our body definition
        Body body = world.createBody(bodyDef);

        // Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
        circle.setRadius(24f);

        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f; // Make it bounce a little bit

        // Create our fixture and attach it to the body
        Fixture fixture = body.createFixture(fixtureDef);

        Sprite bubbleSprite = new Sprite(mainBubbleSprite);
        bubbleSprite.setSize(circle.getRadius() * 2, circle.getRadius() * 2);//I have no idea why *2, but it makes the size better
        bubbleSprite.setOrigin(bubbleSprite.getWidth() / 2, bubbleSprite.getHeight() / 2);
        body.setUserData(bubbleSprite);

        body.setLinearVelocity(MathUtils.random(0.2f, 2f), MathUtils.random(0.2f, 2f));

        // Remember to dispose of any shapes after you're done with them!
        // BodyDef and FixtureDef don't need disposing, but shapes do.
        circle.dispose();

//        float mod = MathUtils.random(0.2f, 2f);
//        bubble.speedModifier = mod;
//        bubble.setSizeByModifier(mod);
        bubbles.add(body);
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
        dropSound.dispose();
        rainMusic.dispose();
        world.dispose();
        mainBubbleSprite.getTexture().dispose();
    }
}
