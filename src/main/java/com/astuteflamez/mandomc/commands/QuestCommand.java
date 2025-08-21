package com.astuteflamez.mandomc.commands;

import com.astuteflamez.mandomc.database.*;
import com.astuteflamez.mandomc.database.data.*;
import com.astuteflamez.mandomc.LangConfig;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.astuteflamez.mandomc.MandoMC.color;

public class QuestCommand implements CommandExecutor, TabCompleter {
    public QuestCommand() {}


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        FileConfiguration config = LangConfig.get();
        String prefix = color(config.getString("Prefix"));
        String noPermission = color(config.getString("NoPermission"));

        String action = args[0].toLowerCase();
        String quest = "";
        if (args.length != 1){
            quest = args[1];
        }
        try {
            switch (action) {
                case "create":
                    if (sender instanceof Player player && !player.hasPermission("mmc.quests.manage")) {

                        player.sendMessage(prefix + noPermission);
                        break;
                    }
                    String desc = args[2];
                    Quest.DurationEnum duration = Quest.DurationEnum.NONE;
                    String trigger;
                    String parent;
                    int weight = 1;
                    if (args.length == 6){
                        trigger = args[4];
                        parent = args[5];

                        QuestsTable.addQuest(new Quest(quest, desc, trigger, parent, weight));
                    }
                    else if (args.length == 7) {
                        var arg4 = args[4];

                        if (arg4.equalsIgnoreCase("daily")) {
                            duration = Quest.DurationEnum.DAILY;
                        }
                        else if (arg4.equalsIgnoreCase("weekly")) {
                            duration = Quest.DurationEnum.WEEKLY;
                        }
                        else {
                            trigger = args[4];
                            parent = args[5];
                            weight = Integer.parseInt(args[6]);

                            QuestsTable.addQuest(new Quest(quest, desc, trigger, parent, weight));
                            outputString(sender, config.getString("commands.quests.create.static.created"));
                            break;
                        }

                        trigger = args[5];
                        parent = args[6];

                        QuestsTable.addQuest(new Quest(quest, desc, trigger, duration, parent, weight));
                        outputString(sender, String.format(config.getString("commands.quests.create.timed.created" ,""), arg4));
                    }
                    else if (args.length > 7) {
                        String durationStr = args[4];
                        trigger = args[5];
                        parent = args[6];
                        weight = Integer.parseInt(args[7]);

                        if (durationStr.equalsIgnoreCase("daily")) {
                            duration = Quest.DurationEnum.DAILY;
                        } else if (durationStr.equalsIgnoreCase("weekly")) {
                            duration = Quest.DurationEnum.WEEKLY;
                        }

                        QuestsTable.addQuest(new Quest(quest, desc, trigger, duration, parent, weight));
                        outputString(sender, String.format(config.getString("commands.quests.create.timed.created", ""), durationStr));
                    }
                    else {
                        return false;
                    }
                    break;

                case "list":
                    if (Objects.equals(args[1].toLowerCase(), "all")) {
                        if (sender instanceof Player player && !player.hasPermission("mmc.quests.manage")) {
                            player.sendMessage(prefix + noPermission);
                            break;
                        }
                        StringBuilder output = new StringBuilder();
                        List<Quest> quests = QuestsTable.getAllQuests();
                        for (Quest q : quests) {
                            List<QuestReward> rewards = QuestsTable.getQuestRewards(q.getQuestName());
                            output.append(q.getDisplayString(rewards)).append("\n");
                        }
                        outputString(sender, output.toString());
                    }
                    else {
                        Player target;
                        if (args.length == 2) {
                            if (sender instanceof Player player && !player.hasPermission("mmc.quests.manage")) {
                                player.sendMessage(prefix + noPermission);
                                break;
                            }
                            target = Bukkit.getPlayer(args[1]);
                        }
                        else {
                            if (sender instanceof Player player) {
                                target = player;
                            }
                            else {
                                throw new CommandException("No Player Specified");
                            }
                        }
                        StringBuilder output = new StringBuilder();
                        if (target == null) break;
                        List<PlayerQuest> quests = PlayerQuestsTable.getInProgressQuests(target.getUniqueId().toString());
                        for (PlayerQuest q : quests) {
                            Quest quest1 = QuestsTable.getQuest(q.getQuestName());
                            if (quest1 == null) continue;
                            List<QuestReward> rewards = QuestsTable.getQuestRewards(q.getQuestName());
                            output.append(quest1.getDisplayString(rewards, q.getQuestProgress())).append("\n");
                        }

                        outputString(sender, output.toString());
                    }
                    break;
                case "delete":
                    if (sender instanceof Player player && !player.hasPermission("mmc.quests.manage")) {
                        player.sendMessage(prefix + noPermission);
                        break;
                    }
                    QuestsTable.removeQuest(quest);
                    outputString(sender, String.format(config.getString("commands.quests.delete.deleted", ""), quest));
                    break;
                case "give":
                    if (sender instanceof Player player && !player.hasPermission("mmc.quests.manage")) {
                        player.sendMessage(prefix + noPermission);
                        break;
                    }
                    String targetNameG = args[2];
                    Player target = Bukkit.getPlayer(targetNameG);
                    if (target == null) break;
                    PlayerQuestsTable.playerStartQuest(target.getUniqueId().toString(), quest);
                    outputString(sender, String.format(config.getString("commands.quests.give.given", ""), targetNameG, quest));
                    break;
                case "take":
                    if (sender instanceof Player player && !player.hasPermission("mmc.quests.manage")) {
                        player.sendMessage(prefix + noPermission);
                        break;
                    }
                    String targetNameT = args[2];
                    Player targetT = Bukkit.getPlayer(targetNameT);
                    if (targetT == null) break;
                    PlayerQuestsTable.removePlayerQuest(targetT.getUniqueId().toString(), quest);
                    outputString(sender, String.format(config.getString("commands.quests.give.given", ""), targetNameT, quest));
                    break;
                case "update":
                    if (sender instanceof Player player && !player.hasPermission("mmc.quests.manage")) {
                        player.sendMessage(prefix + noPermission);
                        break;
                    }
                    String targetNameU = args[2];
                    String progress = args[3];
                    Player targetU = Bukkit.getPlayer(targetNameU);
                    if (targetU == null) break;
                    PlayerQuestsTable.updateQuestProgress(targetU.getUniqueId().toString(), quest, Float.parseFloat(progress));
                    outputString(sender, String.format(config.getString("commands.quests.update.updated", ""), targetNameU, quest, Float.parseFloat(progress) * 100.0));
                    break;
                case "rewards":
                    if (sender instanceof Player player && !player.hasPermission("mmc.quests.manage")) {
                        player.sendMessage(prefix + noPermission);
                        break;
                    }
                    String action2 = args[2];
                    Quest quest1 = QuestsTable.getQuest(quest);
//                    List<QuestReward> rewards = QuestsTable.getQuestRewards(quest1.getQuestName());

                    if (quest1 == null) break;
                    switch (action2){
                        case "add":
                        {
                            String rewardType = args[3];
                            switch (rewardType) {
                                case "item":
                                {
                                    ItemStack item;
                                    if (args.length > 4){
                                        String itemArgs = args[4];
                                        item = Bukkit.getItemFactory().createItemStack(itemArgs);
                                    }
                                    else if (sender instanceof Player player) {
                                        item = player.getInventory().getItemInMainHand();
                                    }
                                    else {
                                        outputString(sender, config.getString("commands.quests.rewards.add.item.none"));
                                        return false;
                                    }
                                    ItemRewardsTable.addItemReward(quest1.getRewardsPool(), item);
                                    outputString(sender, String.format(config.getString("commands.quests.rewards.add.item.added", ""), item, quest));
                                    break;
                                }
                                case "event":
                                {
                                    outputString(sender, "No events are implemented yet, plz contact dev.");
                                    break;
                                }
                            }
                            break;
                        }
                        case "remove":
                        {
                            String rewardType = args[3];
                            int rewardId = Integer.parseInt(args[4]);
                            switch (rewardType){
                                case "item":
                                {
                                    ItemRewardsTable.removeItemReward(quest1.getRewardsPool(), rewardId);
                                    outputString(sender, String.format(config.getString("commands.quests.rewards.remove.item.removed", ""), rewardId, quest));
                                    break;
                                }
                                case "event":
                                {
                                    outputString(sender, "No events are implemented yet, plz contact dev.");
                                    break;
                                }
                            }
                            break;
                        }
                        case "clear":
                        {
                            QuestsTable.clearRewardPool(quest1.getRewardsPool());
                            outputString(sender, String.format(config.getString("commands.quests.rewards.clear.cleared", ""), quest));
                            break;
                        }
                    }

                    break;
                case "help":
                    StringBuilder help = new StringBuilder();
                    help.append(commandHelp("commands.quests.list.user"));

                    if ((sender instanceof Player player && player.hasPermission("mmc.quests.manage")) || !(sender instanceof Player)) {
                        help.append(commandHelp("commands.quests.list.target"));
                        help.append(commandHelp("commands.quests.list.all"));
                        help.append(commandHelp("commands.quests.create.static"));
                        help.append(commandHelp("commands.quests.create.timed"));
                        help.append(commandHelp("commands.quests.delete"));
                        help.append(commandHelp("commands.quests.give"));
                        help.append(commandHelp("commands.quests.take"));
                        help.append(commandHelp("commands.quests.update"));
                        help.append(commandHelp("commands.quests.rewards.add.item"));
                        help.append(commandHelp("commands.quests.rewards.remove"));
                        help.append(commandHelp("commands.quests.rewards.clear"));

                        // TODO: finish help text
                    }
                    help.append(commandHelp("commands.quests.help"));

                    outputString(sender, help.toString());
                    break;
            }
            return true;
        } catch (SQLException e) {
            outputString(sender, e.getStackTrace()[0].toString());
        } catch (java.lang.IllegalArgumentException e){
            outputString(sender, LangConfig.get().getString("DateFormat"));
        } catch (Exception e){
            outputString(sender, e.getStackTrace()[0].toString());
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        ArrayList<String> completions = new ArrayList<>();
        Collection<Player> players = (Collection<Player>) Bukkit.getOnlinePlayers();
        try {
            switch (args.length) {
                case 0:
                    completions.add("list");
                    if (sender instanceof Player player && !player.hasPermission("mmc.quests.manage")) {
                        completions.add("create");
                        completions.add("delete");
                        completions.add("list");
                        completions.add("give");
                        completions.add("take");
                        completions.add("update");
                    }
                    break;
                case 1:
                    if (args[0].equalsIgnoreCase("list")) {
                        if (sender instanceof Player player && !player.hasPermission("mmc.quests.manage")) break;
                        completions.add("all");

                        for (Player p : players) {
                            completions.add(p.getName());
                        }
                        break;
                    } else if (!args[0].equalsIgnoreCase("create")) {
                        try {
                            List<Quest> quests = QuestsTable.getAllQuests();
                            for (Quest q : quests) {
                                completions.add(q.getQuestName());
                            }
                        } catch (SQLException e) {
                            throw new CommandException(e.getMessage());
                        }
                    }
                    break;
                case 2:
                    switch (args[0].toLowerCase()) {
                        case "give":
                        case "take":
                        case "update":
                            for (Player p : players) {
                                completions.add(p.getName());
                            }
                            break;
                    }
            }
        } catch (CommandException e) {
            outputString(sender, e.getStackTrace()[0].toString());
        } catch (Exception e) {
            outputString(sender, e.getStackTrace()[0].toString());
        }
        return completions;
    }

    private static void outputString(@NotNull CommandSender sender, String output) {
        if (sender instanceof Player player) {
            player.sendMessage(output);
        }
        else {
            Bukkit.getConsoleSender().sendMessage(output);
        }
    }

    private static String commandHelp(String commandKey){
        FileConfiguration config = LangConfig.get();
        return "\n" + config.getString(commandKey+ ".command") + "\n   " + config.getString(commandKey + ".description");
    }
}
