package com.arcunis.vaultprovider;

import org.bukkit.Bukkit;

import java.sql.*;

public class Database {

    public final Connection conn;

    public Database() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + Main.dataPath + "/economy.db");
        } catch (SQLException e) {
            Main.logger.warning("Could not connect to database.");
            throw new RuntimeException(e);
        }
    }

    public void createTables(Main main) {
        try {

            PreparedStatement kvTable = conn.prepareStatement("CREATE TABLE IF NOT EXISTS kv (key TEXT PRIMARY KEY NOT NULL, value BLOB);");
            kvTable.execute();

            PreparedStatement accountTable = conn.prepareStatement("CREATE TABLE IF NOT EXISTS accounts (uuid VARCHAR(36) NOT NULL PRIMARY KEY, balance DOUBLE NOT NULL);");
            accountTable.execute();

            PreparedStatement bankTable = conn.prepareStatement("CREATE TABLE IF NOT EXISTS banks (name VARCHAR(255) NOT NULL PRIMARY KEY, owner UUID NOT NULL, balance DOUBLE NOT NULL);");
            bankTable.execute();

            PreparedStatement bankMembersTable = conn.prepareStatement("CREATE TABLE IF NOT EXISTS bank_members (bank_name VARCHAR(255) NOT NULL, member_uuid UUID NOT NULL, PRIMARY KEY (bank_name, member_uuid), FOREIGN KEY (bank_name) REFERENCES banks(name) ON DELETE CASCADE);");
            bankMembersTable.execute();
        } catch (SQLException e) {
            main.getLogger().warning("Could not create tables.");
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
