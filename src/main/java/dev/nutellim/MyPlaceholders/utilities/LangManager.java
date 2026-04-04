package dev.nutellim.MyPlaceholders.utilities;

import dev.nutellim.MyPlaceholders.MyPlaceholder;

public class LangManager {

    private final MyPlaceholder plugin;
    private FileConfig langFile;

    public LangManager(MyPlaceholder plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        for (String langFile : new String[]{"en_US", "es_ES"}) {
            new FileConfig(plugin, "lang/" + langFile + ".yml");
        }

        String lang = plugin.getMainConfig().getRawString("lang");
        if (lang == null || lang.isEmpty()) lang = "en_US";
        langFile = new FileConfig(plugin, "lang/" + lang + ".yml");
    }

    public String get(String path) {
        String value = langFile.getRawString(path);
        return value != null ? value : "§c[Missing lang key: " + path + "]";
    }

    public String getPrefix() {
        return get("prefix");
    }
}
