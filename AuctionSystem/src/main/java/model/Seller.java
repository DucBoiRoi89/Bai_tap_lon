package model;

public class Seller extends User { 

    public Seller(int userId, String username, String password, String role) { // Constructor cập nhật
        super(userId, username, password, role );
        setRole("SELLER"); 
    }

    @Override
    public String getDashboardType() {
        return "MyProductsView.fxml";
    }
    // Phương thức getReputationScore() đã được loại bỏ
}