package com.arcunis.vaultprovider;

import com.arcunis.vaultprovider.commands.*;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
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
        try {
            Class.forName("net.milkbowl.vault.economy.Economy");
            getServer().getServicesManager().register(net.milkbowl.vault.economy.Economy.class, Main.econ, this, ServicePriority.Normal);
        } catch (ClassNotFoundException e) {
            Main.logger.warning("Could not register economy provider. REASON: Could not find Vault");
            throw new RuntimeException(e);
        }

        // Register events
        Bukkit.getPluginManager().registerEvents(new Events(this), this);

        // Register commands
        LifecycleEventManager<Plugin> pluginLifecycleManager = getLifecycleManager();
        pluginLifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            new EconomyAdminCommand(commands);
            if (getConfig().getBoolean("economy.commands.balance")) new BalanceCommand(commands);
            if (getConfig().getBoolean("economy.commands.pay")) new PayCommand(commands);
            if (getConfig().getBoolean("economy.commands.deposit")) new DepositCommand(commands);
            if (getConfig().getBoolean("economy.commands.withdraw")) new WithdrawCommand(commands);
        });

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
