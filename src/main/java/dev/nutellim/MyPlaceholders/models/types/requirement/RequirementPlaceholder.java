package dev.nutellim.MyPlaceholders.models.types.requirement;

import dev.nutellim.MyPlaceholders.models.Placeholder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class RequirementPlaceholder extends Placeholder {

    private final Map<String, RequirementPlaceholderRequirement> requirements;
    private final Map<String, BiFunction<BigInteger, BigInteger, Boolean>> numberOperators;
    private final Map<String, BiFunction<String, String, Boolean>> stringOperators;

    public RequirementPlaceholder(String id, ConfigurationSection section) {
        super(id, section);
        this.requirements = new LinkedHashMap<>();

        ConfigurationSection requirementsSection = section.getConfigurationSection("requirements");

        if (requirementsSection != null) {
            for (String requirementId : requirementsSection.getKeys(false)) {
                ConfigurationSection requirementSection = requirementsSection.getConfigurationSection(requirementId);
                if (requirementSection == null) throw new NullPointerException("Section requirements." + requirementId + " not found");

                requirements.put(requirementId, new RequirementPlaceholderRequirement(requirementId, requirementSection));
            }
        }

        this.numberOperators = new HashMap<>();
        this.numberOperators.put(">=", (inputValue, requiredValue) -> inputValue.compareTo(requiredValue) >= 0);
        this.numberOperators.put("<=", (inputValue, requiredValue) -> inputValue.compareTo(requiredValue) <= 0);
        this.numberOperators.put("=", Objects::equals);
        this.numberOperators.put("!=", (inputValue, requiredValue) -> !Objects.equals(inputValue, requiredValue));
        this.numberOperators.put(">", (inputValue, requiredValue) -> inputValue.compareTo(requiredValue) > 0);
        this.numberOperators.put("<", (inputValue, requiredValue) -> inputValue.compareTo(requiredValue) < 0);

        this.stringOperators = new HashMap<>();
        this.stringOperators.put("string equals", Objects::equals);
        this.stringOperators.put("string equals ignorecase", String::equalsIgnoreCase);
        this.stringOperators.put("string contains", String::contains);
        this.stringOperators.put("string contains ignorecase", (inputValue, requiredValue) ->
                inputValue.toLowerCase().contains(requiredValue.toLowerCase()));
    }

    public BiFunction<String, String, Boolean> getStringComparison(String requirementType) {
        return stringOperators.get(requirementType);
    }

    public BiFunction<BigInteger, BigInteger, Boolean> getNumberComparison(String requirementType) {
        return numberOperators.get(requirementType);
    }

    @Override
    public String process(Player player) {
        String result = requirements.values().stream()
                .map(requirement -> requirement.checkRequirement(player, this))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        return result != null ? applyDecorations(result) : null;
    }
}
