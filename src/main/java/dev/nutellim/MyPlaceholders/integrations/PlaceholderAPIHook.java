package dev.nutellim.MyPlaceholders.integrations;

import dev.nutellim.MyPlaceholders.MyPlaceholder;
import dev.nutellim.MyPlaceholders.controllers.PlaceholderController;
import dev.nutellim.MyPlaceholders.models.Placeholder;
import dev.nutellim.MyPlaceholders.models.types.MathPlaceholder;
import dev.nutellim.MyPlaceholders.models.types.ProgressPlaceholder;
import dev.nutellim.MyPlaceholders.integrations.types.MPExpansion;
import dev.nutellim.MyPlaceholders.integrations.types.MyPlExpansion;
import dev.nutellim.MyPlaceholders.integrations.types.MyPlaceholderExpansion;
import dev.nutellim.MyPlaceholders.utilities.TextConverter;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@UtilityClass
public class PlaceholderAPIHook {

    public void initialize(MyPlaceholder plugin) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceholderExpansion[] expansions = {
                    new MyPlaceholderExpansion(plugin),
                    new MyPlExpansion(plugin),
                    new MPExpansion(plugin)
            };
            for (PlaceholderExpansion expansion : expansions) {
                if (!expansion.isRegistered()) expansion.register();
            }
        }
    }

    public String getPlaceholderValue(
            Player player,
            String identifier,
            String alias,
            PlaceholderController placeholderController) {

        int underscoreIdx = identifier.indexOf('_');
        String placeholderId;
        String argsPart;

        if (underscoreIdx >= 0) {
            placeholderId = identifier.substring(0, underscoreIdx);
            argsPart      = identifier.substring(underscoreIdx + 1);
        } else {
            placeholderId = identifier;
            argsPart      = "";
        }

        switch (placeholderId.toLowerCase()) {
            case "roman":
                return TextConverter.toRoman(argsPart.replace("_", " ").trim());
            case "smallcaps":
                return TextConverter.toSmallCaps(argsPart.replace("_", " ").trim());
            case "superscript":
                return TextConverter.toSuperscript(argsPart.replace("_", " ").trim());
            case "circled":
                return TextConverter.toCircled(argsPart.replace("_", " ").trim());
            case "fullwidth":
                return TextConverter.toFullwidth(argsPart.replace("_", " ").trim());
        }

        Placeholder placeholder = placeholderController.getPlaceholder(placeholderId);
        if (placeholder == null) return null;

        if (placeholder instanceof MathPlaceholder) {
            MathPlaceholder math = (MathPlaceholder) placeholder;
            String[] mathArgs = argsPart.isEmpty() ? new String[0] : argsPart.split("::");
            math.setProcessedValue(injectArguments(math.getValue(), mathArgs));
            return math.process(player);
        }

        if (placeholder instanceof ProgressPlaceholder && !argsPart.isEmpty()) {
            int sepIdx = argsPart.indexOf('_');
            if (sepIdx >= 0) {
                String currentStr = argsPart.substring(0, sepIdx);
                String maxStr     = argsPart.substring(sepIdx + 1);
                return ((ProgressPlaceholder) placeholder).processWithValues(player, currentStr, maxStr);
            }
        }

        if (!argsPart.isEmpty()) {
            String[] args = argsPart.split("::");
            String originalValue = placeholder.getValue();
            placeholder.setValue(injectArguments(originalValue, args));
            String result = placeholder.process(player);
            placeholder.setValue(originalValue);
            return result;
        }

        return placeholder.process(player);
    }

    private String injectArguments(String text, String[] args) {
        if (args == null || args.length == 0 || text == null) return text;
        for (int i = 0; i < args.length; i++) {
            text = text.replace("{" + i + "}", args[i]);
        }
        return text;
    }
}
