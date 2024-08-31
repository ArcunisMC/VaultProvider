package com.arcunis.vaultprovider;

import com.arcunis.vaultprovider.commands.VaultProvider;
import com.arcunis.vaultprovider.economy.EconomyProvider;
import com.arcunis.vaultprovider.economy.VPEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.logging.Logger;

public final class Main extends JavaPlugin implements Listener {

    public static Path dataPath;
    public static Logger logger;

    public static Economy econ;

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

        try {
            Class.forName("net.milkbowl.vault.economy.Economy");
            getServer().getServicesManager().register(Economy.class, econ, this, ServicePriority.Normal);
        } catch (ClassNotFoundException e) {
            getLogger().warning("Could not register economy provider. Could not find Vault. Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            throw new RuntimeException(e);
        }

        // Register events
        Bukkit.getPluginManager().registerEvents(new Events(this), this);

        // Register command
        new VaultProvider(this);

    }

}
