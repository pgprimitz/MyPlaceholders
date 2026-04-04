package dev.nutellim.MyPlaceholders.integrations.types;

import dev.nutellim.MyPlaceholders.MyPlaceholder;
import dev.nutellim.MyPlaceholders.controllers.PlaceholderController;
import dev.nutellim.MyPlaceholders.integrations.PlaceholderAPIHook;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MyPlaceholderExpansion extends PlaceholderExpansion {

    private final MyPlaceholder plugin;
    private final PlaceholderController placeholderController;

    public MyPlaceholderExpansion(MyPlaceholder plugin) {
        this.plugin = plugin;
        this.placeholderController = plugin.getPlaceholderController();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return "nutellim";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "myplaceholder";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        return PlaceholderAPIHook.getPlaceholderValue(player, identifier, "myplaceholder", placeholderController);
    }
}
