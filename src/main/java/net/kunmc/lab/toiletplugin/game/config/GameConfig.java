package net.kunmc.lab.toiletplugin.game.config;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.toiletplugin.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
@Setter
public class GameConfig
{
    private static final Random RANDOM = new Random();

    @Config(min = 1, ranged = true, helpMessage = "クエスト時間：最小")
    int minQuestTime = 30;
    @Config(min = 2, ranged = true, helpMessage = "クエスト時間：最大")
    int maxQuestTime = 180;

    @Config(min = 1, ranged = true, helpMessage = "クエストが始まるまでの時間：最小")
    int minScheduleTime = 30;
    @Config(min = 1, ranged = true, helpMessage = "クエストが始まるまでの時間：最大")
    int maxScheduleTime = 180;

    @Config(helpMessage = "プレイヤのトイレ後クールダウン時間")
    boolean playerCooldownEnable = true;
    @Config(min = 1, ranged = true, helpMessage = "プレイヤのトイレ後クールダウン時間：最小")
    int minPlayerCooldownTime = 30;
    @Config(min = 1, ranged = true, helpMessage = "プレイヤのトイレ後クールダウン時間：最大")
    int maxPlayerCooldownTime = 31;

    @Config(helpMessage = "トイレのトイレ後クールダウン時間")
    boolean toiletCooldownEnable = true;
    @Config(min = 1, ranged = true, helpMessage = "トイレのトイレ後クールダウン時間：最小")
    int minToiletCooldownTime = 30;
    @Config(min = 1, ranged = true, helpMessage = "トイレのトイレ後クールダウン時間：最大")
    int maxToiletCooldownTime = 31;

    @Config(helpMessage = "ゲーム参加時のクエストの自動ケジュール")
    boolean autoScheduleOnJoin = false;
    @Config(helpMessage = "リスポーン時のクエストの自動リスケジュール")
    boolean autoRescheduleOnRespawn = true;
    @Config(helpMessage = "クエスト完了時のクエストの自動リスケジュール")
    boolean autoRescheduleOnSuccess = true;

    @Config(helpMessage = "クエスト中の低音の再生")
    boolean questingOppressiveSoundEnable = true;

    @Config(min = 1, max = 10, helpMessage = "スニークで与える力")
    int powerGainOnSecAmount = 80;
    @Config(min = 1, max = 20, helpMessage = "1秒に剥奪するパワー(スニークしていない)")
    int powerLossOnSecAmount = 80;
    @Config(min = 1, max = 100, helpMessage = "排便時の最小パワー")
    int minDefecationAcceptPower = 80;
    @Config(helpMessage = "パワーが100(最大)を超えたのバースト(爆発)有効化")
    boolean burstOnPowerOver100 = true;

    public int generateQuestTime()
    {
        return RANDOM.nextInt(maxQuestTime - minQuestTime) + minQuestTime;
    }

    public int generateScheduleTime()
    {
        return RANDOM.nextInt(maxScheduleTime - minScheduleTime) + minScheduleTime;
    }

    public int generatePlayerCooldownTime()
    {
        return RANDOM.nextInt(maxPlayerCooldownTime - minPlayerCooldownTime) + minPlayerCooldownTime;
    }

    public int generateToiletCooldownTime()
    {
        return RANDOM.nextInt(maxToiletCooldownTime - minToiletCooldownTime) + minToiletCooldownTime;
    }

    public List<Pair<String, String>> checkConfig()
    {
        List<Pair<String, String>> errors = new ArrayList<>();
        if (minQuestTime >= maxQuestTime)
            errors.add(new Pair<>("questTime", "最小値が最大値より大きいまたは同じです。"));
        if (minScheduleTime >= maxScheduleTime)
            errors.add(new Pair<>("scheduleTime", "最小値が最大値より大きいまたは同じです。"));
        if (minPlayerCooldownTime >= maxPlayerCooldownTime)
            errors.add(new Pair<>("playerCooldownTime", "最小値が最大値より大きいまたは同じです。"));
        if (minToiletCooldownTime >= maxToiletCooldownTime)
            errors.add(new Pair<>("toiletCooldownTime", "最小値が最大値より大きいまたは同じです。"));
        return errors;
    }
}
