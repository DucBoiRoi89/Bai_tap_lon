import java.time.LocalDateTime;

/**
 * Lớp Electronics kế thừa từ Item.
 * Đại diện cho các sản phẩm công nghệ, gia dụng.
 */
public class Electronics extends Item {
    private String brand;
    private int warrantyMonths; // Thời gian bảo hành tính bằng tháng

    public Electronics(String id, String name, double startPrice, LocalDateTime end, String brand, int warrantyMonths) {
        // Gọi constructor của lớp cha (Item)
        super(id, name, startPrice, end);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    // Ghi đè phương thức nếu cần xử lý logic riêng cho đồ điện tử
    @Override
    public String toString() {
        return String.format("[Điện tử] %s - Thương hiệu: %s - Bảo hành: %d tháng - Giá hiện tại: %.2f", 
                             getName(), brand, warrantyMonths, getCurrentPrice());
    }

    // Getters và Setters đặc thù
    public String getBrand() { return brand; }
    public int getWarrantyMonths() { return warrantyMonths; }
}