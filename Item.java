import java.time.LocalDateTime;

/**
 * Lớp Item trừu tượng, kế thừa từ Entity.
 * Đảm bảo tính Encapsulation (Đóng gói) và Abstraction (Trừu tượng).
 */
public abstract class Item extends Entity {
    private double startPrice;
    private double currentPrice;
    private String description; 
    private String category;    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;

    /**
     * Constructor đầy đủ để khớp với cấu trúc khởi tạo từ ItemDAO.
     */
    public Item(String id, String name, String description, String category, double startPrice, LocalDateTime endTime) {
        super(id, name);
        this.description = description;
        this.category = category;
        this.startPrice = startPrice;
        this.currentPrice = startPrice; // Mặc định giá hiện tại bằng giá khởi điểm
        this.startTime = LocalDateTime.now();
        this.endTime = endTime;
        this.status = AuctionStatus.OPEN; // Trạng thái ban đầu khi vừa tạo
    }

    // ============================================================
    // --- CÁC HÀM GETTER & SETTER (GIÚP HẾT LỖI BÁO ĐỎ) ---
    // ============================================================

    public String getDescription() { 
        return description; 
    }

    public String getCategory() { 
        return category; 
    }

    public double getStartPrice() { 
        return startPrice; 
    }

    public double getCurrentPrice() { 
        return currentPrice; 
    }
    
    /**
     * Cập nhật giá hiện tại khi có người đặt giá cao hơn.
     */
    public void updatePrice(double newPrice) { 
        this.currentPrice = newPrice; 
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Lấy thời gian kết thúc đấu giá (Dùng để kiểm tra hết hạn).
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Cập nhật thời gian kết thúc (Dùng cho thuật toán Anti-sniping).
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public AuctionStatus getStatus() { 
        return status; 
    }

    public void setStatus(AuctionStatus status) { 
        this.status = status; 
    }

    /**
     * Phương thức trừu tượng để hiển thị thông tin đặc thù của từng loại sản phẩm.
     * Sẽ được triển khai tại các lớp con (Vehicle, Electronics, Art).
     */
    public abstract void displaySpecificInfo();
    
}