package pro.beanz.discord.beanbot.reactionroles.states;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pro.beanz.discord.beanbot.commands.ReactionRoleSetup.SetupListener;

public class Exit extends State {
    public Exit(IEventManager eventManager, ListenerAdapter listenerAdapter) {
        super(eventManager, listenerAdapter, false);
    }

    @Override
    public MessageEmbed getMessageEmbed() { return null;  }
    
    @Override
    public void exit() {
        ((SetupListener) listenerAdapter).getConfigMessage().delete().complete();
        eventManager.unregister(listenerAdapter);
    }
}
