package pro.beanz.discord.beanbot.commands;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.beanz.discord.beanbot.commands.lib.Command;

public class CommandListener extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(CommandListener.class);

    private final Command[] commands = {
            new Help(),
            new Ping(),
            new ReactionRoleSetup()
    };

    public final char prefix = '/';

    public CommandListener() {
        super();
        ((Help) commands[0]).addCommands(commands);
        log.info("Ready");
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // do nothing for bot messages
        if (event.getAuthor().isBot() || event.getMessage().getContentRaw().charAt(0) != prefix)
            return;

        String[] message = event.getMessage().getContentRaw().trim().split(" ");
        String input = message[0].substring(1);

        String[] args = new String[message.length - 1];
        System.arraycopy(message, 1, args, 0, message.length - 1);

        log.info(event.getAuthor().getAsTag() + " executed " + input);

        for (Command command : commands) {
            for (String trigger : command.getTriggers()) {
                if (trigger.equalsIgnoreCase(input) && command.getMinArgs() < message.length) {
                    try {
                        // execute command if the trigger is detected
                        command.execute(event, args);
                        log.info(command.getName() + " executed");
                    } catch (NullPointerException | PermissionException e) {
                        // catch and display any error messages in command execution
                        // mainly limited to permissions and private message restrictions
                        log.warn(e.getMessage());
                        e.printStackTrace();
                        event.getChannel().sendMessage(e.getMessage()).queue();
                    }
                }
            }
        }
    }

    public Command[] getCommands() {
        return commands;
    }
}
