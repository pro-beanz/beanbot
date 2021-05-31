package pro.beanz.discord.beanbot.reactionroles.json;

import java.util.HashMap;
import java.util.Map;

public class JsonMessage {
    private final Long channelId;
    private final Long messageId;
    private final Map<JsonReaction, JsonRole> pairs;
    
    public JsonMessage(Long channelId, Long messageId) {
        this.channelId = channelId;
        this.messageId = messageId;
        this.pairs = new HashMap<>();
    }

    public JsonMessage(Long channelId, Long messageId, Map<JsonReaction, JsonRole> pairs) {
        this.channelId = channelId;
        this.messageId = messageId;
        this.pairs = pairs;
    }

    public Long getChannelId() { return channelId; }

    public Long getMessageId() { return messageId; }

    public Map<JsonReaction, JsonRole> getPairs() { return pairs; }

    public JsonRole getRole(JsonReaction reaction) {
        for (JsonReaction jsonReaction : pairs.keySet()) {
            if (reaction.equals(jsonReaction)) {
                return pairs.get(jsonReaction);
            }
        }
        return null;
    }

    public boolean addReactionRole(JsonReaction reaction, JsonRole role) {
        JsonRole oldRole = this.pairs.put(reaction, role);
        if (!(oldRole == null || oldRole.equals(role))) {
            // reverts changes and  returns false if the map
            // contains a different reaction/role pair already
            this.pairs.put(reaction, oldRole);
            return false;
        }
        return true;
    }

    public void removeReactionRole(JsonReaction reaction ) {
        if (containsReaction(reaction)) {
            for (JsonReaction r : pairs.keySet()) {
                if (reaction.equals(r)){
                    pairs.remove(r);
                }
            }
        }
    }

    public boolean containsReaction(JsonReaction reaction) {
        for (JsonReaction r : pairs.keySet())
            if (reaction.equals(r))
                return true;
        return false;
    }
}
