package com.arcunis.vaultprovider.economy;

import com.arcunis.vaultprovider.Main;
import org.bukkit.entity.Player;
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
        Player player = event.getPlayer();
        if (EconomyManager.hasAcc(player.getUniqueId())) return;
        Main.logger.info("Created acc for player %s".formatted(player.getName()));
        EconomyManager.createAcc(player.getUniqueId());
        EconomyManager.depositAcc(player.getUniqueId(), plugin.getConfig().getDouble("initial-acc-bal"));
    }

}
