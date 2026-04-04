package dev.nutellim.MyPlaceholders.utilities;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class FileConfig {

    private final JavaPlugin plugin;
    private final File file;
    private FileConfiguration configuration;

    public FileConfig(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            if (plugin.getResource(fileName) == null) {
                try { file.createNewFile(); }
                catch (IOException ex) { plugin.getLogger().severe("Failed to create new file " + fileName); }
            } else {
                plugin.saveResource(fileName, false);
            }
        }
        this.configuration = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfig(JavaPlugin plugin, File file) {
        this.plugin = plugin;
        this.file = file;
        this.configuration = YamlConfiguration.loadConfiguration(file);
    }

    public double  getDouble(String path)  { return configuration.contains(path) ? configuration.getDouble(path)  : 0; }
    public int     getInt(String path)     { return configuration.contains(path) ? configuration.getInt(path)     : 0; }
    public boolean getBoolean(String path) { return configuration.contains(path) ? configuration.getBoolean(path) : false; }
    public long    getLong(String path)    { return configuration.contains(path) ? configuration.getLong(path)    : 0L; }

    public String getString(String path) {
        if (!configuration.contains(path)) return null;
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(configuration.getString(path)));
    }

    public String getRawString(String path) {
        return configuration.contains(path) ? configuration.getString(path) : null;
    }

    public List<String> getStringList(String path) {
        if (!configuration.contains(path))
            return Collections.singletonList("String list not found at path - " + path);
        return configuration.getStringList(path).stream()
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());
    }

    public List<Integer> getIntegerList(String path) {
        if (!configuration.contains(path))
            throw new NullPointerException("Integer list not found at path - " + path);
        return configuration.getStringList(path).stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public ConfigurationSection getConfigurationSection(String path) {
        return configuration.getConfigurationSection(path);
    }

    public void set(String path, Object value) { configuration.set(path, value); }

    public void save() {
        try { configuration.save(file); }
        catch (IOException ex) { plugin.getLogger().severe("Failed to save file " + file.getName()); }
    }

    public void reload() { configuration = YamlConfiguration.loadConfiguration(file); }
}
