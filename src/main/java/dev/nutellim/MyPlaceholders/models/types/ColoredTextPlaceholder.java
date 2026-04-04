package dev.nutellim.MyPlaceholders.models.types;

import dev.nutellim.MyPlaceholders.models.Placeholder;
import dev.nutellim.MyPlaceholders.utilities.ChatUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ColoredTextPlaceholder extends Placeholder {

    public ColoredTextPlaceholder(String id, ConfigurationSection section) {
        super(id, section);
    }

    @Override
    public String process(Player player) {
        String resolved = PlaceholderAPI.setPlaceholders(player, this.value);
        String decorations = buildDecorationCodes();

        String withGradients = ChatUtil.applyGradientLegacy(resolved, decorations);

        String colored = ChatUtil.toLegacyColoredText(withGradients);

        if (decorations.isEmpty()) return colored;

        String result = injectDecorationsAfterColorCodes(colored, decorations);
        return result + "§r";
    }

    private String injectDecorationsAfterColorCodes(String text, String decorations) {
        if (text == null || text.isEmpty() || decorations.isEmpty()) return text;

        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);

            if (c == '§' && i + 1 < text.length()) {
                char next = text.charAt(i + 1);

                if (next == 'x' && i + 13 < text.length()) {
                    sb.append(text, i, i + 14);
                    sb.append(decorations);
                    i += 14;
                } else if ("0123456789abcdefABCDEF".indexOf(next) >= 0) {
                    sb.append(c).append(next);
                    sb.append(decorations);
                    i += 2;
                } else {
                    sb.append(c).append(next);
                    i += 2;
                }
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    private String buildDecorationCodes() {
        StringBuilder sb = new StringBuilder();
        if (bold)          sb.append("§l");
        if (italic)        sb.append("§o");
        if (underline)     sb.append("§n");
        if (strikethrough) sb.append("§m");
        if (obfuscate)     sb.append("§k");
        return sb.toString();
    }
}
