package pro.beanz.discord.beanbot.reactionroles.states;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ModifyLoopCheck extends State {
    public ModifyLoopCheck(IEventManager eventManager, ListenerAdapter listenerAdapter, Message target) {
        super(eventManager, listenerAdapter);
        options.put("✔", new ModifyB(eventManager, listenerAdapter, target));
    }

    @Override
    public MessageEmbed getMessageEmbed() {
        EmbedBuilder builder = new ReactionRoleEmbedBuilder();

        builder.setDescription("Modify another reaction role?");

        return builder.build();
    }
}
