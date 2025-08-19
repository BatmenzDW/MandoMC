package com.astuteflamez.mandomc.features.quests;

import com.astuteflamez.mandomc.MandoMC;
import com.astuteflamez.mandomc.database.QuestsTable;
import com.astuteflamez.mandomc.database.data.Quest;
import com.astuteflamez.mandomc.database.data.QuestReward;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.time.LocalDateTime;

public class TimedQuestScheduler {

    private static final String PREFIX = "§4§lQuests §8» §7";

    private static TimedQuestScheduler instance;
    private final Random random = new Random();
    private List<Quest> dailyQuestPool = new ArrayList<>();
    private List<Quest> weeklyQuestPool = new ArrayList<>();

    public static TimedQuestScheduler getInstance() {
        if (instance == null) {
            instance = new TimedQuestScheduler();
        }
        return instance;
    }

    public void chooseDailyQuests(LocalDateTime tomorrow){
        try {
            dailyQuestPool = QuestsTable.getWeightedDailyQuests();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[Quests] Failed to get daily quests");
        }

        if (dailyQuestPool.isEmpty()) {
            Bukkit.getLogger().warning("[Quests] Daily quest pool is empty. Skipping daily quests.");
            return;
        }

        int dailyQuestsCount = MandoMC.getInstance().getConfig().getInt("quests.daily.number");
        List<Quest> dailyQuests = new ArrayList<>();

        try {
            for (int i = 0; i < dailyQuestsCount; i++) {
                Quest selected = dailyQuestPool.get(random.nextInt(dailyQuestPool.size()));

                // TODO: Add checks to make sure quests are valid daily quests


                QuestsTable.updateQuestExpiration(selected.getQuestName(), Timestamp.valueOf(tomorrow));
                dailyQuests.add(selected);
            }


            Bukkit.broadcastMessage(PREFIX + "§eDaily Quests have been refreshed.");

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.0f);
                player.sendActionBar("§6§lDaily Quests refreshed");
                player.sendMessage("New Daily Quests:");
                for (Quest quest : dailyQuests) {
                    List<QuestReward> rewards = QuestsTable.getQuestRewards(quest.getQuestName());
                    player.sendMessage(quest.getDisplayString(rewards));
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[Quests] Failed to update daily quests");
        }

        scheduleNewDailyQuests();
    }

    public void chooseWeeklyQuests(LocalDateTime nextWeek){
        boolean hasActiveWeeklyQuests = false;
        try {
            var activeWeeklyQuests = QuestsTable.getActiveWeeklyQuests();
            if (!activeWeeklyQuests.isEmpty()) {
                hasActiveWeeklyQuests = true;
            }
            weeklyQuestPool = QuestsTable.getWeightedWeeklyQuests();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[Quests] Failed to get weekly quests");
        }

        if (hasActiveWeeklyQuests) {
            // Don't need to choose yet if some are still active
            scheduleNewWeeklyQuests();
            return;
        }

        if (weeklyQuestPool.isEmpty()) {
            Bukkit.getLogger().warning("[Quests] Weekly quest pool is empty. Skipping weekly quests.");
            return;
        }

        int weeklyQuestsCount = MandoMC.getInstance().getConfig().getInt("quests.weekly.number");

        List<Quest> weeklyQuests = new ArrayList<>();
        try {
            for (int i = 0; i < weeklyQuestsCount; i++) {
                Quest selected = weeklyQuestPool.get(random.nextInt(weeklyQuestPool.size()));

                // TODO: Add checks to make sure quests are valid weekly quests


                QuestsTable.updateQuestExpiration(selected.getQuestName(), Timestamp.valueOf(nextWeek));
                weeklyQuests.add(selected);
            }


            Bukkit.broadcastMessage(PREFIX + "§eWeekly Quests have been refreshed.");

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.0f);
                player.sendActionBar("§6§lWeekly Quests refreshed");
                player.sendMessage("New Weekly Quests:");
                for (Quest quest : weeklyQuests) {
                    List<QuestReward> rewards = QuestsTable.getQuestRewards(quest.getQuestName());
                    player.sendMessage(quest.getDisplayString(rewards));
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[Quests] Failed to update weekly quests");
        }

        scheduleNewWeeklyQuests();
    }

    private void scheduleNewDailyQuests() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);
        long delayMinutes = Duration.between(LocalDateTime.now(), tomorrow).toMinutes();

        long delayTicks = delayMinutes * 60L * 20;

        new BukkitRunnable() {
            @Override
            public void run() {
                chooseDailyQuests(tomorrow.plusDays(1));
            }
        }.runTaskLater(MandoMC.getInstance(), delayTicks);
    }

    private void scheduleNewWeeklyQuests() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

        long delayMinutes = Duration.between(LocalDateTime.now(), cal.toInstant()).toMinutes();

        long delayTicks = delayMinutes * 60L * 20;

        new BukkitRunnable() {
            @Override
            public void run() {
                chooseWeeklyQuests(LocalDateTime.ofInstant(cal.toInstant(), cal.getTimeZone().toZoneId()).toLocalDate().atStartOfDay());
            }
        }.runTaskLater(MandoMC.getInstance(), delayTicks);
    }
}
