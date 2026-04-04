package dev.nutellim.MyPlaceholders.models.types.requirement;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.util.function.BiFunction;

@Getter
public class RequirementPlaceholderRequirement {

    private final String id;
    private final String deny;
    private final String type;
    private final String input;
    private final String output;

    public RequirementPlaceholderRequirement(String id, ConfigurationSection section) {
        this.id     = id;
        this.deny   = section.getString("deny",   "");
        this.type   = section.getString("type",   "");
        this.input  = section.getString("input",  "");
        this.output = section.getString("output", "");
    }

    public String checkRequirement(Player player, RequirementPlaceholder requirementPlaceholder) {
        String parsedInput = PlaceholderAPI.setPlaceholders(player, input);

        BiFunction<String, String, Boolean> stringComparison =
                requirementPlaceholder.getStringComparison(type);
        if (stringComparison != null) {
            return stringComparison.apply(parsedInput, output)
                    ? requirementPlaceholder.getValue() : deny;
        }

        BiFunction<BigInteger, BigInteger, Boolean> numberComparison =
                requirementPlaceholder.getNumberComparison(type);
        if (numberComparison != null) {
            try {
                BigInteger inputInt  = parseToBigInteger(parsedInput);
                BigInteger outputInt = parseToBigInteger(output);
                return numberComparison.apply(inputInt, outputInt)
                        ? requirementPlaceholder.getValue() : deny;
            } catch (Exception e) {
                return deny;
            }
        }

        return "Invalid requirement type '" + type + "'";
    }

    private BigInteger parseToBigInteger(String s) {
        s = s.trim().replace(",", "");
        if (s.contains(".")) {
            s = s.substring(0, s.indexOf('.'));
        }
        if (s.isEmpty()) s = "0";
        return new BigInteger(s);
    }
}
