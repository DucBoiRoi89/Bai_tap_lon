import java.time.LocalDateTime;

/**
 * Lớp Art kế thừa từ Item.
 * Dùng cho các sản phẩm như tranh vẽ, tượng điêu khắc.
 */
public class Art extends Item {
    private String artist;
    private int yearCreated;

    public Art(String id, String name, double startPrice, LocalDateTime end, String artist, int yearCreated) {
        super(id, name, startPrice, end);
        this.artist = artist;
        this.yearCreated = yearCreated;
    }

    @Override
    public String toString() {
        return String.format("[Nghệ thuật] %s - Tác giả: %s (%d) - Giá hiện tại: %.2f", 
                             getName(), artist, yearCreated, getCurrentPrice());
    }

    public String getArtist() { return artist; }
    public int getYearCreated() { return yearCreated; }
}