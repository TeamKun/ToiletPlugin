package net.kunmc.lab.toiletplugin.game.quest;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DefecationType
{
    SHIFT_MASH("連打して"),
    SHIFT_HOLD("押し続けて");

    @Getter
    private final String message;
}
