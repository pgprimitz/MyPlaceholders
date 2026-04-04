package dev.nutellim.MyPlaceholders.commands.subcommands;

import dev.nutellim.MyPlaceholders.MyPlaceholder;
import dev.nutellim.MyPlaceholders.utilities.ChatUtil;
import dev.nutellim.MyPlaceholders.utilities.JavaUtil;
import dev.nutellim.MyPlaceholders.utilities.subcommand.SubCommand;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MyPlaceholderParseCommand extends SubCommand {

    private final MyPlaceholder plugin;

    private static final ExecutorService EVAL_EXECUTOR = Executors.newCachedThreadPool();
    private static final long TIMEOUT_MS = 100;

    public MyPlaceholderParseCommand(MyPlaceholder plugin) {
        super("myplaceholder.command.myplaceholder.parse", "Parse a placeholder for a player.", "<player>", "<placeholder>");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Audience audience = plugin.getAdventure().sender(sender);
        String prefix = plugin.getLang().getPrefix();

        if (args.length < 3) {
            ChatUtil.sendMessage(audience, prefix + plugin.getLang().get("command-messages.parse-usage"));
            return;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            String msg = plugin.getLang().get("command-messages.parse-player-not-found")
                    .replace("%player%", playerName);
            ChatUtil.sendMessage(audience, prefix + msg);
            return;
        }

        String placeholderText = JavaUtil.join(args, 2);

        Future<String> future = EVAL_EXECUTOR.submit(() ->
                PlaceholderAPI.setPlaceholders(target, placeholderText)
        );

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String result;
            try {
                result = future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                result = "§cCalculation timed out (>" + TIMEOUT_MS + "ms).";
            } catch (Exception e) {
                result = "§cError: " + e.getMessage();
            }

            final String finalResult = result;

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                ChatUtil.sendMessage(audience,
                        prefix + plugin.getLang().get("command-messages.parse-result")
                                .replace("%placeholder%", placeholderText));

                audience.sendMessage(
                        LegacyComponentSerializer.legacySection()
                                .deserialize("  " + finalResult)
                );
            });
        });
    }
}
