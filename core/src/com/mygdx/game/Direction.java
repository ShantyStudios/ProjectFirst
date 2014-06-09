package com.mygdx.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Deliquescence <Deliquescence1@gmail.com>
 */
public enum Direction {

    UP, RIGHT, DOWN, LEFT;

    private static final List<Direction> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    public static Direction randomDirection() {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }
}
