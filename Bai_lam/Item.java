import java.time.LocalDateTime;
abstract class Item extends Entity {
    private double startPrice;
    private double currentPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;

    public Item(String id, String name, double startPrice, LocalDateTime end) {
        super(id, name);
        this.startPrice = startPrice;
        this.currentPrice = startPrice;
        this.endTime = end;
        this.status = AuctionStatus.OPEN;
    }

    // Cập nhật giá mới khi có người trả cao hơn
    public void updatePrice(double newPrice) {
        this.currentPrice = newPrice;
    }

    public double getCurrentPrice() { return currentPrice; }
    public void setStatus(AuctionStatus status) { this.status = status; }
    public AuctionStatus getStatus() { return status; }
}