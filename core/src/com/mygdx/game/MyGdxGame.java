package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
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

public class MyGdxGame implements ApplicationListener {

    //Constants
    public static final String TITLE = "Bubble Game";
    public static final int WIDTH = 800;
    public static final int HEIGHT = 480;
    public static final int BUBBLE_SPEED = 200;

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;

    private Sprite mainBubbleSprite;
    private Texture backgroundTexture;
    private Sound dropSound;
    private Music rainMusic;

    //Physics
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Array<Body> bubbles;

    //Game elements
    private double bubbleTime;
    private double bubbleTimeStep;
    private int score;

    @Override
    public void create() {
        world = new World(new Vector2(0, 0), true);

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
        bubbleTime = 0;

        bubbles = new Array<Body>();
        spawnBubble();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, 800, 480);//Draw this before bubbles

        //Draw the bubbles
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
                continue;
            }

            body.setLinearVelocity(body.getLinearVelocity().x * 4f, body.getLinearVelocity().y * 4f);
        }

        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        font.draw(batch, "Score: " + score, 700, 100);//Player score

        batch.end();

        //Debug bubble spawning
        if (Gdx.input.isKeyPressed(Keys.SPACE)) {
            spawnBubble();
        }
        //Debug score adjust
        if (Gdx.input.isKeyPressed(Keys.UP)) {
            score++;
        }
        if (Gdx.input.isKeyPressed(Keys.DOWN)) {
            score--;
        }

        //Click handling
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

        //Timed bubble spawning
        if (bubbleTime > bubbles.size) {
            spawnBubble();
        }

        debugRenderer = new Box2DDebugRenderer();
        debugRenderer.render(world, camera.combined);

        /*
         LOGISTIC BUBBLE SPAWNING
         https://www.desmos.com/calculator/xcrski6uif

         When score is low, slow spawning. When score is high, fast spawning.
         Also is kinda nice because a bubble will always spawn if there are no bubbles on board
         */
        double x = score;
        double var1 = 100;//Carrying capacity. Max number of bubbles
        double var2 = -5;//kinda controls when the graph starts getting steep
        double var3 = .015;//Controls steepness of graph, max rate of spawn

        bubbleTimeStep = var1 / (1 + Math.pow(Math.E, -(var2 + (var3 * x))));

        bubbleTime += bubbleTimeStep * (1 / 60f);

        batch.begin();
        font.draw(batch, "bubbleTimeStep: " + bubbleTimeStep, 0, 45);
        font.draw(batch, "bubbleTime: " + bubbleTime, 0, 30);
        font.draw(batch, "NumBubbles: " + bubbles.size, 0, 15);
        batch.end();

        world.step(1 / 60f, 6, 2);
    }

    private void deleteBubble(Body b) {
        bubbles.removeValue(b, true);
        world.destroyBody(b);
    }

    private boolean inBounds(float x, float y) {
        return x >= 0 &&
                y >= 0 &&
                x <= WIDTH &&
                y <= HEIGHT;
    }

    private void spawnBubble() {

        // First we create a body definition
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;

        // Create our body in the world using our body definition
        Body body = world.createBody(bodyDef);

        //Angle and position
        float angle = MathUtils.random(0, 2 * 3.1415926535f);
        /*
         System.out.println("Angle: " + angle);
         System.out.println("cos: " + MathUtils.cos(angle));
         System.out.println("sin: " + MathUtils.sin(angle));
         System.out.println("cos * .5 width: " + MathUtils.cos(angle) * .5f * WIDTH);
         System.out.println("sin * .5 height: " + MathUtils.sin(angle) * .5f * HEIGHT);
         System.out.println("cos thing + .5width: " + ((MathUtils.cos(angle) * .5f * WIDTH) + (.5f * WIDTH)));
         System.out.println("sin thing + .5height: " + ((MathUtils.sin(angle) * .5f * HEIGHT) + (.5f * HEIGHT)));
         */

        body.setTransform(WIDTH - ((MathUtils.cos(angle) * .5f * WIDTH) + (.5f * WIDTH)), HEIGHT - ((MathUtils.sin(angle) * .5f * HEIGHT) + (.5f * HEIGHT)), angle);
        //body.setAngularVelocity(MathUtils.random(.1f, 4f));

        // Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
        Float random = MathUtils.random(10f, 50f);
        circle.setRadius(random);

        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.1f;
        fixtureDef.friction = 0.1f;
        fixtureDef.restitution = 0.6f;

        // Create our fixture and attach it to the body
        Fixture fixture = body.createFixture(fixtureDef);

        //Sprite setup
        Sprite bubbleSprite = new Sprite(mainBubbleSprite);
        bubbleSprite.setSize(circle.getRadius() * 2, circle.getRadius() * 2);//*2 for radius->diameter
        bubbleSprite.setOrigin(bubbleSprite.getWidth() / 2, bubbleSprite.getHeight() / 2);
        body.setUserData(bubbleSprite);

        float cosine = MathUtils.cos(body.getAngle());
        float sine = MathUtils.sin(body.getAngle());
        body.setLinearVelocity(random * random * cosine * cosine * cosine, random * random * sine * sine * sine);//size ^2 * angle ^3

        circle.dispose();

        bubbles.add(body);

        bubbleTime = 0;
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
