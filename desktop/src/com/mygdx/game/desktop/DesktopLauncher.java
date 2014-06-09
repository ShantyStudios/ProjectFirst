package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.MyGdxGame;

public class DesktopLauncher {

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = MyGdxGame.Title;
        config.width = MyGdxGame.V_Width * MyGdxGame.Scale;
        config.height = MyGdxGame.V_Height * MyGdxGame.Scale;
        new LwjglApplication(new MyGdxGame(), config);
    }
}
