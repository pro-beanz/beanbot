package pro.beanz.reactionroles.states;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CreateA extends State {
    public CreateA(IEventManager eventManager, ListenerAdapter listenerAdapter) {
        super(eventManager, listenerAdapter);
    }

    @Override
    public MessageEmbed getMessageEmbed() {
        EmbedBuilder builder = new ReactionRoleEmbedBuilder();

        builder.setDescription("Input the message ID of the desired reaction role message");

        return builder.build();
    }

    @Override
    public void runMessageState(GuildMessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().matches("^\\d{18}$")) {
            try {
                setNextState(new CreateB(eventManager, listenerAdapter,
                        event.getChannel().retrieveMessageById(event.getMessage().getContentRaw()).complete()
                ));
                event.getMessage().delete().queue();
            } catch (ErrorResponseException e) {}
        }
    }
}