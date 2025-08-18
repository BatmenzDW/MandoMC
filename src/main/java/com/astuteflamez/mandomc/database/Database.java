package com.astuteflamez.mandomc.database;

import com.astuteflamez.mandomc.MandoMC;
import org.bukkit.Bukkit;

import java.sql.*;

public class Database {
    public static Connection getConnection() throws SQLException {
        String url = MandoMC.getInstance().getConfig().getString("DatabaseURL");
        String user = MandoMC.getInstance().getConfig().getString("DatabaseUSER");
        String password = MandoMC.getInstance().getConfig().getString("DatabasePASSWORD");

        //        Bukkit.getConsoleSender().sendMessage("[MandoMC] Database connected!");

        return DriverManager.getConnection(url, user, password);
    }
}
