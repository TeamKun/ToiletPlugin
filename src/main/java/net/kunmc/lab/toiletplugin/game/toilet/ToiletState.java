package net.kunmc.lab.toiletplugin.game.toilet;

import lombok.Getter;

public enum ToiletState
{
    OPEN("使用可能"),
    PLAYER_USING("使用中"),
    PLAYER_COOLDOWN("クールダウン中"),
    TOILET_COOLDOWN("クールダウン中");

    @Getter
    private final String displayName;

    ToiletState(String displayName)
    {
        this.displayName = displayName;
    }

}
