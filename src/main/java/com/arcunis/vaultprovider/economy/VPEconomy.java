package com.arcunis.vaultprovider.economy;

import com.arcunis.vaultprovider.Main;
import com.arcunis.vaultprovider.economy.commands.*;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

public class VPEconomy {

    public void onEnable(Main plugin) {
        try {
            Class.forName("net.milkbowl.vault.economy.Economy");
            plugin.getServer().getServicesManager().register(net.milkbowl.vault.economy.Economy.class, Main.econ, plugin, ServicePriority.Normal);
        } catch (ClassNotFoundException e) {
            Main.logger.warning("Could not register economy provider. REASON: Could not find Vault");
            throw new RuntimeException(e);
        }

        // Register events
        Bukkit.getPluginManager().registerEvents(new Events(plugin), plugin);

        // Register commands
        LifecycleEventManager<Plugin> pluginLifecycleManager = plugin.getLifecycleManager();
        pluginLifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            new EconomyAdminCommand(commands);
            if (plugin.getConfig().getBoolean("economy.commands.balance")) new BalanceCommand(commands);
            if (plugin.getConfig().getBoolean("economy.commands.pay")) new PayCommand(commands);
            if (plugin.getConfig().getBoolean("economy.commands.deposit")) new DepositCommand(commands);
            if (plugin.getConfig().getBoolean("economy.commands.withdraw")) new WithdrawCommand(commands);
        });
    }

}
