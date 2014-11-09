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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class MyGdxGame implements ApplicationListener {

    //Constants
    public static final String TITLE = "Bubble Game";
    public static final int WIDTH = 900;
    public static final int HEIGHT = 480;
    private static final float BUBBLE_SPEED = 200f;
    private static final float CIRCLE_RADIUS = 35f;

    private static final String[] MUSICS = {//Todo: generante from /assets/sound/music folder instead of listing
        "Run Amok.mp3",
        "The Builder.mp3",
        "Monkeys Spinning Monkeys.mp3",
        "Fun in a Bottle.mp3",
        "Pamgaea.mp3",
        "Jaunty Gumption.mp3"
    };

    //Creates collision parts variables
    private static final short BIT_ASTEROID = 2;

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
    private BodyDef bdefPlatform;
    private BodyDef bdefcharacter;
    private CircleShape circle;
    private FixtureDef fixtureDef;
    private Rectangle character;

    //Game elements
    private double bubbleTime;
    private double bubbleTimeStep;
    private int score;

    @Override
    public void create() {
        world = new World(new Vector2(0, 0), true);

        //creates character
        character = new Rectangle();
        character.x = 800 / 2 - 64 / 2;
        character.y = 20;
        character.width = 10;
        character.height = 10;

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
        camera.setToOrtho(false, WIDTH, HEIGHT);

        backgroundTexture = new Texture("background.jpg");
        mainBubbleSprite = new Sprite(new Texture("fireball.jpg"));

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
        spawnBubbles();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        //this comment below draws background
        //batch.draw(backgroundTexture, 0, 0, WIDTH, HEIGHT);//Draw this before bubbles

        batch.draw(backgroundTexture, character.x, character.y);

        //creates left and right bounds
        boxForbounds();

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
                continue;
            }
            //I think this is supposed to be acceleration. Don't think it works though
            body.setLinearVelocity(body.getLinearVelocity().x * 4f, body.getLinearVelocity().y * 4f);
        }

        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        font.draw(batch, "Score: " + score, 500, 100);//Player score

        batch.end();

        //Debug bubble spawning
        if (Gdx.input.isKeyPressed(Keys.SPACE)) {
            spawnBubbles();
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
        if (bubbleTime > bubbles.size + 2) {
            spawnBubbles();//Spawn 3 bubbles at a time
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
        /*
         batch.begin();
         font.draw(batch, "bubbleTimeStep: " + bubbleTimeStep, 0, 45);
         font.draw(batch, "bubbleTime: " + bubbleTime, 0, 30);
         font.draw(batch, "NumBubbles: " + bubbles.size, 0, 15);
         batch.end();
         */
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            character.x -= 300 * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            character.x += 300 * Gdx.graphics.getDeltaTime();
        }

        if (character.x < 25) {
            character.x = 25;
        }
        if (character.x > WIDTH - 317) {
            character.x = WIDTH - 317;
        }

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

    private void spawnBubbles() {
        //Sevenths to alternate gaps between other bubbles and edges
        //[ ][x][ ][x][ ][x][ ]
        spawnBubble((1f / 7f) * WIDTH, (2f / 7f) * WIDTH);
        spawnBubble((3f / 7f) * WIDTH, (4f / 7f) * WIDTH);
        spawnBubble((5f / 7f) * WIDTH, (6f / 7f) * WIDTH);
    }

    private void spawnBubble(float minX, float maxX) {

        circle = new CircleShape();

        Body body = world.createBody(bodyDef);

        //Angle and position
        //Angle from 250ish to 300ish to give variance to travel path
        float minAngle = 4.25f;
        float maxAngle = 5.15f;
        float angle = MathUtils.random(minAngle, maxAngle);

        float xOrigin = MathUtils.random(minX, maxX);
        body.setTransform(xOrigin, 480, angle);

        circle.setRadius(CIRCLE_RADIUS);

        //for the physics
        fixtureDef.shape = circle;
        fixtureDef.filter.maskBits = BIT_ASTEROID;//makes it so asteroids(bubbles) don't collide into each other
        body.createFixture(fixtureDef);

        //Sprite setup
        Sprite bubbleSprite = new Sprite(mainBubbleSprite);
        bubbleSprite.setSize(circle.getRadius() * 2, circle.getRadius() * 2);//Set sprite size to match body. *2 for radius->diameter
        bubbleSprite.setOrigin(bubbleSprite.getWidth() / 2, bubbleSprite.getHeight() / 2);//Set sprite on top of body
        //body.setUserData(bubbleSprite);

        float cosine = MathUtils.cos(body.getAngle());
        float sine = MathUtils.sin(body.getAngle());

        //Soo the speed x, y is set fom cosine and sine of the angle from earlier.
        //Then multiplied by random (Which is also the size of the bubble)
        //Instead of math.pow wich I think is clunky, I opted to just multoply everything out.
        //I think the powers are arbitrary, from trial and error to get a speed that was about right
        //So the big bubbles are faster
        //(They're easier to click because they'e big, so the higher speed cancels it out)
        body.setLinearVelocity(CIRCLE_RADIUS * CIRCLE_RADIUS * cosine * cosine * cosine, CIRCLE_RADIUS * CIRCLE_RADIUS * sine * sine * sine);//size ^2 * angle ^3
        // body.setLinearVelocity(BUBBLE_SPEED,BUBBLE_SPEED);
        circle.dispose();

        bubbles.add(body);

        bubbleTime = 0;
    }

    private void boxForbounds() {
        //create right bound
        bdefPlatform = new BodyDef();
        bdefPlatform.position.set(0, 0);
        bdefPlatform.type = BodyType.StaticBody;
        Body body = world.createBody(bdefPlatform);

        PolygonShape pshape = new PolygonShape();
        pshape.setAsBox(25, 480);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = pshape;
        body.createFixture(fdef);

        //create left bound
        bdefPlatform.position.set(WIDTH, 0);
        bdefPlatform.type = BodyType.StaticBody;
        body = world.createBody(bdefPlatform);

        pshape.setAsBox(25, 480);
        fdef.shape = pshape;
        body.createFixture(fdef);
    }

    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub

    }

    //the character and his movements
    public void characterAndMovement() {

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
