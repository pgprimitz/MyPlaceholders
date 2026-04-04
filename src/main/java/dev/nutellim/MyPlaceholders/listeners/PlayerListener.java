package dev.nutellim.MyPlaceholders.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        event.setReason(PlaceholderAPI.setPlaceholders(null, event.getReason()));
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        event.setKickMessage(PlaceholderAPI.setPlaceholders(null, event.getKickMessage()));
    }
}
