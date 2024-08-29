package com.arcunis.vaultprovider;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Events implements Listener {

    private final Main plugin;

    public Events(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasPlayedBefore()) return;
        Main.logger.info("Created acc for new player %s".formatted(event.getPlayer().getName()));
        EconomyManager.createAcc(event.getPlayer().getUniqueId());
        EconomyManager.depositAcc(event.getPlayer().getUniqueId(), plugin.getConfig().getInt("initial-acc-bal:"));
    }

}
