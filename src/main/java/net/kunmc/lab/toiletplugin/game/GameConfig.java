package net.kunmc.lab.toiletplugin.game;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.toiletplugin.game.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
@Setter
public class GameConfig
{
    private static final Random RANDOM = new Random();
    @Config(min = 1, helpMessage = "クエスト時間：最小")
    int minQuestTime = 30;
    @Config(min = 2, helpMessage = "クエスト時間：最大")
    int maxQuestTime = 180;
    @Config(min = 1, helpMessage = "クエストが始まるまでの時間：最小")
    int minScheduleTime = 30;
    @Config(min = 1, helpMessage = "クエストが始まるまでの時間：最大")
    int maxScheduleTime = 180;
    @Config(helpMessage = "ゲーム参加時のクエストの自動ケジュール")
    boolean autoScheduleOnJoin = false;
    @Config(helpMessage = "リスポーン時のクエストの自動リスケジュール")
    boolean autoRescheduleOnRespawn = true;
    @Config(helpMessage = "クエスト完了時のクエストの自動リスケジュール")
    boolean autoRescheduleOnSuccess = true;

    public int generateQuestTime()
    {
        return RANDOM.nextInt(maxQuestTime - minQuestTime) + minQuestTime;
    }

    public int generateScheduleTime()
    {
        return RANDOM.nextInt(maxScheduleTime - minScheduleTime) + minScheduleTime;
    }

    public String[] checkConfig()
    {
        List<String> errors = new ArrayList<>();
        if (minQuestTime >= maxQuestTime)
            errors.add("questTime：最小値が最大値より大きいまたは同じです。");
        if (minScheduleTime >= maxScheduleTime)
            errors.add("scheduleTime：最小値が最大値より大きいまたは同じです。");
        return errors.toArray(new String[0]);
    }
}
