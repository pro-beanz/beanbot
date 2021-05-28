package pro.beanz.reactionroles.states;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pro.beanz.reactionroles.ReactionRoleListener;
import pro.beanz.reactionroles.json.JsonReaction;

public class RemoveC extends State {
    private Message target;
    private JsonReaction reaction;

    public RemoveC(IEventManager eventManager, ListenerAdapter listenerAdapter, Message target,
            JsonReaction reaction) {
        super(eventManager, listenerAdapter);
        this.target = target;
        this.reaction = reaction;
    }

    @Override
    public MessageEmbed getMessageEmbed() {
        EmbedBuilder builder = new ReactionRoleEmbedBuilder();

        builder.setDescription("Toggle again to confirm.");

        return builder.build();
    }

    public void run(GenericGuildEvent event) {
        GenericGuildMessageReactionEvent e = (GenericGuildMessageReactionEvent) event;
        Long messageId = e.getMessageIdLong();
        if (messageId == target.getIdLong()) {
            ReactionEmote reactionEmote = e.getReactionEmote();
            JsonReaction newReaction;
            if (reactionEmote.isEmote()) {
                newReaction = new JsonReaction(JsonReaction.EMOTE, reactionEmote.getEmote().getId());
            } else {
                newReaction = new JsonReaction(JsonReaction.EMOJI, reactionEmote.getEmoji());
            }

            if (reaction.equals(newReaction)) {
                ReactionRoleListener listener = (ReactionRoleListener) eventManager.getRegisteredListeners().get(1);
                listener.removeReactionRole(messageId, reaction);
                if (listener.containsMessage(messageId)) {
                    listener.removeReactionRole(messageId);
                }
                target.clearReactions(reaction.getReaction()).queue();

                // iterate state
                if (listener.containsMessage(messageId)) {
                    setNextState(new RemoveLoopCheck(eventManager, listenerAdapter, target));
                } else {
                    setNextState(new Exit(eventManager, listenerAdapter));
                }
            }
        } else if (e instanceof GuildMessageReactionAddEvent) {
            runReactionState((GuildMessageReactionAddEvent) e);
        }
    }
}
