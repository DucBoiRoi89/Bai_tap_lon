package model;

public class Seller extends User {
    private String shopName;
    private double reputationScore; 

    public Seller(int userId, String username, String password, String email, String shopName, double reputationScore) {
        super(userId, username, password, email, "SELLER");
        this.shopName = shopName;
        this.reputationScore = reputationScore;
    }

    @Override
    public String getDashboardType() {
        return "Bảng điều khiển Người bán - Quản lý sản phẩm và doanh thu";
    }

    public String getShopName() { return shopName; }
    public double getReputationScore() { return reputationScore; }
}