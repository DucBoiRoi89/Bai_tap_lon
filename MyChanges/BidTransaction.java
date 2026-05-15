package model;

public class BidTransaction {
    private String bidderId;
    private String itemId;
    private double amount;

    public BidTransaction(String bidderId, String itemId, double amount) {
        this.bidderId = bidderId;
        this.itemId = itemId;
        this.amount = amount;
    }

    public String getBidderId() { return bidderId; }
    public String getItemId() { return itemId; }
    public double getAmount() { return amount; }
}
