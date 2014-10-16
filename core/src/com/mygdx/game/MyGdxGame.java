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

    private static final String[] MUSICS = {//Todo: generante from /assets/sound/music folder instead of listing
        "Run Amok.mp3",
        "The Builder.mp3",
        "Monkeys Spinning Monkeys.mp3",
        "Fun in a Bottle.mp3",
        "Pamgaea.mp3",
        "Jaunty Gumption.mp3"
    };

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;

    private Sprite mainBubbleSprite;
    private Texture backgroundTexture;
    private Sound dropSound;
    private Music backgroundMusic;

    //Physics
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Array<Body> bubbles;

    private BodyDef bodyDef;
    private CircleShape circle;
    private FixtureDef fixtureDef;

    //Game elements
    private double bubbleTime;
    private double bubbleTimeStep;
    private int score;

    @Override
    public void create() {
        world = new World(new Vector2(0, 0), true);

        //Bubble physics setup
        bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;

        fixtureDef = new FixtureDef();
        fixtureDef.density = 0.1f;
        fixtureDef.friction = 0.1f;
        fixtureDef.restitution = 0.6f;
        //End bubble physics setup

        //This shows the wireframes of physics objects
        debugRenderer = new Box2DDebugRenderer();

        //Setting up things
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        backgroundTexture = new Texture("background.jpg");
        mainBubbleSprite = new Sprite(new Texture("bubble.png"));

        dropSound = Gdx.audio.newSound(Gdx.files.internal("Sound/drop.wav"));

        String musicString = "Sound/Music/" + MUSICS[MathUtils.random(0, MUSICS.length - 1)];//Get a random music
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(musicString));//Set the random music
        backgroundMusic.setLooping(true);//Loop the random music
        backgroundMusic.play();//Play the random music

        //More setup
        batch = new SpriteBatch();
        font = new BitmapFont();

        score = 0;
        bubbleTime = 0;

        bubbles = new Array<Body>();

        //And make the first bubbles to start the game
        spawnBubbles(3);
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
                //Update the bubble's sprite (image) to match the bubble pysics body
                Sprite sprite = (Sprite) body.getUserData();
                sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);
                sprite.setRotation(body.getAngle() * MathUtils.radiansToDegrees);
                sprite.draw(batch);
            }
            if (!inBounds(body.getPosition().x, body.getPosition().y)) {
                //Remove bubbles outside the screen and decrease score
                deleteBubble(body);
                score--;
                continue;
            }
            //I think this is supposed to be acceleration. Don't think it works though
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
            spawnBubbles(3);//Spawn 3 bubbles at a time
        }

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

        bubbleTimeStep = var1 / (1 + Math.pow(Math.E, -(var2 + (var3 * x))));//Pretty much the equation from the link above

        bubbleTime += bubbleTimeStep * Gdx.graphics.getDeltaTime();

        //Debug output
        batch.begin();
        font.draw(batch, "bubbleTimeStep: " + bubbleTimeStep, 0, 45);
        font.draw(batch, "bubbleTime: " + bubbleTime, 0, 30);
        font.draw(batch, "NumBubbles: " + bubbles.size, 0, 15);
        batch.end();

        world.step(1 / 60f, 6, 2);
    }

    /*
     Remove a bubble (physics body)
     @args The bubble body to remove.
     */
    private void deleteBubble(Body b) {
        bubbles.removeValue(b, true);
        world.destroyBody(b);
        b.setUserData(null);
        b = null;
    }
    /*
     Check if a point is within the visible screen area
     */

    private boolean inBounds(float x, float y) {
        return x >= 0 &&
                y >= 0 &&
                x <= WIDTH &&
                y <= HEIGHT;
    }

    private void spawnBubbles(int n) {
        for (int i = 1; i <= n; i++) {
            spawnBubble();
        }
    }

    private void spawnBubble() {

        circle = new CircleShape();

        Body body = world.createBody(bodyDef);

        //Angle and position
        float angle = MathUtils.random(3.926990817f, 5.497787144f);//Random angle from 225 deg to 315 deg

        //From the angle the bubble is facing, the position is set so it will travel to the center
        body.setTransform(WIDTH - ((MathUtils.cos(angle) * .5f * WIDTH) + (.5f * WIDTH)), HEIGHT - ((MathUtils.sin(angle) * .5f * HEIGHT) + (.5f * HEIGHT)), angle);

        Float random = MathUtils.random(10f, 50f);//The numbers are arbitrary
        circle.setRadius(random);

        //for the physics
        fixtureDef.shape = circle;
        body.createFixture(fixtureDef);

        //Sprite setup
        Sprite bubbleSprite = new Sprite(mainBubbleSprite);
        bubbleSprite.setSize(circle.getRadius() * 2, circle.getRadius() * 2);//Set sprite size to match body. *2 for radius->diameter
        bubbleSprite.setOrigin(bubbleSprite.getWidth() / 2, bubbleSprite.getHeight() / 2);//Set sprite on top of body
        body.setUserData(bubbleSprite);

        float cosine = MathUtils.cos(body.getAngle());
        float sine = MathUtils.sin(body.getAngle());

        //Soo the speed x, y is set fom cosine and sine of the angle from earlier.
        //Then multiplied by random (Which is also the size of the bubble)
        //Instead of math.pow wich I think is clunky, I opted to just multoply everything out.
        //I think the powers are arbitrary, from trial and error to get a speed that was about right
        //So the big bubbles are faster
        //(They're easier to click because they'e big, so the higher speed cancels it out)
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

        world.dispose();
        mainBubbleSprite.getTexture().dispose();
        backgroundMusic.dispose();

    }
}
