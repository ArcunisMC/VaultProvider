package com.arcunis.vaultprovider.economy;

import com.arcunis.vaultprovider.Database;
import com.arcunis.vaultprovider.Main;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

import java.io.File;
import java.io.IOException;

public class VPEconomy {

    public void onEnable(Main plugin) {
        // Create files
        try {
            File dbFile = new File(plugin.getDataFolder(), "economy.db");
            if (!dbFile.exists()) dbFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Create db tables
        Database db = new Database();
        db.createTables(plugin);

        try {
            Class.forName("net.milkbowl.vault.economy.Economy");
            plugin.getServer().getServicesManager().register(net.milkbowl.vault.economy.Economy.class, Main.econ, plugin, ServicePriority.Normal);
        } catch (ClassNotFoundException e) {
            Main.logger.warning("Could not register economy provider. Could not find Vault. Disabling...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            throw new RuntimeException(e);
        }

        // Register events
        Bukkit.getPluginManager().registerEvents(new Events(plugin), plugin);

        // Register command
        new EconomyCommand(plugin);
    }

}
