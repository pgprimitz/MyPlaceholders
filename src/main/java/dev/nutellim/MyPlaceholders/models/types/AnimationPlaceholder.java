package dev.nutellim.MyPlaceholders.models.types;

import dev.nutellim.MyPlaceholders.models.Placeholder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.List;

public class AnimationPlaceholder extends Placeholder {

    public enum AnimationMode {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
        BOUNCE,
        FULL_CYCLE,
        WAVE;

        public static AnimationMode find(String s) {
            if (s == null) return LEFT_TO_RIGHT;
            try {
                return AnimationMode.valueOf(s.toUpperCase().replace("-", "_").replace(" ", "_"));
            } catch (Exception e) {
                return LEFT_TO_RIGHT;
            }
        }
    }

    private final Color[] colors;
    private final int speed;
    private final int windowSize;
    private final AnimationMode mode;
    private final String plainText;
    private final int totalFrames;
    private final String decorationCodes;

    private volatile String currentFrame = "";
    private volatile int pos = 0;
    private volatile int bounceDir = 1;

    public AnimationPlaceholder(String id, ConfigurationSection section) {
        super(id, section);

        List<String> colorList = section.getStringList("colors");
        if (colorList.size() < 2) {
            this.colors = new Color[]{ Color.WHITE, Color.GRAY };
        } else {
            Color[] parsed = new Color[colorList.size()];
            for (int i = 0; i < colorList.size(); i++) {
                String raw = colorList.get(i).trim().replace("&", "");
                if (!raw.startsWith("#")) raw = "#" + raw;
                try {
                    parsed[i] = Color.decode(raw);
                } catch (NumberFormatException e) {
                    parsed[i] = Color.WHITE;
                }
            }
            this.colors = parsed;
        }

        this.speed  = Math.max(1, section.getInt("speed", 2));
        this.mode   = AnimationMode.find(section.getString("animation-mode", "LEFT_TO_RIGHT"));

        String rawValue   = (this.value != null) ? this.value : "";
        this.plainText    = stripColorCodes(rawValue);
        int textLen       = plainText.length();
        int defaultWindow = Math.max(1, textLen / 2);
        this.windowSize   = Math.max(1, section.getInt("window", defaultWindow));
        this.totalFrames  = computeTotalFrames();

        StringBuilder dec = new StringBuilder();
        if (bold)          dec.append("§l");
        if (italic)        dec.append("§o");
        if (underline)     dec.append("§n");
        if (strikethrough) dec.append("§m");
        if (obfuscate)     dec.append("§k");
        this.decorationCodes = dec.toString();

        updateFrame();
    }

    public void tick(long globalTick) {
        if (globalTick % speed != 0) return;
        advancePosition();
        updateFrame();
    }

    @Override
    public String process(Player player) {
        return decorationCodes.isEmpty()
                ? currentFrame
                : currentFrame + "§r";
    }



    private void advancePosition() {
        switch (mode) {
            case LEFT_TO_RIGHT:
            case RIGHT_TO_LEFT:
            case FULL_CYCLE:
            case WAVE:
                pos = (pos + 1) % totalFrames;
                break;
            case BOUNCE:
                pos += bounceDir;
                if (pos >= totalFrames - 1) { pos = totalFrames - 1; bounceDir = -1; }
                else if (pos <= 0)          { pos = 0;               bounceDir =  1; }
                break;
        }
    }

    private int computeTotalFrames() {
        switch (mode) {
            case FULL_CYCLE:
                return Math.max(1, colors.length * 10);
            default:
                return Math.max(1, plainText.length() + windowSize);
        }
    }

    private void updateFrame() {
        if (plainText.isEmpty()) { currentFrame = ""; return; }
        switch (mode) {
            case LEFT_TO_RIGHT: currentFrame = buildSlidingFrame(false); break;
            case RIGHT_TO_LEFT: currentFrame = buildSlidingFrame(true);  break;
            case BOUNCE:        currentFrame = buildSlidingFrame(false); break;
            case FULL_CYCLE:    currentFrame = buildFullCycleFrame();    break;
            case WAVE:          currentFrame = buildWaveFrame();         break;
            default:            currentFrame = plainText;
        }
    }

    private String buildSlidingFrame(boolean reversed) {
        int len = plainText.length();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < len; i++) {
            char c = plainText.charAt(i);

            int localPos;
            if (reversed) {
                int mirroredI = len - 1 - i;
                int windowStart = pos - windowSize;
                localPos = mirroredI - windowStart;
            } else {
                int windowStart = pos - windowSize;
                localPos = i - windowStart;
            }

            Color charColor;
            if (localPos < 0 || localPos >= windowSize) {
               float staticRatio = len <= 1 ? 0f : (float) i / (len - 1);
                charColor = interpolateMulti(colors, staticRatio);
            } else {
                float ratio = windowSize <= 1 ? 0f : (float) localPos / (windowSize - 1);
                charColor = interpolateMulti(colors, ratio);
            }

            sb.append(ChatColor.of(colorToHex(charColor)));
            sb.append(decorationCodes);
            sb.append(c);
        }
        return sb.toString();
    }

    private String buildFullCycleFrame() {
        float ratio = (float) pos / totalFrames;
        return ChatColor.of(colorToHex(interpolateMulti(colors, ratio)))
                + decorationCodes
                + plainText;
    }

    private String buildWaveFrame() {
        int len = plainText.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int effectivePos = (i + pos) % totalFrames;
            float ratio = (float) effectivePos / totalFrames;
            sb.append(ChatColor.of(colorToHex(interpolateMulti(colors, ratio))));
            sb.append(decorationCodes);
            sb.append(plainText.charAt(i));
        }
        return sb.toString();
    }

    private Color interpolateMulti(Color[] stops, float ratio) {
        if (stops.length == 1) return stops[0];
        ratio = Math.max(0f, Math.min(1f, ratio));
        float scaledPos  = ratio * (stops.length - 1);
        int segment      = Math.min((int) scaledPos, stops.length - 2);
        float localRatio = scaledPos - segment;
        Color s = stops[segment], e = stops[segment + 1];
        return new Color(
            clamp((int)(s.getRed()   + localRatio * (e.getRed()   - s.getRed()))),
            clamp((int)(s.getGreen() + localRatio * (e.getGreen() - s.getGreen()))),
            clamp((int)(s.getBlue()  + localRatio * (e.getBlue()  - s.getBlue())))
        );
    }

    private int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    private String colorToHex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    private String stripColorCodes(String text) {
        if (text == null) return "";
        text = text.replaceAll("§x(§[A-Fa-f0-9]){6}", "");
        text = text.replaceAll("§[0-9A-Fa-fK-ORk-or]", "");
        text = text.replaceAll("&#[A-Fa-f0-9]{6}", "");
        text = text.replaceAll("&[0-9A-Fa-fK-ORk-or]", "");
        text = text.replaceAll("<[^>]+>", "");
        return text;
    }
}
