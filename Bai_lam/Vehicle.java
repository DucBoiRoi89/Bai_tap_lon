import java.time.LocalDateTime;

/**
 * Lớp Vehicle kế thừa từ Item.
 * Dùng cho ô tô, xe máy...
 */
public class Vehicle extends Item {
    private String licensePlate; // Biển số xe
    private long mileage;        // Số km đã đi

    public Vehicle(String id, String name, double startPrice, LocalDateTime end, String licensePlate, long mileage) {
        super(id, name, startPrice, end);
        this.licensePlate = licensePlate;
        this.mileage = mileage;
    }

    @Override
    public String toString() {
        return String.format("[Phương tiện] %s - Biển số: %s - Odo: %d km - Giá hiện tại: %.2f", 
                             getName(), licensePlate, mileage, getCurrentPrice());
    }

    public String getLicensePlate() { return licensePlate; }
    public long getMileage() { return mileage; }
}