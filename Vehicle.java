import java.time.LocalDateTime;

/**
 * Lớp Vehicle kế thừa từ Item.
 * Dùng cho ô tô, xe máy... 
 */
public class Vehicle extends Item {
    private String licensePlate; // Biển số xe
    private long mileage;        // Số km đã đi

    // Sửa Constructor để khớp với cấu trúc 6 tham số của Item
    public Vehicle(String id, String name, String description, double startPrice, LocalDateTime end, String licensePlate, long mileage) {
        // "Vehicle" được truyền vào tham số category [cite: 5, 113, 118]
        super(id, name, description, "Vehicle", startPrice, end);
        this.licensePlate = licensePlate;
        this.mileage = mileage;
    }

    /**
     * Triển khai phương thức trừu tượng từ lớp Item.
     * Thể hiện tính Polymorphism (Đa hình)[cite: 5, 121].
     */
    @Override
    public void displaySpecificInfo() {
        System.out.println("Biển số xe: " + licensePlate);
        System.out.println("Số km đã đi: " + mileage + " km");
    }

    @Override
    public String toString() {
        return String.format("[Phương tiện] %s - Biển số: %s - Odo: %d km - Giá hiện tại: %.2f", 
                             getName(), licensePlate, mileage, getCurrentPrice());
    }

    // --- Getters (Tính đóng gói)  ---
    public String getLicensePlate() { return licensePlate; }
    public long getMileage() { return mileage; }
    // Thêm các Setter để đủ tính Encapsulation 
    public void setLicensePlate(String licensePlate) {
        if (licensePlate == null || licensePlate.isEmpty()) {
            throw new IllegalArgumentException("Biển số không được để trống"); // Xử lý lỗi [cite: 56]
        }
        this.licensePlate = licensePlate;
    }

    public void setMileage(long mileage) {
        if (mileage < 0) {
            throw new IllegalArgumentException("Số km không thể âm");
        }
        this.mileage = mileage;
    }
}