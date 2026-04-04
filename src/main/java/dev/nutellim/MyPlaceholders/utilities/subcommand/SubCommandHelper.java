package dev.nutellim.MyPlaceholders.utilities.subcommand;

import dev.nutellim.MyPlaceholders.utilities.ChatUtil;
import dev.nutellim.MyPlaceholders.utilities.MapUtil;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class SubCommandHelper {

    public String build(String template, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            template = template.replace(entry.getKey(), entry.getValue());
        }
        return template;
    }

    public String getSubCommandFormat(String label, Map<String, SubCommand> subCommands, String text, String format) {
        String subcommandsFormatted = subCommands.entrySet().stream()
                .map(entry -> {
                    SubCommand value = entry.getValue();

                    Map<String, String> placeholders = MapUtil.of(
                            "%label%", label,
                            "%subcommand%", entry.getKey() + value.getParametersFormatted(),
                            "%description%", value.getDescription()
                    );

                    return build(format, placeholders);
                })
                .collect(Collectors.joining("\n"));

        return text
                .replace("%subcommands%", subcommandsFormatted)
                .replace("%line%", ChatUtil.NORMAL_LINE);
    }
}
