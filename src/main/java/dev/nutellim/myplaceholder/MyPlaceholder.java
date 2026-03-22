package dev.nutellim.myplaceholder;

import dev.nutellim.myplaceholder.commands.MyPlaceholderCommand;
import dev.nutellim.myplaceholder.controllers.PlaceholderController;
import dev.nutellim.myplaceholder.listeners.CommandListener;
import dev.nutellim.myplaceholder.integrations.PlaceholderAPIHook;
import dev.nutellim.myplaceholder.listeners.PlayerListener;
import dev.nutellim.myplaceholder.utilities.FileConfig;
import dev.nutellim.myplaceholder.utilities.LangManager;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Random;

import org.bstats.bukkit.Metrics;

@Getter
public class MyPlaceholder extends JavaPlugin {

    private Random random;
    private BukkitAudiences adventure;
    private FileConfig mainConfig;
    private LangManager lang;
    private PlaceholderController placeholderController;

    @Override
    public void onEnable() {
        this.random = new Random();
        this.adventure = BukkitAudiences.create(this);
        this.mainConfig = new FileConfig(this, "config.yml");
        this.lang = new LangManager(this);

        new FileConfig(this, "placeholders/example.yml");

        this.placeholderController = new PlaceholderController(this);

        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new CommandListener(), this);
        pluginManager.registerEvents(new PlayerListener(), this);

        MyPlaceholderCommand cmd = new MyPlaceholderCommand(this);
        Objects.requireNonNull(this.getCommand("myplaceholder")).setExecutor(cmd);
        Objects.requireNonNull(this.getCommand("myplaceholder")).setTabCompleter(cmd);

        PlaceholderAPIHook.initialize(this);

        // bStats 21/03/2026
        int pluginId = 30356;
        Metrics metrics = new Metrics(this, pluginId);
    }


    @Override
    public void onDisable() {
        if (placeholderController != null) {
            placeholderController.getAnimationTickManager().stop();
        }
        if (adventure != null) {
            adventure.close();
        }
    }

    public void onReload() {
        this.mainConfig.reload();
        this.lang.reload();
        this.placeholderController.onReload();
        PlaceholderAPIHook.initialize(this);
    }

    public boolean isDebug() {
        return mainConfig.getBoolean("debug");
    }

    public void debug(String message) {
        if (isDebug()) getLogger().info("[DEBUG] " + message);
    }
}
