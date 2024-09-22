package com.arcunis.vaultprovider.economy;

import com.arcunis.vaultprovider.Database;
import com.arcunis.vaultprovider.Main;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.eclipse.sisu.bean.LifecycleManager;

import java.io.File;
import java.io.IOException;

public class VPEconomy {

    public void onEnable(Main plugin) {
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

        LifecycleEventManager<Plugin> pluginLifecycleManager = plugin.getLifecycleManager();
        pluginLifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            new EconomyBrigadierCommand(commands);
        });
    }

}
