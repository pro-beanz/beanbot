package pro.beanz.reactionroles.json;

public class JsonRole {
    private final Long id;

    public JsonRole(Long id) {
        this.id = id;
    }

    public long getId() { return id; }

    public boolean equals(JsonRole other) {
        return id == other.id;
    }
}
