package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;

/**
 *
 * @author Deliquescence <Deliquescence1@gmail.com>
 */
public class Bubble extends Rectangle {

    public float speedModifier;
    public float size;

    public Bubble() {

    }

    private final int BASE_SIZE = 64;

    public void setSizeByModifier(float mod) {
        this.size = BASE_SIZE * mod;
        this.width = size;
        this.height = size;
    }

    public void move() {

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
