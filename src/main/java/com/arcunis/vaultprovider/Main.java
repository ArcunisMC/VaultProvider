package com.arcunis.vaultprovider;

import com.arcunis.vaultprovider.economy.EconomyProvider;
import com.arcunis.vaultprovider.economy.VPEconomy;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
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
        saveResource("messages.yml", false);

        // Create data folder
        if (!getDataFolder().exists()) getDataFolder().mkdir();

        // Set static variables
        dataPath = getDataPath();
        logger = getLogger();

        // Create db tables
        Database db = new Database();
        db.createTables(this);

        // Initialize economy
        if (getConfig().getBoolean("economy.enabled")) {
            econ = new EconomyProvider(this);
            new VPEconomy().onEnable(this);
        }

        // Initialize permission

        // Initialize chat

    }

    public static @Nullable String getMessage(String message) {
        try {
            YamlConfiguration messages = new YamlConfiguration();
            messages.load(new File(dataPath.toFile(), "messages.yml"));
            return messages.getString(message);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

}
