package pro.beanz.beanbot.reactionroles.states;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RemoveLoopCheck extends State {
    public RemoveLoopCheck(IEventManager eventManager, ListenerAdapter listenerAdapter, Message target) {
        super(eventManager, listenerAdapter);
        options.put("❌", new Exit(eventManager, listenerAdapter));
        options.put("✔", new RemoveB(eventManager, listenerAdapter, target));
    }

    @Override
    public MessageEmbed getMessageEmbed() {
        EmbedBuilder builder = new ReactionRoleEmbedBuilder();

        builder.setDescription("Remove more reaction roles?");

        return builder.build();
    }
}
