package pro.beanz.discord.beanbot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.beanz.discord.beanbot.commands.lib.Command;
import pro.beanz.discord.beanbot.commands.lib.CommandData;
import pro.beanz.discord.beanbot.reactionroles.states.*;

@CommandData(
        name = "Reaction Role Setup",
        description = "Initiates setup for reaction roles",
        usage = "/rr",
        triggers = {
                "rr",
                "reactionrole",
                "reactionroles",
                "rrole"
        },
        callerPermissions = {
                Permission.MESSAGE_READ,
                Permission.MESSAGE_WRITE,
                Permission.MANAGE_ROLES,
                Permission.MESSAGE_ADD_REACTION
        },
        botPermissions = {
                Permission.MESSAGE_READ,
                Permission.MESSAGE_WRITE,
                Permission.MESSAGE_EMBED_LINKS,
                Permission.MANAGE_ROLES,
                Permission.MESSAGE_ADD_REACTION,
                Permission.MESSAGE_MANAGE,
                Permission.MESSAGE_HISTORY
        },
        gatewayIntents = {
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS
        },
        serverOnly = true
)

public class ReactionRoleSetup extends Command {
    private static final Logger log = LoggerFactory.getLogger(ReactionRoleSetup.class);

    public ReactionRoleSetup() {
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!event.isFromGuild()) {
            throw new NullPointerException(PRIVATE_MESSAGE_ERROR);
        }

        if (userPermissionCheck(event)) {
            if (botPermissionCheck(event)) {
                event.getJDA().getEventManager().register(new SetupListener(event));
                event.getMessage().delete().queue();
            } else throw new PermissionException(PERMISSION_ERROR.replaceAll("TARGET", "Bot"));
        } else throw new PermissionException(PERMISSION_ERROR.replaceAll("TARGET", event.getAuthor().getName()));
    }

    // worker class for taking input specifically for Reaction Role setup
    // modifies the main ReactionRoleListener's list of ReactionRoles
    public static class SetupListener extends ListenerAdapter {
        private State state;
        private final Message configMessage;
        private final long callerId;

        public SetupListener(MessageReceivedEvent event) {
            state = new Start(event.getJDA().getEventManager(), this);
            callerId = event.getAuthor().getIdLong();

            configMessage = event.getChannel().sendMessage(state.getMessageEmbed()).complete();
            for (String reaction : state.getValidReactions().keySet()) {
                configMessage.addReaction(reaction).queue();
            }
        }

        @Override
        public void onGenericGuildMessageReaction(GenericGuildMessageReactionEvent event) {
            if (event.getUserIdLong() == callerId &&
                    (state instanceof RemoveB ||
                            state instanceof RemoveC ||
                            state instanceof ModifyB ||
                            state instanceof ModifyD)) {
                runAction(event);
            }
        }

        @Override
        public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
            if (event.getUserIdLong() == callerId &&
                    (event.getMessageIdLong() == configMessage.getIdLong() ||
                            (state instanceof CreateB && event.getMessageIdLong() == state.getTargetId())))
                runAction(event);
        }

        @Override
        public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
            if (event.getAuthor().getIdLong() == callerId &&
                    event.getChannel().getIdLong() == configMessage.getChannel().getIdLong())
                runAction(event);
        }

        private void runAction(GenericGuildEvent event) {
            try {
                state.run(event);
            } catch (IllegalArgumentException e) {
                log.info("Illegal argument");
                e.printStackTrace();
            }
            if (!state.equals(state.nextState())) {
                state = state.nextState();

                // run exit sequence if state is Exit
                if (state instanceof Exit) {
                    Exit exit = (Exit) state;
                    exit.exit();
                    return;
                }

                // clear reactions, then add reactions for next state
                configMessage.clearReactions().complete();
                for (String reaction : state.getValidReactions().keySet()) {
                    configMessage.addReaction(reaction).queue();
                }

                configMessage.editMessage(state.getMessageEmbed()).queue();
            }
        }

        public Message getConfigMessage() {
            return configMessage;
        }
    }
}