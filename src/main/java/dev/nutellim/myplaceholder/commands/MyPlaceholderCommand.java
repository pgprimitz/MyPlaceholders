package dev.nutellim.myplaceholder.commands;

import dev.nutellim.myplaceholder.MyPlaceholder;
import dev.nutellim.myplaceholder.commands.subcommands.*;
import dev.nutellim.myplaceholder.commands.subcommands.*;
import dev.nutellim.myplaceholder.controllers.PlaceholderController;
import dev.nutellim.myplaceholder.models.PlaceholderCategory;
import dev.nutellim.myplaceholder.utilities.ChatUtil;
import dev.nutellim.myplaceholder.utilities.subcommand.SubCommand;
import dev.nutellim.myplaceholder.utilities.subcommand.SubCommandHelper;
import dev.nutellim.myplaceholder.utilities.MapUtil;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MyPlaceholderCommand implements CommandExecutor, TabCompleter {

    private final MyPlaceholder plugin;
    private final PlaceholderController placeholderController;
    private final Map<String, SubCommand> subCommands;

    private final String helpMessage = String.join("\n",
            "%line%",
            "<b><gold>MyPlaceholder Commands</gold></b>",
            "",
            "%subcommands%",
            "%line%"
    );
    private final String helpMessageFormat =
            " <gray>●</gray> /%label% %subcommand% <dark_gray>-</dark_gray> <yellow>%description%</yellow>";

    public MyPlaceholderCommand(MyPlaceholder plugin) {
        this.plugin = plugin;
        this.placeholderController = plugin.getPlaceholderController();
        this.subCommands = MapUtil.ofOrderMap(
                MapUtil.entry("config", new MyPlaceholderConfigCommand(plugin)),
                MapUtil.entry("reload", new MyPlaceholderReloadCommand(plugin)),
                MapUtil.entry("parse", new MyPlaceholderParseCommand(plugin)),
                MapUtil.entry("errorlog", new MyPlaceholderErrorLogCommand(plugin)),
                MapUtil.entry("folder", new MyPlaceholderFolderCommand(plugin))
        );
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        Audience audience = plugin.getAdventure().sender(sender);

        if (args.length == 0) {
            ChatUtil.sendMessage(audience, SubCommandHelper.getSubCommandFormat(
                    label, subCommands, helpMessage, helpMessageFormat));
            return true;
        }

        String sub = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(sub);

        if (subCommand == null) {
            String msg = plugin.getLang().get("command-messages.not-found").replace("%command%", sub);
            ChatUtil.sendMessage(audience, plugin.getLang().getPrefix() + msg);
            return true;
        }

        if (!subCommand.hasPermission(sender)) {
            String msg = plugin.getLang().get("command-messages.no-permission");
            ChatUtil.sendMessage(audience, plugin.getLang().getPrefix() + msg);
            return true;
        }

        subCommand.execute(sender, label, args);
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                       @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(new ArrayList<>(subCommands.keySet()), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("config")) {
            return filter(new ArrayList<>(placeholderController.getPlaceholderCategories().keySet()), args[1]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("folder")) {
            File root = new File(plugin.getDataFolder(), "placeholders");
            List<String> files = new ArrayList<>();
            collectFiles(root, root, files);
            return filter(files, args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("folder")) {
            return filter(Arrays.asList("enable", "disable", "status"), args[2]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("config")) {
            PlaceholderCategory cat = findCategory(args[1]);
            if (cat == null) return null;
            return filter(new ArrayList<>(cat.getPlaceholders().keySet()), args[2]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("parse")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return null;
    }

    private PlaceholderCategory findCategory(String name) {
        for (Map.Entry<String, PlaceholderCategory> entry : placeholderController.getPlaceholderCategories().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) return entry.getValue();
        }
        return null;
    }

    private void collectFiles(File folder, File root, List<String> result) {
        File[] files = folder.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                collectFiles(file, root, result);
            } else if (file.getName().endsWith(".yml")) {
                String rel = root.toURI().relativize(file.toURI()).getPath();
                result.add(rel.replace(".yml", "").replace("\\", "/"));
            }
        }
    }

    private List<String> filter(List<String> list, String typed) {
        String lower = typed.toLowerCase();
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}
