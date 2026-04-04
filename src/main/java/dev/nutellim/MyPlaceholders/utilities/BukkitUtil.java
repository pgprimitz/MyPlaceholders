package dev.nutellim.MyPlaceholders.utilities;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@UtilityClass
public class BukkitUtil {

    public Player getPlayerByArguments(String[] args) {
        for (String arg : args) {
            Player player = Bukkit.getPlayer(arg);

            if (player != null) {
                return player;
            }
        }
        return null;
    }
}
