package dev.nutellim.MyPlaceholders.commands.subcommands;

import dev.nutellim.MyPlaceholders.MyPlaceholder;
import dev.nutellim.MyPlaceholders.controllers.PlaceholderController;
import dev.nutellim.MyPlaceholders.utilities.ChatUtil;
import dev.nutellim.MyPlaceholders.utilities.subcommand.SubCommand;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;

public class MyPlaceholderFolderCommand extends SubCommand {

    private final MyPlaceholder plugin;
    private final PlaceholderController placeholderController;

    public MyPlaceholderFolderCommand(MyPlaceholder plugin) {
        super("myplaceholder.command.myplaceholder.folder",
              "Enable or disable a placeholder file.",
              "<file>", "<enable|disable|status>");
        this.plugin = plugin;
        this.placeholderController = plugin.getPlaceholderController();
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Audience audience = plugin.getAdventure().sender(sender);
        String prefix = plugin.getLang().getPrefix();

        if (args.length == 1) {
            listFiles(audience, prefix);
            return;
        }

        if (args.length < 3) {
            ChatUtil.sendMessage(audience, prefix + "&cUsage: /" + label + " folder <file> <enable|disable|status>");
            return;
        }

        String categoryId = args[1];
        String action = args[2].toLowerCase();
        File file = placeholderController.getPlaceholderFile(categoryId);

        if (!file.exists()) {
            ChatUtil.sendMessage(audience, prefix + "&cFile '&f" + categoryId + "&c' not found.");
            return;
        }

        switch (action) {
            case "enable":
                if (isEnabled(file)) {
                    ChatUtil.sendMessage(audience, prefix + "&eFile '&f" + categoryId + "&e' is already enabled.");
                    return;
                }
                placeholderController.setFileEnabled(file, true);
                plugin.onReload();
                ChatUtil.sendMessage(audience, prefix + "&#7CFC00File '&f" + categoryId + "&#7CFC00' enabled and reloaded.");
                break;
            case "disable":
                if (!isEnabled(file)) {
                    ChatUtil.sendMessage(audience, prefix + "&eFile '&f" + categoryId + "&e' is already disabled.");
                    return;
                }
                placeholderController.setFileEnabled(file, false);
                plugin.onReload();
                ChatUtil.sendMessage(audience, prefix + "&#FF5500File '&f" + categoryId + "&#FF5500' disabled and reloaded.");
                break;
            case "status":
                boolean enabled = isEnabled(file);
                String state = enabled ? "&#7CFC00ENABLED" : "&#FF5500DISABLED";
                ChatUtil.sendMessage(audience, prefix + "&f" + categoryId + " &8→ " + state);
                break;
            default:
                ChatUtil.sendMessage(audience, prefix + "&cUnknown action '&f" + action + "&c'. Use enable, disable or status.");
        }
    }

    private void listFiles(Audience audience, String prefix) {
        File root = new File(plugin.getDataFolder(), "placeholders");
        ChatUtil.sendMessage(audience, prefix + "&#BEFFDD&lPlaceholder Files:");
        listDir(audience, root, root);
    }

    private void listDir(Audience audience, File folder, File root) {
        File[] files = folder.listFiles();
        if (files == null) return;
        Arrays.sort(files);
        for (File file : files) {
            if (file.isDirectory()) {
                listDir(audience, file, root);
            } else if (file.getName().endsWith(".yml")) {
                String relativePath = root.toURI().relativize(file.toURI()).getPath();
                String categoryId = relativePath.replace(".yml", "").replace("\\", "/");
                boolean enabled = isEnabled(file);
                String state = enabled ? "&#7CFC00● " : "&#FF5500● ";
                ChatUtil.sendMessage(audience, " " + state + "&f" + categoryId);
            }
        }
    }

    private boolean isEnabled(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getBoolean("enabled", true);
    }
}
