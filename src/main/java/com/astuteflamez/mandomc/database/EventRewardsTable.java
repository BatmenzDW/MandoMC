package com.astuteflamez.mandomc.database;

import com.astuteflamez.mandomc.database.data.RewardEvent;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventRewardsTable extends Database{
    public static void initializeEventRewardsTable() throws SQLException {
        Connection connection = getConnection();

        Statement statement = connection.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS questEventRewards (PoolId int, Id int, EventName varchar(64), MetaData varchar(64), primary key (PoolId, Id))";

        statement.executeUpdate(sql);

        statement.close();

        Bukkit.getConsoleSender().sendMessage("[MandoMC] Created quest event rewards table successfully!");

        connection.close();
    }

    public static void addEventReward(int poolId, String eventName, String metadata) throws SQLException {
        int id = getEventPoolSize(poolId);

        Connection connection = getConnection();

        PreparedStatement statement = connection.prepareStatement("INSERT INTO questEventRewards(PoolId, Id, EventName, MetaData) VALUES (?,?,?,?)");
        statement.setInt(1, poolId);
        statement.setInt(2, id);
        statement.setString(3, eventName);
        statement.setString(4, metadata);

        statement.executeUpdate();
    }

    public static void removeEventReward(int poolId, int id) throws SQLException {
        Connection connection = getConnection();

        PreparedStatement statement = connection.prepareStatement("DELETE FROM questEventRewards WHERE PoolId = ? AND Id = ?");
        statement.setInt(1, poolId);
        statement.setInt(2, id);

        statement.executeUpdate();

        statement.close();
        connection.close();
    }

    public static List<RewardEvent> getEventRewards(int poolId) throws SQLException {
        List<RewardEvent> events = new ArrayList<>();

        Connection connection = getConnection();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM questEventRewards WHERE PoolId = ?");
        statement.setInt(1, poolId);

        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            int id = resultSet.getInt("Id");
            String eventName = resultSet.getString("EventName");
            String metadata = resultSet.getString("MetaData");

            events.add(RewardEvent.getRewardEvent(id, eventName, metadata, poolId));
        }

        resultSet.close();
        statement.close();
        connection.close();

        return events;
    }

    public static int getEventPoolSize(int poolId) throws SQLException {
        Connection connection = getConnection();

        PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) as size FROM questEventRewards WHERE poolId = ?");
        statement.setInt(1, poolId);

        ResultSet resultSet = statement.executeQuery();

        int size = 0;
        while (resultSet.next()) {
            size = resultSet.getInt("size");
        }

        resultSet.close();
        statement.close();
        connection.close();

        return size;
    }
}
