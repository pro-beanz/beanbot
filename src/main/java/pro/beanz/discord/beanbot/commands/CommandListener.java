package pro.beanz.discord.beanbot.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pro.beanz.discord.beanbot.commands.lib.Command;

public class CommandListener extends ListenerAdapter {
    private static Logger log = LoggerFactory.getLogger(CommandListener.class);

    private Command[] commands = {
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
    public void onReady(ReadyEvent event) {
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // do nothing for bot messages
        if (event.getAuthor().isBot() || event.getMessage().getContentRaw().charAt(0) != prefix)
            return;

        String[] message = event.getMessage().getContentRaw().trim().split(" ");
        String input = message[0].substring(1);

        String[] args = new String[message.length - 1];
        for (int i = 1; i < message.length; i++)
            args[i - 1] = message[i];

        log.info(event.getAuthor().getAsTag() + " executed " + input);

        for (Command command : commands) {
            for (String trigger : command.getTriggers()) {
                if (trigger.equals(input) && command.getMinArgs() < message.length) {
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
