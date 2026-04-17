import java.time.LocalDate;

public abstract class Entity {
    protected String id;
    protected String name;
    private LocalDate createdAt;
    private int dmy;

    public Entity(String id, String name) {
        this.id = id;
        this.name = name;
        this.createdAt = LocalDate.now();
    }

    public String getId() { return id; }
    public LocalDate getCreateAt() { return createdAt; }
    public void setName(String name) { this.name = name; }
}