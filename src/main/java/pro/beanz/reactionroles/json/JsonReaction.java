package pro.beanz.reactionroles.json;

public class JsonReaction {
    public static final int EMOTE = 0;
    public static final int EMOJI = 1;

    private final int type;
    private final String data;

    public JsonReaction(int type, String data) {
        this.type = type;
        this.data = data;
    }
    
    public String getReaction() { return data; };
    
    public boolean isEmote() { return type == EMOTE; }
    
    public boolean isEmoji() { return type == EMOJI; }

    public boolean equals(JsonReaction other) {
        return type == other.type &&
            data.equals(other.data);
    }
}
