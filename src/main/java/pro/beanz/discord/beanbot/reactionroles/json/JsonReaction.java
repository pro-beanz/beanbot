package pro.beanz.discord.beanbot.reactionroles.json;

public class JsonReaction {
    public static final int EMOTE = 0;
    public static final int EMOJI = 1;

    private final int type;
    private final String data;

    public JsonReaction(int type, String data) {
        this.type = type;
        this.data = data;
    }

    public boolean isEmote() {
        return type == EMOTE;
    }

    public boolean equals(JsonReaction other) {
        return type == other.type &&
                data.equals(other.data);
    }
}
