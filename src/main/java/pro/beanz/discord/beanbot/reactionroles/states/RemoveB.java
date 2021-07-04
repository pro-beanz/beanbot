package pro.beanz.discord.beanbot.reactionroles.states;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pro.beanz.discord.beanbot.reactionroles.json.JsonReaction;

public class RemoveB extends State {
    private final Message target;

    public RemoveB(IEventManager eventManager, ListenerAdapter listenerAdapter, Message target) {
        super(eventManager, listenerAdapter);
        this.target = target;
    }

    @Override
    public MessageEmbed getMessageEmbed() {
        EmbedBuilder builder = new ReactionRoleEmbedBuilder();

        builder.setDescription("Toggle the reaction you would like to remove");

        return builder.build();
    }

    @Override
    public void run(GenericGuildEvent event) {
        GenericGuildMessageReactionEvent e = (GenericGuildMessageReactionEvent) event;
        long messageId = e.getMessageIdLong();
        if (messageId == target.getIdLong()) {
            ReactionEmote reactionEmote = e.getReactionEmote();
            JsonReaction reaction;
            if (reactionEmote.isEmote()) {
                reaction = new JsonReaction(JsonReaction.EMOTE, reactionEmote.getEmote().getId());
            } else {
                reaction = new JsonReaction(JsonReaction.EMOJI, reactionEmote.getEmoji());
            }

            if (containsReaction(messageId, reaction)) {
                setNextState(new RemoveC(eventManager, listenerAdapter, target, reaction));
            }
        } else if (e instanceof GuildMessageReactionAddEvent) {
            runReactionState((GuildMessageReactionAddEvent) e);
        }
    }
}
