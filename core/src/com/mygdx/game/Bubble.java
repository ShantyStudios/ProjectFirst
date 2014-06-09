package com.mygdx.game;

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
}
