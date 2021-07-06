package pro.beanz.discord.beanbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import pro.beanz.discord.beanbot.commands.lib.Command;
import pro.beanz.discord.beanbot.commands.lib.CommandData;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
    private Command[] commands;
    private EmbedBuilder builder;
    private MessageReceivedEvent e;

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        this.e = event;

        if (e.isFromGuild() && !botPermissionCheck(e)) {
            throw new PermissionException(PERMISSION_ERROR);
        }

        builder = new EmbedBuilder();
        builder.setTitle("Command Help");
        builder.setColor(Color.MAGENTA);

        if (!argumentToCommand(new ArrayList<>(Arrays.asList(args)), (CommandListener) e.getJDA().getRegisteredListeners().get(0))) {
            for (Command command : commands)
                addCommand(command);
        }

        if (builder.getFields().size() == 0)
            builder.setDescription("No command(s) found");

        event.getMessage().reply(builder.build()).queue();
    }

    private void addCommand(Command command) {
        if ((e.isFromGuild() && command.userPermissionCheck(e)
                || !(e.isFromGuild() || command.isServerOnly()))) {
            // subcommand detection
            StringBuilder subCommands = new StringBuilder();
            if (command.getSubcommands().length > 0) {
                subCommands.append("\nSubcommands: ").append(String.join(", ", command.getSubcommands()));
            }

            // add fields
            builder.addField(
                    command.getName(), "Triggers: " + String.join(", ", command.getTriggers()) +
                            "\nUsage: " + command.getUsage() +
                            subCommands +
                            "\nDescription: " + command.getDescription(),
                    false
            );
        }
    }

    public void addCommands(Command[] commands) {
        Arrays.sort(commands, new CommandSort());
        this.commands = commands;
    }

    private boolean argumentToCommand(List<String> args, CommandListener t) {
        // exit recursion if no args
        if (args.size() == 0) return false;

        // figure out if the first arg matches a command trigger in the listener
        for (Command command : t.getCommands()) {
            for (String trigger : command.getTriggers()) {
                // command trigger is in the listener
                if (args.get(0).equalsIgnoreCase(trigger)) {
                    // recurse without the first argument if there is a command listener for the command
                    args.remove(0);
                    CommandListener target = command.getCommandListener();
                    if (args.size() > 0 && target != null) {
                        return argumentToCommand(args, target);
                    }

                    addCommand(command);
                    return true;
                }
            }
        }

        // exit recursion if args do not match command triggers
        return false;
    }

    private static class CommandSort implements Comparator<Command> {
        @Override
        public int compare(Command c1, Command c2) {
            return c1.getName().compareToIgnoreCase(c2.getName());
        }
    }
}
