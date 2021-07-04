package pro.beanz.discord.beanbot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.beanz.discord.beanbot.commands.lib.Command;
import pro.beanz.discord.beanbot.commands.lib.CommandData;

@CommandData(
        name = "Ping",
        description = "Pings the bot",
        usage = "/ping",
        triggers = {"ping"},
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
    private static final Logger log = LoggerFactory.getLogger(Ping.class);

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (event.isFromGuild() && !botPermissionCheck(event)) {
            throw new PermissionException(PERMISSION_ERROR);
        }

        event.getJDA().getRestPing().queue((ping) -> {
            event.getMessage().replyFormat("pong! my ping is %dms", ping).queue();
            log.info(String.format("%dms", ping));
        });
    }
}
