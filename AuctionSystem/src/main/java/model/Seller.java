package model;

public class Seller extends Bidder { // Seller kế thừa từ Bidder để có thể đấu giá (có balance)
    // reputationScore đã được loại bỏ theo yêu cầu

    public Seller(int userId, String username, String password, String email, double balance) { // Constructor cập nhật
        super(userId, username, password, email, balance); // Gọi constructor của Bidder
        setRole("SELLER"); // Đảm bảo vai trò vẫn là SELLER, vì Bidder constructor sẽ set là BIDDER
    }

    @Override
    public String getDashboardType() {
        return "MyProductsView.fxml";
    }
    // Phương thức getReputationScore() đã được loại bỏ
}