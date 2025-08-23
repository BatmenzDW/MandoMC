package com.astuteflamez.mandomc.database;

import com.astuteflamez.mandomc.database.data.PlayerQuest ;
import com.astuteflamez.mandomc.database.data.Quest;
import com.astuteflamez.mandomc.database.data.QuestReward;

import org.bukkit.Bukkit;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class PlayerQuestsTable extends Database {
    public static void initializePlayerQuestsTable() throws SQLException {
        Connection connection = getConnection();

        Statement statement = connection.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS PlayerQuests(uuid varchar(36), QuestName varchar(64), Progress float, primary key (uuid, QuestName))";

        statement.executeUpdate(sql);

        statement.close();

        Bukkit.getConsoleSender().sendMessage("[MandoMC] Created player quests table successfully!");

        connection.close();
    }

    public static void playerStartQuest(String uuid, String questName) throws SQLException {
        Connection connection = getConnection();

        PreparedStatement statement = connection.prepareStatement("INSERT INTO PlayerQuests(uuid, QuestName, Progress) VALUES(?,?,?)");
        statement.setString(1, uuid);
        statement.setString(2, questName);
        statement.setFloat(3, 0.0f);

        statement.executeUpdate();

        statement.close();
        connection.close();
    }

    private static void playerStartQuests(String uuid, ArrayList<String> questNames) throws SQLException {
        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO PlayerQuests(uuid, QuestName, Progress) VALUES(?,?,?)");
        ) {
            for (String questName : questNames) {
                statement.setString(1, uuid);
                statement.setString(2, questName);
                statement.setFloat(3, 0.0f);

                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    public static void removePlayerQuest(String uuid, String questName) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement("DELETE FROM PlayerQuests WHERE uuid = ? AND QuestName = ?");
        statement.setString(1, uuid);
        statement.setString(2, questName);

        statement.executeUpdate();

        statement.close();
        connection.close();
    }

    private static void playerFinishQuest(String uuid, String questName) throws SQLException {
        Connection connection = getConnection();

        PreparedStatement statement = connection.prepareStatement("UPDATE PlayerQuests SET Progress = ? WHERE uuid = ? AND QuestName = ?");

        statement.setFloat(1, 1.0f);
        statement.setString(2, uuid);
        statement.setString(3, questName);

        statement.executeUpdate();
        statement.close();
        connection.close();

        List<QuestReward> rewards = QuestsTable.getQuestRewards(questName);

        for (QuestReward reward : rewards)
        {
            reward.givePlayer(uuid);
        }

        List<Quest> quests = QuestsTable.getChildren(questName);

        if (quests.isEmpty()) {
            return;
        }

        ArrayList<String> questNames = new ArrayList<>();
        for (Quest quest : quests) {
            questNames.add(quest.getQuestName());
        }

        playerStartQuests(uuid, questNames);
    }

    public static void updateQuestProgress(String uuid, String questName, float progress) throws SQLException {
        if (progress <= 0.0f) {
            progress = 0.0f;
        }
        else if (progress > 1.0f) {
            progress = 1.0f;
        }

        Connection connection = getConnection();

        PreparedStatement statement = connection.prepareStatement("UPDATE PlayerQuests SET Progress = ? WHERE uuid = ? AND QuestName = ?");

        statement.setFloat(1, progress);
        statement.setString(2, uuid);
        statement.setString(3, questName);

        statement.executeUpdate();

        statement.close();
        connection.close();

        if (progress >= 1.0f) {
            playerFinishQuest(uuid, questName);
        }
    }

    public static PlayerQuest getQuest(String uuid, String questName) throws SQLException {
        Connection connection = getConnection();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM PlayerQuests WHERE uuid = ? AND QuestName = ?");
        statement.setString(1, uuid);
        statement.setString(2, questName);

        ResultSet resultSet = statement.executeQuery();

        PlayerQuest quest = null;
        if (resultSet.next()) {
            quest = new PlayerQuest(UUID.fromString(uuid), questName, resultSet.getFloat("Progress"));
        }

        resultSet.close();
        statement.close();
        connection.close();

        return quest;
    }

    public static List<PlayerQuest> getActiveQuests(String uuid) throws SQLException {
        Connection connection = getConnection();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM PlayerQuests WHERE uuid = ? ORDER BY Progress DESC");
        statement.setString(1, uuid);

        ResultSet resultSet = statement.executeQuery();

        List<PlayerQuest> quests = new ArrayList<>();
        if (resultSet.next()) {
            PlayerQuest quest = new PlayerQuest(UUID.fromString(uuid), resultSet.getString("QuestName"), resultSet.getFloat("Progress"));
            quests.add(quest);
        }

        resultSet.close();
        statement.close();
        connection.close();

        return quests;
    }

    public static List<PlayerQuest> getInactiveTimedQuests(String uuid) throws SQLException {
        Connection connection = getConnection();

        PreparedStatement statement = connection.prepareStatement("SELECT q.QuestName as QuestName FROM quests as q LEFT JOIN PlayerQuests as p ON p.QuestName = q.QuestName WHERE (p.uuid IS NULL OR p.uuid != ?) AND q.Expiration > CURRENT_TIMESTAMP");
        statement.setString(1, uuid);

        ResultSet resultSet = statement.executeQuery();

        List<PlayerQuest> quests = new ArrayList<>();
        if (resultSet.next()) {
            PlayerQuest quest = new PlayerQuest(UUID.fromString(uuid), resultSet.getString("QuestName"), 0.0f);
            quests.add(quest);
        }

        resultSet.close();
        statement.close();
        connection.close();

        return quests;
    }

    public static List<PlayerQuest> getInProgressQuests(String uuid) throws SQLException {
        Connection connection = getConnection();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM PlayerQuests WHERE uuid = ? AND Progress < 1.0 ORDER BY Progress DESC");
        statement.setString(1, uuid);

        ResultSet resultSet = statement.executeQuery();

        List<PlayerQuest> inProgressQuests = new ArrayList<>();
        while (resultSet.next()) {
            PlayerQuest quest = new PlayerQuest(UUID.fromString(uuid), resultSet.getString("QuestName"), resultSet.getFloat("Progress"));
            inProgressQuests.add(quest);
        }

        resultSet.close();
        statement.close();
        connection.close();

        return inProgressQuests;
    }

    public static List<PlayerQuest> getCompletedQuests(String uuid) throws SQLException {
        Connection connection = getConnection();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM PlayerQuests WHERE uuid = ? AND Progress >= 1.0 ORDER BY Progress DESC");
        statement.setString(1, uuid);

        ResultSet resultSet = statement.executeQuery();

        List<PlayerQuest> completedQuests = new ArrayList<>();
        if (resultSet.next()) {
            PlayerQuest quest = new PlayerQuest(UUID.fromString(uuid), resultSet.getString("QuestName"), resultSet.getFloat("Progress"));
            completedQuests.add(quest);
        }

        resultSet.close();
        statement.close();
        connection.close();

        return completedQuests;
    }

    public static List<PlayerQuest> getTriggeredQuests(String uuid, String trigger) throws SQLException {
        Connection connection = getConnection();

        PreparedStatement statement = connection.prepareStatement("SELECT p.QuestName, p.Progress, q.QuestTrigger, q.Parent FROM `PlayerQuests` as p INNER JOIN `quests` as q ON p.QuestName = q.QuestName WHERE p.Progress < 1.0 AND p.uuid = ? AND q.QuestTrigger = ? AND (q.Expiration > ? OR q.Expiration IS NULL) ORDER BY Progress DESC");
        statement.setString(1, uuid);
        statement.setString(2, trigger);
        statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

        ResultSet resultSet = statement.executeQuery();

        List<PlayerQuest> triggeredQuests = new ArrayList<>();
        if (resultSet.next()) {
            PlayerQuest quest = new PlayerQuest(UUID.fromString(uuid), resultSet.getString("QuestName"), resultSet.getFloat("Progress"));
            triggeredQuests.add(quest);
        }

        resultSet.close();
        statement.close();
        connection.close();

        return triggeredQuests;
    }

    public static void clearTimeLimitedQuests() throws SQLException {
        Connection connection = getConnection();

        PreparedStatement statement = connection.prepareStatement("DELETE FROM PlayerQuests as p INNER JOIN Quests as Q on Q.QuestName = P.QuestName WHERE Q.Expiration <= ?");

        statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));

        int rowsAffected = statement.executeUpdate();
        Bukkit.getConsoleSender().sendMessage("[MandoMC] Cleared " + rowsAffected + " Daily/Weekly Quests Progress.");

        statement.close();
        connection.close();
    }
}
