package com.arcunis.vaultprovider;

import com.google.gson.Gson;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {

    public static Path dataPath;
    public static Logger logger;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Create files
        try {
            if (!getDataFolder().exists()) getDataFolder().mkdir();
            File dbFile = new File(getDataFolder(), "economy.db");
            if (!dbFile.exists()) dbFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Set sratic variables
        dataPath = getDataPath();
        logger = getLogger();

        // Create db tables
        Database db = new Database();
        db.createTables(this);

        // Register vault economy provider
        try {
            Class.forName("net.milkbowl.vault.economy.Economy");
            getServer().getServicesManager().register(Economy.class, new EconomyProvider(this), this, ServicePriority.Normal);
        } catch (ClassNotFoundException e) {
            getLogger().warning("Could not register economy provider. Could not find Vault. Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            throw new RuntimeException(e);
        }

    }

}
