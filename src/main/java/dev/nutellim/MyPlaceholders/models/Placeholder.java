package dev.nutellim.MyPlaceholders.models;

import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

@Getter
public abstract class Placeholder {

    protected String id;
    @Setter protected String value;

    protected final boolean bold;
    protected final boolean italic;
    protected final boolean underline;
    protected final boolean strikethrough;
    protected final boolean obfuscate;

    protected final boolean locked;

    public Placeholder(String id, ConfigurationSection section) {
        this.id = id;
        this.value = section.getString("value");

        this.bold          = section.getBoolean("bold",          false);
        this.italic        = section.getBoolean("italic",        false);
        this.underline     = section.getBoolean("underline",     false);
        this.strikethrough = section.getBoolean("strikethrough", false);
        this.obfuscate     = section.getBoolean("obfuscate",     false);
        this.locked        = section.getBoolean("locked",        false);
    }

    protected String applyDecorations(String text) {
        if (!bold && !italic && !underline && !strikethrough && !obfuscate) return text;

        StringBuilder prefix = new StringBuilder();
        if (bold)          prefix.append("§l");
        if (italic)        prefix.append("§o");
        if (underline)     prefix.append("§n");
        if (strikethrough) prefix.append("§m");
        if (obfuscate)     prefix.append("§k");

        return prefix.toString() + text + "§r";
    }

    public String process(Player player) {
        return applyDecorations(PlaceholderAPI.setPlaceholders(player, value));
    }
}
