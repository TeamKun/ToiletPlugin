package net.kunmc.lab.toiletplugin.game;

import net.kunmc.lab.toiletplugin.game.config.Config;

public class GameConfig
{
    @Config(min = 1, helpMessage = "クエストが始まってから死ぬまでの秒数範囲：最小")
    public int minQuestProgressTime = 20;
    @Config(min = 1, helpMessage = "クエストが始まってから死ぬまでの秒数範囲：最大")
    public int maxQuestProgressTime = 30;

}
