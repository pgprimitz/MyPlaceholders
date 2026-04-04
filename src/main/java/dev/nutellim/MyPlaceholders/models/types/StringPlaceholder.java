package dev.nutellim.MyPlaceholders.models.types;

import dev.nutellim.MyPlaceholders.models.Placeholder;
import org.bukkit.configuration.ConfigurationSection;

public class StringPlaceholder extends Placeholder {
    public StringPlaceholder(String id, ConfigurationSection section) {
        super(id, section);
    }
}
