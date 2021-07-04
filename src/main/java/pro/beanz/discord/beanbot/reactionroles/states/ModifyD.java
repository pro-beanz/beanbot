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
import pro.beanz.discord.beanbot.reactionroles.ReactionRoleListener;
import pro.beanz.discord.beanbot.reactionroles.json.JsonReaction;
import pro.beanz.discord.beanbot.reactionroles.json.JsonRole;

public class ModifyD extends State {
    Message target;
    JsonReaction reaction;
    JsonRole role;

    public ModifyD(IEventManager eventManager, ListenerAdapter listenerAdapter,
            Message target, JsonReaction reaction, JsonRole role) {
        super(eventManager, listenerAdapter);
        this.target = target;
        this.reaction = reaction;
        this.role = role;
    }

    @Override
    public MessageEmbed getMessageEmbed() {
        EmbedBuilder builder = new ReactionRoleEmbedBuilder();

        builder.setDescription(("Toggle reaction again to confirm"));

        return builder.build();
    }
    
    public void run(GenericGuildEvent event) {
        GenericGuildMessageReactionEvent e = (GenericGuildMessageReactionEvent) event;
        long messageId = e.getMessageIdLong();
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
                listener.addReactionRole(target.getChannel().getIdLong(),
                    target.getIdLong(), reaction, role);
                

                // iterate state
                if (listener.containsMessage(messageId)) {
                    setNextState(new ModifyLoopCheck(eventManager, listenerAdapter, target));
                } else {
                    setNextState(new Exit(eventManager, listenerAdapter));
                }
            }
        } else if (e instanceof GuildMessageReactionAddEvent) {
            runReactionState((GuildMessageReactionAddEvent) e);
        }
    }
}
