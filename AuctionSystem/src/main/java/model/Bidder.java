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
}