package com.arcunis.vaultprovider;

import com.arcunis.vaultprovider.economy.EconomyProvider;
import com.arcunis.vaultprovider.economy.VPEconomy;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.logging.Logger;

public final class Main extends JavaPlugin implements Listener {

    public static Path dataPath;
    public static Logger logger;

    public static Economy econ;
    public static Permission perm;
    public static Chat chat;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Create data folder
        if (!getDataFolder().exists()) getDataFolder().mkdir();

        // Set static variables
        dataPath = getDataPath();
        logger = getLogger();

        // Create db tables
        Database db = new Database();
        db.createTables(this);

        // Initialize economy
        econ = new EconomyProvider(this);
        new VPEconomy().onEnable(this);

        // Initialize permission


        // Initialize chat

    }

}
