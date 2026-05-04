package model;
public class ClientRequest {
    private String action;
    private int auctionId;
    private int userId;
    private double amount;
    public ClientRequest(String action, int auctionId, int userId, double amount) {
        this.action = action;
        this.auctionId = auctionId;
        this.userId = userId;
        this.amount = amount;
    }
    public String getAction() { return action; }
    public int getAuctionId() { return auctionId; }
    public int getUserId() { return userId; }
    public double getAmount() { return amount; }
}