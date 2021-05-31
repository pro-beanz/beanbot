package pro.beanz.discord.beanbot.reactionroles.states;

import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CreateB extends State {
    private Message target;

    public CreateB(IEventManager eventManager, ListenerAdapter listenerAdapter,
            Message target) {
        super(eventManager, listenerAdapter);
        this.target = target;
    }

    @Override
    public MessageEmbed getMessageEmbed() {
        EmbedBuilder builder = new ReactionRoleEmbedBuilder();
        
        builder.setDescription("React to the target message with the desired reaction.");

        return builder.build();
    }

    @Override
    public void runReactionState(GuildMessageReactionAddEvent event) {
        Long messageId = event.getMessageIdLong();
        if (messageId == target.getIdLong()) {
            ReactionEmote reactionEmote = event.getReactionEmote();

            // check if the bot has already reacted with this emote
            MessageHistory history = MessageHistory.getHistoryAround(event.getChannel(), messageId + "").complete();
            List<User> users;
            if (reactionEmote.isEmote()) {
                users = history.getMessageById(messageId).retrieveReactionUsers(reactionEmote.getEmote()).complete();
            } else {
                users = history.getMessageById(messageId).retrieveReactionUsers(reactionEmote.getEmoji()).complete();
            }
            JDA jda = event.getJDA();
            if (users.contains(jda.getUserById(jda.getSelfUser().getIdLong()))) {
                return;
            }

            try {
                target.addReaction(reactionEmote.getAsReactionCode()).complete();
                // emote vs emoji handling
                if (reactionEmote.isEmote())
                    target.removeReaction(reactionEmote.getEmote(), event.getUser()).queue();
                else   
                    target.removeReaction(reactionEmote.getEmoji(), event.getUser()).queue();
                
                setNextState(new CreateC(eventManager, listenerAdapter, target, reactionEmote));
            } catch (ErrorResponseException e) {}
        } else {
            super.runReactionState(event);
        }
    }

    @Override
    public long getTargetId() { return target.getIdLong(); }
}
