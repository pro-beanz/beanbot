package pro.beanz.discord.beanbot.reactionroles.json;

public class JsonRole {
    private final long id;

    public JsonRole(long id) {
        this.id = id;
    }

    public long getId() { return id; }

    public boolean equals(JsonRole other) {
        return id == other.id;
    }
}
