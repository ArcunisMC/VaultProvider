package com.arcunis.vaultprovider;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class Database {

    public final Connection conn;

    public Database() {
        try {
            // Create file if not exists
            File dbFile = new File(Main.dataPath.toFile(), "vaultprovider.db");
            if (!dbFile.exists()) dbFile.createNewFile();

            // Connect to db
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());

        } catch (SQLException e) {
            Main.logger.warning("Could not connect to database.");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createTables(Main main) {
        try {

            // Economy
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
