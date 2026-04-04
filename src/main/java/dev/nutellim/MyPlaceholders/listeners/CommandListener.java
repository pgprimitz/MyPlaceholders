package dev.nutellim.MyPlaceholders.listeners;

import dev.nutellim.MyPlaceholders.utilities.BukkitUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class CommandListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerCommand(ServerCommandEvent event) {
        executePlaceholderCommand(event.getCommand(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        executePlaceholderCommand(event.getMessage().substring(1), event);
    }

    private void executePlaceholderCommand(String command, Cancellable event) {
        if (!command.contains("%myplaceholder_")
                && !command.contains("%mp_")
                && !command.contains("%mypl_")) return;

        if (command.startsWith("papi") || command.startsWith("placeholderapi")) return;

        if (command.startsWith("mp ") || command.startsWith("myplaceholder ")
                || command.startsWith("mypl ")) return;

        Player player = BukkitUtil.getPlayerByArguments(command.split(" "));
        if (player == null) return;

        event.setCancelled(true);

        command = PlaceholderAPI.setPlaceholders(player, command);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
