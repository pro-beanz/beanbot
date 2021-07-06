package pro.beanz.discord.beanbot.commands.lib;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pro.beanz.discord.beanbot.Main;
import pro.beanz.discord.beanbot.commands.CommandListener;

public abstract class CommandParent extends Command {
    private final CommandListener listener;

    public CommandParent(Command[] subCommands) {
        String[] triggers = getTriggers();
        for (int i = 0; i < triggers.length; i++) {
            triggers[i] = Main.prefix + triggers[i] + " ";
        }
        listener = new CommandListener(triggers, subCommands);
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        // base command does nothing
        // root listener displays a help prompt for this command on call
    }

    @Override
    public CommandListener getCommandListener() { return listener; }
}
