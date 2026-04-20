import java.time.LocalDateTime;

public class Electronics extends Item {
    private String brand;
    private int warrantyMonths;

    public Electronics(String id, String name, String description, double startPrice, 
                       LocalDateTime end, String brand, int warrantyMonths) {
        // Gọi constructor lớp cha (Item) với đầy đủ tham số để khớp với DB
        super(id, name, description, "Electronics", startPrice, end); 
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }

    /**
     * Triển khai phương thức hiển thị đặc thù (Mục 3.3.2) 
     * Giúp hệ thống in ra thông tin chi tiết tùy theo loại sản phẩm.
     */
    @Override
    public void displaySpecificInfo() {
        System.out.println("=== CHI TIẾT ĐỒ ĐIỆN TỬ ===");
        System.out.println("ID: " + getId());
        System.out.println("Sản phẩm: " + getName());
        System.out.println("Thương hiệu: " + brand);
        System.out.println("Bảo hành: " + warrantyMonths + " tháng");
        System.out.println("Giá hiện tại: " + getCurrentPrice());
        System.out.println("Trạng thái: " + getStatus());
        System.out.println("===========================");
    }

    @Override
    public String toString() {
        return String.format("[Electronics] %s (%s)", getName(), brand);
    }

    // Getters
    public String getBrand() { return brand; }
    public int getWarrantyMonths() { return warrantyMonths; }
}