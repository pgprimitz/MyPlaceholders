package dev.nutellim.MyPlaceholders.utilities;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.audience.Audience;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ChatUtil {

    private final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final Pattern LEGACY_HEX_PATTERN = Pattern.compile("§x(§[A-Fa-f0-9]){6}");
    private final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("(?i)([&§])([0-9A-FK-ORX])");
    private final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");

    private final Pattern MULTI_GRADIENT_PATTERN = Pattern.compile(
            "((?:(?:&#|#)[A-Fa-f0-9]{6}:){1,}(?:&#|#)[A-Fa-f0-9]{6})\\s+([^\\n]+)"
    );

    private final Pattern HEX_COLOR_EXTRACT = Pattern.compile("(?:&#|#)([A-Fa-f0-9]{6})");

    public String NORMAL_LINE = "<gray><st>-----------------------------------------</st></gray>";

    public Component toComponent(String text) {
        return MINI_MESSAGE.deserialize(toMiniMessage(text));
    }

    public Component toComponentPrefix(CommandSender sender, String text, String prefix) {
        return toComponent(replacePlaceholders(sender, prefix + text));
    }

    public String toLegacyColoredText(String text) {
        text = text.replaceAll("&([0-9a-fk-or])", "§$1");

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append("§").append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public String toMiniMessage(String text) {
        Matcher legacyHexMatcher = LEGACY_HEX_PATTERN.matcher(text);
        StringBuffer legacyHexBuilder = new StringBuffer();
        while (legacyHexMatcher.find()) {
            String hexColor = legacyHexMatcher.group()
                    .replace("§x", "")
                    .replace("§", "");
            legacyHexMatcher.appendReplacement(legacyHexBuilder, "<#" + hexColor + ">");
        }
        legacyHexMatcher.appendTail(legacyHexBuilder);
        text = legacyHexBuilder.toString();

        text = text.replaceAll("&#([A-Fa-f0-9]{6})", "<#$1>");
        text = text.replaceAll("\\{#([A-Fa-f0-9]{6})}", "<#$1>");

        Matcher legacyColorMatcher = LEGACY_COLOR_PATTERN.matcher(text);
        StringBuffer legacyColorBuilder = new StringBuffer();
        while (legacyColorMatcher.find()) {
            String colorCode = legacyColorMatcher.group(2).toLowerCase();
            String replacement = getMiniMessageTag(colorCode);
            legacyColorMatcher.appendReplacement(legacyColorBuilder, replacement);
        }
        legacyColorMatcher.appendTail(legacyColorBuilder);
        return legacyColorBuilder.toString();
    }

    public String applyGradientLegacy(String text) {
        return applyGradientLegacy(text, "");
    }

    public String applyGradientLegacy(String text, String decorations) {
        if (text == null) return "";

        text = applyGradientFromLegacyCodes(text, decorations);

        Matcher m = MULTI_GRADIENT_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String colorsPart = m.group(1);
            String content    = m.group(2);

            Matcher hexMatcher = HEX_COLOR_EXTRACT.matcher(colorsPart);
            java.util.List<Color> colorList = new java.util.ArrayList<>();
            while (hexMatcher.find()) {
                try {
                    colorList.add(Color.decode("#" + hexMatcher.group(1)));
                } catch (NumberFormatException ignored) {}
            }

            if (colorList.size() < 2) {
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0)));
                continue;
            }

            String gradient = generateMultiGradient(content, colorList.toArray(new Color[0]), decorations);
            m.appendReplacement(sb, Matcher.quoteReplacement(gradient));
        }

        m.appendTail(sb);
        return sb.toString();
    }

    private String applyGradientFromLegacyCodes(String text, String decorations) {
        Pattern p = Pattern.compile("(§x(?:§[A-Fa-f0-9]){6}):(§x(?:§[A-Fa-f0-9]){6})\\s+([^\\n]+)");
        Matcher m = p.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            Color c1 = legacyCodeToColor(m.group(1));
            Color c2 = legacyCodeToColor(m.group(2));
            String content = m.group(3);

            if (c1 == null || c2 == null) {
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0)));
                continue;
            }

            String gradient = generateMultiGradient(content, new Color[]{c1, c2}, decorations);
            m.appendReplacement(sb, Matcher.quoteReplacement(gradient));
        }

        m.appendTail(sb);
        return sb.toString();
    }

    private Color legacyCodeToColor(String code) {
        try {
            String hex = code.replace("§x", "").replace("§", "");
            return Color.decode("#" + hex);
        } catch (Exception e) {
            return null;
        }
    }

    public String generateMultiGradient(String text, Color[] stops, String decorations) {
        if (text.isEmpty()) return text;
        if (stops.length == 1) return ChatColor.of(
                String.format("#%02X%02X%02X", stops[0].getRed(), stops[0].getGreen(), stops[0].getBlue()))
                + decorations + text;

        StringBuilder result = new StringBuilder();
        int length = text.length();
        int segments = stops.length - 1;

        for (int i = 0; i < length; i++) {
            float globalRatio = length == 1 ? 0f : (float) i / (length - 1);
            float scaledPos   = globalRatio * segments;
            int segment       = Math.min((int) scaledPos, segments - 1);
            float localRatio  = scaledPos - segment;

            Color s = stops[segment], e = stops[segment + 1];
            int r = clamp((int)(s.getRed()   + localRatio * (e.getRed()   - s.getRed())));
            int g = clamp((int)(s.getGreen() + localRatio * (e.getGreen() - s.getGreen())));
            int b = clamp((int)(s.getBlue()  + localRatio * (e.getBlue()  - s.getBlue())));

            result.append(ChatColor.of(String.format("#%02X%02X%02X", r, g, b)));
            result.append(decorations);
            result.append(text.charAt(i));
        }
        return result.toString();
    }

    public String generateMultiGradient(String text, Color[] stops) {
        return generateMultiGradient(text, stops, "");
    }

    public String generateGradient(String text, Color start, Color end) {
        return generateMultiGradient(text, new Color[]{start, end}, "");
    }

    public String applyGradient(String input) {
        Pattern gradientPattern = Pattern.compile("<#([A-Fa-f0-9]{6})>:<#([A-Fa-f0-9]{6})>([^<>]+)");
        Matcher matcher = gradientPattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String gradient = generateGradient(matcher.group(3),
                    Color.decode("#" + matcher.group(1)),
                    Color.decode("#" + matcher.group(2)));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(gradient));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    private int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    public void sendMessage(Audience audience, String text) {
        audience.sendMessage(toComponent(text));
    }

    public void sendMessagePrefix(Audience audience, CommandSender sender, String text, FileConfig configFile) {
        audience.sendMessage(toComponentPrefix(sender, text, configFile.getString("command-messages.prefix")));
    }

    public String replacePlaceholders(CommandSender sender, String text) {
        return sender instanceof Player ?
                PlaceholderAPI.setPlaceholders((Player) sender, text) :
                text;
    }

    private String getMiniMessageTag(String colorCode) {
        switch (colorCode) {
            case "0": return "<black>";
            case "1": return "<dark_blue>";
            case "2": return "<dark_green>";
            case "3": return "<dark_aqua>";
            case "4": return "<dark_red>";
            case "5": return "<dark_purple>";
            case "6": return "<gold>";
            case "7": return "<gray>";
            case "8": return "<dark_gray>";
            case "9": return "<blue>";
            case "a": return "<green>";
            case "b": return "<aqua>";
            case "c": return "<red>";
            case "d": return "<light_purple>";
            case "e": return "<yellow>";
            case "f": return "<white>";
            case "k": return "<obfuscated>";
            case "l": return "<bold>";
            case "m": return "<strikethrough>";
            case "n": return "<underlined>";
            case "o": return "<italic>";
            case "r": return "<reset>";
            default:  return "";
        }
    }
}
