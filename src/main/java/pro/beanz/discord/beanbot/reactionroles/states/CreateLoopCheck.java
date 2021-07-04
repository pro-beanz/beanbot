package pro.beanz.discord.beanbot.reactionroles.states;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CreateLoopCheck extends State {
    public CreateLoopCheck(IEventManager eventManager, ListenerAdapter listenerAdapter, Message target) {
        super(eventManager, listenerAdapter);
        options.put("\u2705", new CreateB(eventManager, listenerAdapter, target));
    }

    @Override
    public MessageEmbed getMessageEmbed() {
        EmbedBuilder builder = new ReactionRoleEmbedBuilder();

        builder.setDescription("Add another reaction role?");

        return builder.build();
    }
}
