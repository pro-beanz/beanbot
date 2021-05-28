package pro.beanz.beanbot.reactionroles.states;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pro.beanz.beanbot.reactionroles.ReactionRoleListener;
import pro.beanz.beanbot.reactionroles.json.JsonReaction;
import pro.beanz.beanbot.reactionroles.json.JsonRole;

public class CreateC extends State {
    private Message target;
    private ReactionEmote reactionEmote;

    public CreateC(IEventManager eventManager, ListenerAdapter listenerAdapter,
            Message target, ReactionEmote reactionEmote) {
        super(eventManager, listenerAdapter);
        this.target = target;
        this.reactionEmote = reactionEmote;
    }

    @Override
    public MessageEmbed getMessageEmbed() {
        EmbedBuilder builder = new ReactionRoleEmbedBuilder();

        builder.setDescription("Input the ID of the role you would like to link to this emote");

        return builder.build();
    }

    @Override
    public void runReactionState(GuildMessageReactionAddEvent event) {
        String reaction = event.getReactionEmote().getName();
        
        for (String validReaction : options.keySet()) {
            if (reaction.equals(validReaction)){
                if (reactionEmote.isEmote()) {
                    target.removeReaction(reactionEmote.getEmote()).queue();
                } else {
                    target.removeReaction(reactionEmote.getEmoji()).queue();
                }
                setNextState(options.get(reaction));
            }
        }
    }

    @Override
    public void runMessageState(GuildMessageReceivedEvent event) throws IllegalArgumentException {
        String id = event.getMessage().getContentRaw();
        if (id.matches("^\\d{18}$")) {
            Role role = event.getGuild().getRoleById(id);
            if (role != null) {
                event.getMessage().delete().queue();

                ReactionRoleListener listener = (ReactionRoleListener) eventManager.getRegisteredListeners().get(1);

                JsonReaction reaction;
                if (reactionEmote.isEmote()) {
                    reaction = new JsonReaction(JsonReaction.EMOTE, reactionEmote.getEmote().getId());
                } else {
                    reaction = new JsonReaction(JsonReaction.EMOJI, reactionEmote.getEmoji());
                }

                if (listener.addReactionRole(target.getChannel().getIdLong(), target.getIdLong(),
                        reaction, new JsonRole(role.getIdLong()))) {
                    setNextState(new CreateLoopCheck(eventManager, listenerAdapter, target));
                }
            }
        }
    }

    @Override
    public long getTargetId() {
        return target.getIdLong();
    }
}
