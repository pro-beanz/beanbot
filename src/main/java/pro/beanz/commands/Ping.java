package pro.beanz.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import pro.beanz.commands.lib.Command;
import pro.beanz.commands.lib.CommandData;

@CommandData(
    name = "Ping",
    description = "Pings the bot",
    usage = "/ping",
    triggers = { "ping" },
    callerPermissions = {
        Permission.MESSAGE_READ,
        Permission.MESSAGE_WRITE
    },
    botPermissions = {
        Permission.MESSAGE_READ,
        Permission.MESSAGE_WRITE
    },
    gatewayIntents = {
        GatewayIntent.DIRECT_MESSAGES,
        GatewayIntent.GUILD_MESSAGES
    }
)

public class Ping extends Command {
    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (event.isFromGuild() && !botPermissionCheck(event)) {
            throw new PermissionException(PERMISSION_ERROR);
        }

        event.getChannel().sendMessage("Pong!").queue();
    }
}
