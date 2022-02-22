package net.kunmc.lab.toiletplugin.game.quest;

import lombok.Getter;

public enum QuestPhase
{
    NONE(null, null, false, true),
    SCHEDULED(null, null, false, true),
    STARTED("トイレに向かう", "使えるトイレを探して中に入ろう！", true, false),
    TOILET_JOINED("排便をしよう", "便器の中に入ってシフトを%sメーターを緑にしよう！", false, true),
    PLAYER_COOLDOWN("待機しよう", "クールダウン中, 待機しましょう！", false, true);

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
