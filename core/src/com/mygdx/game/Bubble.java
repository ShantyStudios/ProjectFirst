package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;

/**
 *
 * @author Deliquescence <Deliquescence1@gmail.com>
 */
public class Bubble extends Rectangle {

    public Direction movementDirection;
    public float speedModifier;
    public float size;

    public Bubble(Direction d) {
        movementDirection = d;
    }

    private final int BASE_SIZE = 64;

    public void setSizeByModifier(float mod) {
        this.size = BASE_SIZE * mod;
        this.width = size;
        this.height = size;
    }

    public void move() {
        switch (movementDirection) {
            case UP:
                moveUp();
                break;
            case RIGHT:
                moveRight();
                break;
            case LEFT:
                moveLeft();
                break;
            case DOWN:
                moveDown();
                break;
            case UP_RIGHT:
                moveUp();
                moveRight();
                break;
            case UP_LEFT:
                moveUp();
                moveLeft();
                break;
            case DOWN_LEFT:
                moveDown();
                moveLeft();
                break;
            case DOWN_RIGHT:
                moveDown();
                moveRight();
                break;

        }
    }

    private void moveRight() {
        x += MyGdxGame.BUBBLE_SPEED * speedModifier * Gdx.graphics.getDeltaTime();
    }

    private void moveUp() {
        y += MyGdxGame.BUBBLE_SPEED * speedModifier * Gdx.graphics.getDeltaTime();
    }

    private void moveDown() {
        y -= MyGdxGame.BUBBLE_SPEED * speedModifier * Gdx.graphics.getDeltaTime();
    }

    private void moveLeft() {
        x -= MyGdxGame.BUBBLE_SPEED * speedModifier * Gdx.graphics.getDeltaTime();
    }
}
