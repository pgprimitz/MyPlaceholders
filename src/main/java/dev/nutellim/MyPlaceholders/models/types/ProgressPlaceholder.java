package dev.nutellim.MyPlaceholders.models.types;

import dev.nutellim.MyPlaceholders.models.Placeholder;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ProgressPlaceholder extends Placeholder {

    private final int length;
    private final String fillChar;
    private final String emptyChar;
    private final String fillColor;
    private final String emptyColor;
    private final boolean showPercentage;
    private final String maxValueSource;

    public ProgressPlaceholder(String id, ConfigurationSection section) {
        super(id, section);
        this.length         = Math.max(1, section.getInt("length", 20));
        String fc = section.getString("fill-char");
        String ec = section.getString("empty-char");
        String fcol = section.getString("fill-color");
        String ecol = section.getString("empty-color");
        String mv   = section.getString("max-value");
        this.fillChar       = (fc   != null && !fc.isEmpty())   ? fc   : "█";
        this.emptyChar      = (ec   != null && !ec.isEmpty())   ? ec   : "░";
        this.fillColor      = (fcol != null && !fcol.isEmpty()) ? fcol : "&#00FF00";
        this.emptyColor     = (ecol != null && !ecol.isEmpty()) ? ecol : "&#555555";
        this.maxValueSource = (mv   != null && !mv.isEmpty())   ? mv   : "100";
        this.showPercentage = section.getBoolean("show-percentage", false);
    }

    @Override
    public String process(Player player) {
        String currentStr = (value != null) ? PlaceholderAPI.setPlaceholders(player, value) : "0";
        String maxStr     = PlaceholderAPI.setPlaceholders(player, maxValueSource);
        return applyDecorations(buildBar(parseRatio(currentStr, maxStr)));
    }

    public String processWithValues(Player player, String currentStr, String maxStr) {
        return applyDecorations(buildBar(parseRatio(currentStr, maxStr)));
    }

    private double parseRatio(String currentStr, String maxStr) {
        double current, max;
        try { current = Double.parseDouble(currentStr.trim().replace(",", "")); } catch (Exception e) { current = 0; }
        try { max     = Double.parseDouble(maxStr.trim().replace(",", ""));     } catch (Exception e) { max = 100; }
        if (max <= 0) max = 1;
        return Math.max(0, Math.min(1, current / max));
    }

    private String buildBar(double ratio) {
        int filled = (int) Math.round(ratio * length);
        int empty  = length - filled;

        StringBuilder sb = new StringBuilder();
        if (filled > 0) { sb.append(colorToLegacy(fillColor));  for (int i = 0; i < filled; i++) sb.append(fillChar); }
        if (empty  > 0) { sb.append(colorToLegacy(emptyColor)); for (int i = 0; i < empty;  i++) sb.append(emptyChar); }
        if (showPercentage) sb.append(" §f").append((int) Math.round(ratio * 100)).append("%");

        return sb.toString();
    }

    private String colorToLegacy(String color) {
        if (color == null || color.isEmpty()) return "";
        color = color.trim();
        String hex = null;
        if (color.startsWith("&#") && color.length() == 8) hex = color.substring(2);
        else if (color.startsWith("#") && color.length() == 7) hex = color.substring(1);

        if (hex != null) {
            StringBuilder sb = new StringBuilder("§x");
            for (char c : hex.toCharArray()) sb.append("§").append(c);
            return sb.toString();
        }
        return color.replace("&", "§");
    }
}
