package com.arcunis.vaultprovider.events;

import com.arcunis.vaultprovider.EconomyManager;
import com.arcunis.vaultprovider.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnPlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasPlayedBefore()) return;
        Main.logger.info("Created acc for new player %s".formatted(event.getPlayer().getName()));
        EconomyManager.createAcc(event.getPlayer().getUniqueId());
    }

}
