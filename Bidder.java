package model;

public class Bidder extends User {
    private double balance; 

    public Bidder(int userId, String username, String password, String email, double balance) {
        super(userId, username, password, email, "BIDDER");
        this.balance = balance;
    }

    @Override
    public String getDashboardType() {
        return "BiddingView.fxml";
    }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public boolean placeBid(Item item, BidTransaction bid) {
        if (bid == null || item == null) return false;
        if (bid.getAmount() <= item.getCurrentPrice()) return false;

        // Cập nhật giá hiện tại của item
        item.setCurrentPrice(bid.getAmount());
        return true;
    }
}