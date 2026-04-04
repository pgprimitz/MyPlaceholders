package dev.nutellim.MyPlaceholders.models.types;

import dev.nutellim.MyPlaceholders.MyPlaceholder;
import dev.nutellim.MyPlaceholders.models.Placeholder;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class RandomPlaceholder extends Placeholder {

    private final MyPlaceholder plugin;
    private final List<String> values;

    public RandomPlaceholder(MyPlaceholder plugin, String id, ConfigurationSection section) {
        super(id, section);
        this.plugin = plugin;
        this.values = section.getStringList("values");
    }

    private String getRandomValue() {
        return values.isEmpty() ? "" : values.get(plugin.getRandom().nextInt(values.size()));
    }

    @Override
    public String process(Player player) {
        String processed = PlaceholderAPI.setPlaceholders(player, getRandomValue());
        return applyDecorations(processed);
    }
}

