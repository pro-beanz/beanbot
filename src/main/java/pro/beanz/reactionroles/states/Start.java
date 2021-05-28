package pro.beanz.reactionroles.states;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Start extends State {
    public Start(IEventManager eventManager, ListenerAdapter listenerAdapter) {
        super(eventManager, listenerAdapter);
        options.put("1️⃣", new CreateA(eventManager, listenerAdapter));
        options.put("2️⃣", new RemoveA(eventManager, listenerAdapter));
        // TODO
    }

    @Override
    public MessageEmbed getMessageEmbed() {
        EmbedBuilder builder = new ReactionRoleEmbedBuilder();
        
        builder.setDescription("Choose an action\n" + 
            "1️⃣ Add a reaction role\n" + 
            "2️⃣ Remove an existing reaction role\n" +
            "3️⃣ Modify an existing reaction role\n"
        );

        return builder.build();
    }
}
