package pro.beanz.discord.beanbot.reactionroles.states;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Start extends State {
    public Start(IEventManager eventManager, ListenerAdapter listenerAdapter) {
        super(eventManager, listenerAdapter);
        options.put("\u0031\u20E3", new CreateA(eventManager, listenerAdapter));
        options.put("\u0032\u20E3", new RemoveA(eventManager, listenerAdapter));
        options.put("\u0033\u20E3", new ModifyA(eventManager, listenerAdapter));
    }

    @Override
    public MessageEmbed getMessageEmbed() {
        EmbedBuilder builder = new ReactionRoleEmbedBuilder();
        
        builder.setDescription("Choose an action\n" + 
            "\u0031\u20E3 Add a reaction role\n" +
            "\u0032\u20E3 Remove an existing reaction role\n" +
            "\u0033\u20E3 Modify an existing reaction role\n"
        );

        return builder.build();
    }
}
