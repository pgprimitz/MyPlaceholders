package dev.nutellim.MyPlaceholders.models;

import dev.nutellim.MyPlaceholders.MyPlaceholder;
import dev.nutellim.MyPlaceholders.utilities.FileConfig;
import lombok.Getter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Getter
public class PlaceholderCategory {

    private final String id;
    private final FileConfig fileConfig;
    private final Map<String, Placeholder> placeholders;

    public PlaceholderCategory(MyPlaceholder plugin, String id, File file) {
        this.id = id;
        this.fileConfig = new FileConfig(plugin, file);
        this.placeholders = new HashMap<>();
    }

    public Placeholder getPlaceholder(String id) {
        return placeholders.get(id);
    }

    public void savePlaceholder(String placeholder, String value) {
        fileConfig.set("placeholders." + placeholder + ".value", value);
        fileConfig.save();
    }
}
