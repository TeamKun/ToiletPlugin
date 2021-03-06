package net.kunmc.lab.toiletplugin.game.config;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.toiletplugin.game.quest.DefecationType;
import net.kunmc.lab.toiletplugin.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
@Setter
public class GameConfig
{
    private static final Random RANDOM = new Random();

    // Quest

    // Quest => Time
    @Config(min = 1, ranged = true, helpMessage = "クエスト時間")
    int minQuestTime = 30;
    @Config(min = 2, ranged = true, helpMessage = "クエスト時間")
    int maxQuestTime = 180;

    @Config(min = 1, ranged = true, helpMessage = "クエストが始まるまでの時間")
    int minScheduleTime = 30;
    @Config(min = 1, ranged = true, helpMessage = "クエストが始まるまでの時間")
    int maxScheduleTime = 180;

    // Quest => Scheduler
    @Config(helpMessage = "ゲーム参加時のクエストの自動ケジュール")
    boolean autoScheduleOnJoin = false;
    @Config(helpMessage = "リスポーン時のクエストの自動リスケジュール")
    boolean autoRescheduleOnRespawn = true;
    @Config(helpMessage = "クエスト完了時のクエストの自動リスケジュール")
    boolean autoRescheduleOnSuccess = true;

    // Quest => Questing
    @Config(min = 1, max = 5, helpMessage = "便の数", ranged = true)
    int minPoopAmount = 1;
    @Config(min = 2, max = 6, helpMessage = "便の数", ranged = true)
    int maxPoopAmount = 3;
    @Config(helpMessage = "クエスト失敗時プレイヤの場所溶岩設置")
    boolean placeLavaOnFail = false;
    @Config(helpMessage = "クエスト失敗時プレイヤ周囲に便をばらまく")
    boolean spreadPoopOnFail = true;
    @Config(helpMessage = "クエスト失敗時プレイヤ周囲に便をばらまく数")
    int spreadPoopAmount = 6;
    @Config(helpMessage = "クエスト失敗時プレイヤ周囲に便をばらまく最大強さ")
    double spreadPoopDistanceMax = 0.6;

    // Player

    // Player => Logic
    @Config(helpMessage = "プレイヤのトイレ後クールダウン時間")
    boolean playerCooldownEnable = true;
    @Config(min = 1, ranged = true, helpMessage = "プレイヤのトイレ後クールダウン時間")
    int minPlayerCooldownTime = 30;
    @Config(min = 1, ranged = true, helpMessage = "プレイヤのトイレ後クールダウン時間")
    int maxPlayerCooldownTime = 31;
    @Config(helpMessage = "リスポーン有効化")
    boolean respawnEnable = true;
    @Config(min = 1, ranged = true, helpMessage = "リスポーンするまでの時間")
    int minRespawnTime = 5;
    @Config(min = 1, ranged = true, helpMessage = "リスポーンするまでの時間")
    int maxRespawnTime = 30;
    @Config(helpMessage = "クエスト開始時のブロードキャスト")
    boolean broadcastOnQuestStart = true;

    // Player => Effect
    @Config(helpMessage = "クエスト中の低音の再生")
    boolean questingOppressiveSoundEnable = true;
    @Config(helpMessage = "クエスト中の発光")
    boolean glowingOnQuesting = true;
    @Config(helpMessage = "クエスト中の発光の色の有効化")
    boolean glowingOnQuestingColorEnable = true;
    @Config(helpMessage = "露骨な表現")
    boolean explictExpressionEnable = true;
    // Player => Assist
    @Config(helpMessage = "トイレの地図を与える")
    boolean giveToiletMap = true;


    // Toilet

    // Toilet => Logic
    @Config(helpMessage = "トイレのトイレ後クールダウン時間")
    boolean toiletCooldownEnable = true;
    @Config(min = 1, ranged = true, helpMessage = "トイレのトイレ後クールダウン時間")
    int minToiletCooldownTime = 30;
    @Config(min = 1, ranged = true, helpMessage = "トイレのトイレ後クールダウン時間")
    int maxToiletCooldownTime = 31;

    // Toilet => Logic => Defecation
    @Config(helpMessage = "排便の方法")
    DefecationType defecationType = DefecationType.SHIFT_HOLD;
    @Config(helpMessage = "排便時に大釜の中に入る必要があるか")
    boolean strictDefecation = false;

    // Toilet => Logic => Defecation => Power
    @Config(min = 0, max = 10, helpMessage = "排便時に力を保持する必要がある時間")
    int powerKeepCountSeconds = 5;
    @Config(min = 1, max = 10, helpMessage = "スニークで与える力。ホールドモードの場合は1秒間にどれだけ与えるか。")
    int powerGainAmount = 10;
    @Config(min = 1, max = 99, helpMessage = "1秒に剥奪するパワー(スニークしていない)")
    int powerLossOnSecAmount = 20;
    @Config(min = 1, max = 100, helpMessage = "排便時に必要なパワー")
    int defecationNeedPower = 80;
    @Config(min = 0, helpMessage = "多動(余計な動き)をしたときに剥奪するパワー")
    int powerLossOnUnnecessaryActionAmount = 10;

    // Toilet => Logic => Misc
    @Config(helpMessage = "パワーが最大になったときの爆発")
    boolean enablePowerBurst = true;
    @Config(helpMessage = "トイレに入った瞬間にタイマーを止める")
    boolean stopTimerOnJoinToilet = false;

    public int generatePoopAmount()
    {
        return RANDOM.nextInt(maxPoopAmount - minPoopAmount) + minPoopAmount;
    }

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

    public int generateRespawnTime()
    {
        return RANDOM.nextInt(maxRespawnTime - minRespawnTime) + maxRespawnTime;
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
