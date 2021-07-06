package pro.beanz.discord.beanbot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.beanz.discord.beanbot.commands.lib.Command;

public class CommandListener extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(CommandListener.class);

    private final Command[] commands;
    private final String[] triggers;

    // for the primary command listener
    public CommandListener(String[] triggers, Command[] commands) {
        super();
        this.triggers = triggers;
        this.commands = commands;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // do nothing for bot messages
        if (event.getAuthor().isBot()) return;

        // do nothing for messages not beginning with a trigger prefix
        String prefix = "";
        for (String trigger : triggers) {
            if (event.getMessage().getContentRaw().startsWith(trigger)) {
                prefix = trigger;
                break;
            }
        }
        if (prefix.equals("")) return;

        String rawMessage = event.getMessage().getContentRaw().trim().substring(prefix.length());
        String[] message = rawMessage.split(" ");

        String input = message[0];

        String[] args = new String[message.length - 1];
        System.arraycopy(message, 1, args, 0, message.length - 1);

        for (Command command : commands) {
            for (String trigger : command.getTriggers()) {
                if (trigger.equalsIgnoreCase(input) && command.getMinArgs() < message.length) {
                    try {
                        // execute command if the trigger is detected
                        command.execute(event, args);
                    } catch (NullPointerException | PermissionException e) {
                        // catch and display any error messages in command execution
                        // mainly limited to permissions and private message restrictions
                        log.warn(e.getMessage());
                        e.printStackTrace();
                        event.getChannel().sendMessage(e.getMessage()).queue();
                    }
                    return;
                } else if (trigger.equalsIgnoreCase(input)) {
                    commands[0].execute(event, message);
                }
            }
        }
    }

    public Command[] getCommands() {
        return commands;
    }
}
