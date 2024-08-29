package com.arcunis.vaultprovider;

import com.arcunis.vaultprovider.events.OnPlayerJoin;
import com.google.gson.Gson;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Path;
import java.sql.*;
import java.util.logging.Logger;

public final class Main extends JavaPlugin implements Listener {

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

        // Set static variables
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


        if (getConfig().getBoolean("auto-create-player-acc")) {

            // Register existing players
            registerExistingPlayers();

            // Register player join event
            Bukkit.getPluginManager().registerEvents(new OnPlayerJoin(), this);

        }

    }

    public void registerExistingPlayers() {
        try {
            Database db = new Database();
            ResultSet res = db.conn.prepareStatement("SELECT (key) FROM kv WHERE key = ran").executeQuery();
            if (res.getString("key") != null) {
                return;
            }
            db.conn.prepareStatement("INSERT INTO kv (key) VALUES (ran);").execute();
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                EconomyManager.createAcc(player.getUniqueId());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
