package dev.nutellim.MyPlaceholders.commands.subcommands;

import dev.nutellim.MyPlaceholders.MyPlaceholder;
import dev.nutellim.MyPlaceholders.utilities.ChatUtil;
import dev.nutellim.MyPlaceholders.utilities.subcommand.SubCommand;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;

import java.util.List;

public class MyPlaceholderErrorLogCommand extends SubCommand {

    private final MyPlaceholder plugin;

    public MyPlaceholderErrorLogCommand(MyPlaceholder plugin) {
        super("myplaceholder.command.myplaceholder", "Show placeholder error log.");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Audience audience = plugin.getAdventure().sender(sender);
        String prefix = plugin.getLang().getPrefix();
        List<String> errors = plugin.getPlaceholderController().getErrorLog();

        if (errors.isEmpty()) {
            ChatUtil.sendMessage(audience, prefix + plugin.getLang().get("command-messages.error-none"));
            return;
        }

        ChatUtil.sendMessage(audience, plugin.getLang().get("command-messages.error-log-header"));
        for (String error : errors) {
            ChatUtil.sendMessage(audience, " &#FF5500● &f" + error);
        }
    }
}
