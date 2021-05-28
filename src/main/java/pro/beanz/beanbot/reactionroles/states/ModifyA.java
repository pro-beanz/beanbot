package pro.beanz.beanbot.reactionroles.states;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pro.beanz.beanbot.reactionroles.ReactionRoleListener;

public class ModifyA extends State {
    public ModifyA(IEventManager eventManager, ListenerAdapter listenerAdapter) {
        super(eventManager, listenerAdapter);
    }

    @Override
    public MessageEmbed getMessageEmbed() {
        EmbedBuilder builder = new ReactionRoleEmbedBuilder();

        builder.setDescription("Input the message ID of the message you would like to modify");

        return builder.build();
    }

    @Override
    public void runMessageState(GuildMessageReceivedEvent event) {
        String id = event.getMessage().getContentRaw();
        if (id.matches("^\\d{18}$")) {
            try {
                Message message = event.getChannel().retrieveMessageById(id).complete();
                if (((ReactionRoleListener) eventManager.getRegisteredListeners().get(1))
                        .getReactionRoles(message.getIdLong()) != null) {
                    setNextState(new ModifyB(eventManager, listenerAdapter, message));
                    event.getMessage().delete().queue();
                }
            } catch (ErrorResponseException e) {}
        }
    }
}
