import java.time.LocalDateTime;

/**
 * Lớp Art kế thừa từ Item[cite: 5, 113, 120].
 * Dùng cho các sản phẩm như tranh vẽ, tượng điêu khắc.
 */
public class Art extends Item {
    private String artist;
    private int yearCreated;

    // Sửa đổi Constructor để khớp với tham số của lớp cha (Item)
    public Art(String id, String name, String description, double startPrice, LocalDateTime end, String artist, int yearCreated) {
        // "Art" được truyền cố định vào tham số category của Item [cite: 5, 113]
        super(id, name, description, "Art", startPrice, end); 
        this.artist = artist;
        this.yearCreated = yearCreated;
    }

    /**
     * Triển khai phương thức trừu tượng từ lớp Item.
     * Thể hiện tính Polymorphism (Đa hình).
     */
    @Override
    public void displaySpecificInfo() {
        System.out.println("Tác giả: " + artist);
        System.out.println("Năm sáng tác: " + yearCreated);
    }

    @Override
    public String toString() {
        return String.format("[Nghệ thuật] %s - Tác giả: %s (%d) - Giá hiện tại: %.2f", 
                             getName(), artist, yearCreated, getCurrentPrice());
    }

    public String getArtist() { return artist; }
    public int getYearCreated() { return yearCreated; }
}