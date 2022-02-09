package net.kunmc.lab.toiletplugin.game.quest;

import lombok.Getter;

public enum QuestPhase
{
    NONE(null, null, false, true),
    SCHEDULED(null, null, false, true),
    STARTED("トイレに向かう", "使えるトイレを探して中に入ろう！", true, false),
    TOILET_JOINED("排便をしよう", "便器の中に入って指示通りにしゃがもう！", false, true),
    DEFECATION_STARTED("排便をしよう", "%s！", false, true),
    DEFECATION_FINISHED("トイレから出よう", "トイレから出よう！", false, true);

    @Getter
    private final String subTitle;
    @Getter
    private final String title;
    @Getter
    private final boolean isEmergency;
    @Getter
    private final boolean isSubQuest;

    QuestPhase(String title, String subTitle, boolean isEmergency, boolean isSubQuest)
    {
        this.title = title;
        this.subTitle = subTitle;
        this.isEmergency = isEmergency;
        this.isSubQuest = isSubQuest;
    }
}