package net.kunmc.lab.toiletplugin.game;

import net.kunmc.lab.toiletplugin.game.config.Config;

public class GameConfig
{
    @Config(helpMessage = "テスト。")
    private String test;

    @Config(toggle = true)
    private boolean hoge;

    @Config(min = 20, max = 25)
    private int fuga;

    @Config(name = "testB")
    private String hogera;
}
