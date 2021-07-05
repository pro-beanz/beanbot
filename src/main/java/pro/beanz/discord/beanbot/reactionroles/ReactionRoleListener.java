package pro.beanz.discord.beanbot.reactionroles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.beanz.discord.beanbot.BeanbotHelper;
import pro.beanz.discord.beanbot.reactionroles.json.JsonMessage;
import pro.beanz.discord.beanbot.reactionroles.json.JsonReaction;
import pro.beanz.discord.beanbot.reactionroles.json.JsonRole;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReactionRoleListener extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ReactionRoleListener.class);

    private final File cacheFile = new File("./data/reaction_role_cache.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting()
            .enableComplexMapKeySerialization().create();

    private final Type type = new TypeToken<Map<Long, JsonMessage>>() {
    }.getType();
    private Map<Long, JsonMessage> map;

    public ReactionRoleListener() {
        super();

        reloadCache();

        log.info("Ready");
    }

    public void reloadCache() {
        try {
            try (InputStreamReader in = new InputStreamReader(new FileInputStream(cacheFile), StandardCharsets.UTF_16)) {
                map = gson.fromJson(in, type);
                if (map == null) {
                    map = new HashMap<>();
                }
            } catch (FileNotFoundException e) {
                if (!cacheFile.createNewFile()) {
                    BeanbotHelper.insufficientFilePrivileges();
                }
                map = new HashMap<>();
            } catch (JsonSyntaxException e) {
                // create new backup dir if one does not yet exist
                File backupDir = new File("./data/backup");
                if (!backupDir.exists()) {
                    if (!backupDir.mkdir()) {
                        BeanbotHelper.insufficientFilePrivileges();
                    }
                }

                // backup file and replace with an empty one
                Files.move(cacheFile.toPath(), Paths.get(String.format(
                        "data/backup/%s_%s", cacheFile.getName(), new Date().toString().replaceAll(" ", "-"))));
                if (!cacheFile.createNewFile()) {
                    BeanbotHelper.insufficientFilePrivileges();
                }
                map = new HashMap<>();
            }
            writeCache();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeCache() {
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(cacheFile), StandardCharsets.UTF_16);
            writer.write(gson.toJson(map, type));
            writer.close();
        } catch (FileNotFoundException e) {
            try {
                if (!cacheFile.createNewFile()) {
                    BeanbotHelper.insufficientFilePrivileges();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // returns false if the map already contains the reaction
    public boolean addReactionRole(Long channelId, Long messageId, JsonReaction reaction, JsonRole role) {
        boolean status;
        // add the message if it doesn't already exist
        if (!map.containsKey(messageId)) {
            Map<JsonReaction, JsonRole> pairs = new HashMap<>();
            pairs.put(reaction, role);
            map.put(messageId, new JsonMessage(channelId, pairs));
            status = true;
        } else {
            // if the map contains the messageId, just add to it
            JsonMessage message = map.get(messageId);
            status = message.addReactionRole(reaction, role);
            map.put(messageId, message);
        }

        writeCache();
        return status;
    }

    public void removeReactionRole(Long messageId, JsonReaction reaction) {
        if (map.containsKey(messageId)) {
            JsonMessage message = map.get(messageId);
            message.removeReactionRole(reaction);
            map.put(messageId, message);
        }

        writeCache();
    }

    public void removeReactionRole(Long messageId) {
        map.remove(messageId);
        writeCache();
    }

    public Map<JsonReaction, JsonRole> getReactionRoles(Long messageId) {
        return map.get(messageId).getPairs();
    }

    public boolean containsReaction(Long messageId, JsonReaction reaction) {
        return map.get(messageId).containsReaction(reaction);
    }

    public boolean containsMessage(Long messageId) {
        return map.containsKey(messageId);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        // check for registered messages that were deleted while the bot was offline
        for (Long messageId : map.keySet()) {
            TextChannel channel = event.getJDA().getTextChannelById(map.get(messageId).getChannelId());
            assert channel != null;
            MessageHistory history = MessageHistory.getHistoryAround(channel, messageId + "").complete();
            if (history.getMessageById(messageId) == null) {
                removeReactionRole(messageId);
            }
        }
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        // automatically unregisters reaction role messages when they're deleted
        Long messageId = event.getMessageIdLong();
        map.remove(messageId);

        writeCache();
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        Long messageId = event.getMessageIdLong();
        if (map.containsKey(messageId)) {
            JsonReaction reaction;
            ReactionEmote reactionEmote = event.getReactionEmote();
            if (reactionEmote.isEmote()) {
                reaction = new JsonReaction(JsonReaction.EMOTE, reactionEmote.getEmote().getId());
            } else {
                reaction = new JsonReaction(JsonReaction.EMOJI, reactionEmote.getEmoji());
            }

            JsonMessage message = map.get(messageId);
            if (message.containsReaction(reaction)) {
                event.getGuild().addRoleToMember(event.getMember(),
                        Objects.requireNonNull(event.getGuild().getRoleById(message.getRole(reaction).getId()))).queue();
            }
        }
    }

    @Override
    public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
        Long messageId = event.getMessageIdLong();
        if (map.containsKey(messageId)) {
            JsonReaction reaction;
            ReactionEmote reactionEmote = event.getReactionEmote();
            if (reactionEmote.isEmote()) {
                reaction = new JsonReaction(JsonReaction.EMOTE, reactionEmote.getEmote().getId());
            } else {
                reaction = new JsonReaction(JsonReaction.EMOJI, reactionEmote.getEmoji());
            }

            JsonMessage message = map.get(messageId);
            if (message.containsReaction(reaction)) {
                event.getGuild().removeRoleFromMember(event.retrieveMember().complete(),
                        Objects.requireNonNull(event.getGuild().getRoleById(message.getRole(reaction).getId()))).queue();
            }
        }
    }
}
