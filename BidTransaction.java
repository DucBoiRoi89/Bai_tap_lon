import java.time.LocalDateTime;

class BidTransaction {
    private String bidderId;
    private String itemId;
    private double amount;
    private LocalDateTime timestamp;

    public BidTransaction(String bidderId, String itemId, double amount) {
        this.bidderId = bidderId;
        this.itemId = itemId;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    public double getAmount() { return amount; }
    public String getBidderId() { return bidderId; }
    public String getItemId() { return itemId; }
    public LocalDateTime getTimestamp() { 
        return timestamp;
    }
}