package dev.nutellim.MyPlaceholders.utilities.subcommand;

import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
public abstract class SubCommand {

    private final String description;
    private final Set<String> parameters;
    private final String permission;

    public SubCommand(String permission, String description, Set<String> parameters) {
        this.parameters = parameters;
        this.permission = permission;
        this.description = description;
    }

    public SubCommand(String permission, String description, String... parameters) {
        this.parameters = new LinkedHashSet<>();
        this.parameters.addAll(Arrays.asList(parameters));
        this.permission = permission;
        this.description = description;
    }

    public SubCommand(String description, Set<String> parameters) {
        this("", description, parameters);
    }

    public SubCommand(String permission, String description) {
        this(permission, description, new HashSet<>());
    }

    public SubCommand(String description) {
        this("", description, new HashSet<>());
    }

    public String getParametersFormatted() {
        return parameters.isEmpty() ? "" : " " + String.join(" ", parameters);
    }

    public boolean hasPermission(CommandSender sender) {
        return permission.isEmpty() || sender.hasPermission(permission);
    }

    public abstract void execute(CommandSender sender, String label, String[] args);
}
