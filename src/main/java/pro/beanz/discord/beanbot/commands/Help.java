package pro.beanz.discord.beanbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import pro.beanz.discord.beanbot.commands.lib.Command;
import pro.beanz.discord.beanbot.commands.lib.CommandData;

@CommandData(
        name = "Help",
        description = "Displays command descriptions, usages, and triggers",
        usage = "/help [command trigger]",
        triggers = {
                "help", "?"
        },
        callerPermissions = {
                Permission.MESSAGE_READ,
                Permission.MESSAGE_WRITE
        },
        botPermissions = {
                Permission.MESSAGE_READ,
                Permission.MESSAGE_EMBED_LINKS,
                Permission.MESSAGE_WRITE
        },
        gatewayIntents = {
                GatewayIntent.GUILD_MESSAGES
        }
)

public class Help extends Command {
    Command[] commands;

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (event.isFromGuild() && !botPermissionCheck(event)) {
            throw new PermissionException(PERMISSION_ERROR);
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Command Help");

        if (args.length > 0) {
            String arg = args[0].toLowerCase();
            for (Command command : commands)
                for (String trigger : command.getTriggers())
                    if (arg.equalsIgnoreCase(trigger))
                        addCommand(builder, command, event);
        } else
            for (Command command : commands)
                addCommand(builder, command, event);

        if (builder.getFields().size() == 0)
            builder.setDescription("No command(s) found");

        event.getChannel().sendMessage(builder.build()).queue();
    }

    private void addCommand(EmbedBuilder builder, Command command, MessageReceivedEvent event) {
        if ((event.isFromGuild() && command.userPermissionCheck(event)
                || !(event.isFromGuild() || command.isServerOnly()))) {
            builder.addField(command.getName(), "Triggers: " + String.join(", ", command.getTriggers()) + "\nUsage: "
                    + command.getUsage() + "\nDescription: " + command.getDescription(), false);
        }
    }

    public void addCommands(Command[] commands) {
        this.commands = commands;
    }
}
