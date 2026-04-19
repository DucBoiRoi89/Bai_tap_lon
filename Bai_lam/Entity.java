import java.time.LocalDateTime;

/**
 * Lớp trừu tượng cơ sở để định nghĩa các thuộc tính chung.
 * Thể hiện tính Abstraction và Reusability.
 */
abstract class Entity {
    private String id;
    private String name;
    private LocalDateTime createdAt;

    public Entity(String id, String name) {
        this.id = id;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}