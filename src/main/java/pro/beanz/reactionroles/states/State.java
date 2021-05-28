package pro.beanz.reactionroles.states;

import java.awt.Color;
import java.util.Map;
import java.util.TreeMap;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pro.beanz.reactionroles.ReactionRoleListener;
import pro.beanz.reactionroles.json.JsonReaction;
import pro.beanz.reactionroles.json.JsonRole;

public abstract class State {
    protected final int EMOTE = 0;
    protected final int EMOJI = 1;

    protected final Map<String, State> options;
    protected final IEventManager eventManager;
    protected final ListenerAdapter listenerAdapter;
    private State nextState = this;

    public State(IEventManager eventManager, ListenerAdapter listenerAdapter) {
        options = new TreeMap<String, State>();
        options.put("❌", new Exit(eventManager, listenerAdapter));
        this.eventManager = eventManager;
        this.listenerAdapter = listenerAdapter;
    }

    protected State(IEventManager eventManager, ListenerAdapter listenerAdapter, boolean exit) {
        options = new TreeMap<>();
        this.eventManager = eventManager;
        this.listenerAdapter = listenerAdapter;
    }

    public State nextState() { return nextState; };
    
    public Map<String, State> getValidReactions() { return options; }
    
    public abstract MessageEmbed getMessageEmbed();
    
    public void runReactionState(GuildMessageReactionAddEvent event) {
        String reaction = event.getReactionEmote().getName();

        for (String validReaction : options.keySet()) {
            if (reaction.equals(validReaction))
                nextState = options.get(reaction);
        }
    };
    
    public void runMessageState(GuildMessageReceivedEvent event) {}

    public void run(GenericGuildMessageReactionEvent event) {}

    public void run(GenericGuildEvent event) {
        if (event instanceof GuildMessageReactionAddEvent)
            runReactionState((GuildMessageReactionAddEvent) event);
        else if (event instanceof GuildMessageReceivedEvent)
            runMessageState((GuildMessageReceivedEvent) event);
    }

    protected void setNextState(State nextState) { this.nextState = nextState; }

    public long getTargetId() { return 0; }

    public void exit() {}

    protected Map<JsonReaction, JsonRole> getReactionRoles(Long messageId) {
        ReactionRoleListener listener = (ReactionRoleListener) eventManager.getRegisteredListeners().get(1);
        return listener.getReactionRoles(messageId);
    }

    protected boolean containsReaction(Long messageId, JsonReaction reaction) {
        ReactionRoleListener listener = (ReactionRoleListener) eventManager.getRegisteredListeners().get(1);
        return listener.containsReaction(messageId, reaction);
    }

    protected class ReactionRoleEmbedBuilder extends EmbedBuilder {
        public ReactionRoleEmbedBuilder() {
            super();
            setColor(Color.MAGENTA);
            setTitle("Reaction Roles");
        }
    }
}
