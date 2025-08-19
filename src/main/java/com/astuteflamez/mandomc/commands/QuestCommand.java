package com.astuteflamez.mandomc.commands;

import com.astuteflamez.mandomc.database.*;
import com.astuteflamez.mandomc.database.data.*;
import com.astuteflamez.mandomc.LangConfig;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
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
                    String trigger = null;
                    String parent = null;
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
                            break;
                        }

                        trigger = args[5];
                        parent = args[6];

                        QuestsTable.addQuest(new Quest(quest, desc, trigger, duration, parent, weight));
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
                        OutputString(sender, output.toString());
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
                        List<PlayerQuest> quests = PlayerQuestsTable.getInProgressQuests(target.getUniqueId().toString());
                        for (PlayerQuest q : quests) {
                            Quest quest1 = QuestsTable.getQuest(q.getQuestName());
                            List<QuestReward> rewards = QuestsTable.getQuestRewards(q.getQuestName());
                            output.append(quest1.getDisplayString(rewards, q.getQuestProgress())).append("\n");
                        }

                        OutputString(sender, output.toString());
                    }
                    break;
                case "delete":
                    if (sender instanceof Player player && !player.hasPermission("mmc.quests.manage")) {
                        player.sendMessage(prefix + noPermission);
                        break;
                    }
                    QuestsTable.removeQuest(quest);
                    break;
                case "give":
                    if (sender instanceof Player player && !player.hasPermission("mmc.quests.manage")) {
                        player.sendMessage(prefix + noPermission);
                        break;
                    }
                    String targetNameG = args[2];

                    PlayerQuestsTable.playerStartQuest(Bukkit.getPlayer(targetNameG).getUniqueId().toString(), quest);
                    break;
                case "take":
                    if (sender instanceof Player player && !player.hasPermission("mmc.quests.manage")) {
                        player.sendMessage(prefix + noPermission);
                        break;
                    }
                    String targetNameT = args[2];

                    PlayerQuestsTable.removePlayerQuest(Bukkit.getPlayer(targetNameT).getUniqueId().toString(), quest);
                    break;
                case "update":
                    if (sender instanceof Player player && !player.hasPermission("mmc.quests.manage")) {
                        player.sendMessage(prefix + noPermission);
                        break;
                    }
                    String targetNameU = args[2];
                    String progress = args[3];

                    PlayerQuestsTable.updateQuestProgress(Bukkit.getPlayer(targetNameU).getUniqueId().toString(), quest, Float.parseFloat(progress));
                    break;
                case "rewards":
                    String action2 = args[2];

                    // TODO: finish rewards command
                    switch (action2){
                        case "add":
                        {
                            break;
                        }
                        case "remove":
                        {
                            break;
                        }
                        case "clear":
                        {
                            break;
                        }
                    }

                    break;
                case "help":
                    StringBuilder help = new StringBuilder();
                    help.append(config.getString("commands.quests.list.user.command")).append("\n");
                    help.append("\t").append(config.getString("commands.quests.list.user.description")).append("\n");

                    if ((sender instanceof Player player && player.hasPermission("mmc.quests.manage")) || !(sender instanceof Player)) {
                        help.append(config.getString("commands.quests.list.target.command")).append("\n");
                        help.append("\t").append(config.getString("commands.quests.list.target.description")).append("\n");

                        help.append(config.getString("commands.quests.list.all")).append("\n");
                        help.append("\t").append(config.getString("commands.quests.list.all.description")).append("\n");

                        help.append(config.getString("commands.quests.create.static.command")).append("\n");
                        help.append("\t").append(config.getString("commands.quests.create.static.description")).append("\n");

                        help.append(config.getString("commands.quests.create.timed.command")).append("\n");
                        help.append("\t").append(config.getString("commands.quests.create.timed.description")).append("\n");

                        // TODO: finish help text
                    }
                    help.append(config.getString("commands.quests.help.command")).append("\n");
                    help.append("\t").append(config.getString("commands.quests.help.description")).append("\n");

                    OutputString(sender, help.toString());
                    break;
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (java.lang.IllegalArgumentException e){
            OutputString(sender, LangConfig.get().getString("DateFormat"));
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        ArrayList<String> completions = new ArrayList<>();
        switch (args.length){
            case 0:
                completions.add("list");
                if (sender instanceof Player player && !player.hasPermission("mmc.quests.manage")){
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
                    ArrayList<Player> players = (ArrayList<Player>) Bukkit.getOnlinePlayers();
                    for (Player p: players){
                        completions.add(p.getName());
                    }
                    break;
                }
                else if (!args[0].equalsIgnoreCase("create")) {
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
                switch (args[0].toLowerCase()){
                    case "give":
                    case "take":
                    case "update":
                        ArrayList<Player> players = (ArrayList<Player>) Bukkit.getOnlinePlayers();
                        for (Player p: players){
                            completions.add(p.getName());
                        }
                        break;
                }
        }
        return completions;
    }

    private static void OutputString(@NotNull CommandSender sender, String output) {
        if (sender instanceof Player player) {
            player.sendMessage(output);
        }
        else {
            Bukkit.getConsoleSender().sendMessage(output);
        }
    }
}
