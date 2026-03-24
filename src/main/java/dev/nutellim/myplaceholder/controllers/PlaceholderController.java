package dev.nutellim.myplaceholder.controllers;

import dev.nutellim.myplaceholder.MyPlaceholder;
import dev.nutellim.myplaceholder.models.Placeholder;
import dev.nutellim.myplaceholder.models.PlaceholderCategory;
import dev.nutellim.myplaceholder.models.PlaceholderType;
import dev.nutellim.myplaceholder.models.types.*;
import dev.nutellim.myplaceholder.models.types.*;
import dev.nutellim.myplaceholder.models.types.requirement.RequirementPlaceholder;
import dev.nutellim.myplaceholder.utilities.ChatUtil;
import dev.nutellim.myplaceholder.utilities.FileConfig;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class PlaceholderController {

    private final MyPlaceholder plugin;
    @Getter private final Map<String, PlaceholderCategory> placeholderCategories;
    @Getter private final AnimationTickManager animationTickManager;

    private final Map<String, String> seenIds = new HashMap<>();
    private final List<String> errorLog = new ArrayList<>();
    private long lastNotifyTime = 0;
    private static final long NOTIFY_COOLDOWN_MS = 2000;

    public PlaceholderController(MyPlaceholder plugin) {
        this.plugin = plugin;
        this.placeholderCategories = new HashMap<>();
        this.animationTickManager = new AnimationTickManager(plugin);
        this.animationTickManager.start();
        this.onReload();
    }

    public Placeholder getPlaceholder(String id) {
        return placeholderCategories.values().stream()
                .flatMap(category -> category.getPlaceholders().values().stream())
                .filter(placeholder -> placeholder.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    public PlaceholderCategory getPlaceholderCategory(String id) {
        return placeholderCategories.get(id);
    }

    public List<String> getErrorLog() {
        return Collections.unmodifiableList(errorLog);
    }

    public void onLoad(String id, File file) {
        PlaceholderCategory cat = placeholderCategories.computeIfAbsent(
                id, k -> new PlaceholderCategory(plugin, id, file));
        FileConfig fileConfig = cat.getFileConfig();

        ConfigurationSection placeholdersSection = fileConfig.getConfigurationSection("placeholders");
        if (placeholdersSection == null) {
            addError("Section 'placeholders' not found in " + id);
            return;
        }

        for (String placeholderId : placeholdersSection.getKeys(false)) {
            ConfigurationSection placeholderSection = placeholdersSection.getConfigurationSection(placeholderId);
            if (placeholderSection == null) {
                addError("'" + placeholderId + "' in '" + id + "' is a plain value, not a section block. "
                       + "Make sure it has at least 'value:' underneath it.");
                continue;
            }

            String lower = placeholderId.toLowerCase();
            if (seenIds.containsKey(lower)) {
                addError("Duplicate ID '" + placeholderId + "' in '" + id
                       + "' (already defined in '" + seenIds.get(lower) + "')");
            } else {
                seenIds.put(lower, id);
            }

            try {
                Placeholder placeholder = createPlaceholder(placeholderId, placeholderSection);
                cat.getPlaceholders().put(placeholderId, placeholder);
            } catch (Exception e) {
                addError("Error loading '" + placeholderId + "' in '" + id + "': " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + cat.getPlaceholders().size() + " placeholders from " + id);
    }

    private void addError(String msg) {
        errorLog.add(msg);
        plugin.getLogger().warning("[MyPlaceholder] " + msg);
    }

    private Placeholder createPlaceholder(String placeholderId, ConfigurationSection section) {
        PlaceholderType type = PlaceholderType.find(section.getString("type"));
        switch (type) {
            case STRING:       return new StringPlaceholder(placeholderId, section);
            case COLORED_TEXT: return new ColoredTextPlaceholder(placeholderId, section);
            case RANDOM:       return new RandomPlaceholder(plugin, placeholderId, section);
            case MATH:         return new MathPlaceholder(placeholderId, section);
            case REQUIREMENT:  return new RequirementPlaceholder(placeholderId, section);
            case PROGRESS:     return new ProgressPlaceholder(placeholderId, section);
            case ANIMATION: {
                AnimationPlaceholder anim = new AnimationPlaceholder(placeholderId, section);
                animationTickManager.register(anim);
                return anim;
            }
            default: throw new RuntimeException("Unknown type: " + type);
        }
    }

    private void onLoadFiles(File folder, File root) {
        File[] files = folder.listFiles();
        if (files == null) return;

        Arrays.sort(files, (a, b) -> {
            if (a.isDirectory() == b.isDirectory()) return a.getName().compareTo(b.getName());
            return a.isDirectory() ? 1 : -1;
        });

        for (File file : files) {
            if (file.isDirectory()) {
                onLoadFiles(file, root);
            } else if (file.getName().endsWith(".yml")) {
                String relativePath = root.toURI().relativize(file.toURI()).getPath();
                String categoryId = relativePath.replace(".yml", "").replace("\\", "/");
                if (!isFileEnabled(file)) {
                    plugin.getLogger().info("[MyPlaceholder] Skipping disabled: " + categoryId);
                    continue;
                }
                onLoad(categoryId, file);
            }
        }
    }

    public void setFileEnabled(File file, boolean enabled) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("enabled", enabled);
        try {
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save enabled state for " + file.getName());
        }
    }

    public File getPlaceholderFile(String categoryId) {
        File root = new File(plugin.getDataFolder(), "placeholders");
        return new File(root, categoryId + ".yml");
    }

    private boolean isFileEnabled(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.contains("enabled")) return true;
        return config.getBoolean("enabled", true);
    }

    public void onReload() {
        animationTickManager.unregisterAll();
        placeholderCategories.clear();
        seenIds.clear();
        errorLog.clear();

        File placeholdersFolder = new File(plugin.getDataFolder(), "placeholders");
        if (!placeholdersFolder.exists()) {
            placeholdersFolder.mkdirs();
            plugin.getLogger().warning("Placeholders folder was missing — created a new one.");
        }

        onLoadFiles(placeholdersFolder, placeholdersFolder);
        notifyAdminsOfErrors();
    }

    private void notifyAdminsOfErrors() {
        if (errorLog.isEmpty()) return;

        long now = System.currentTimeMillis();
        if (now - lastNotifyTime < NOTIFY_COOLDOWN_MS) return;
        lastNotifyTime = now;

        Bukkit.getScheduler().runTask(plugin, () -> {
            String prefix = plugin.getLang().getPrefix();
            String msg = prefix + "&#FF3D00" + errorLog.size()
                    + " error(s) loading placeholders. Use /mp errorlog for details.";
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("myplaceholder.command.myplaceholder"))
                    .forEach(p -> {
                        Audience audience = plugin.getAdventure().player(p);
                        ChatUtil.sendMessage(audience, msg);
                    });
        });
    }
}
