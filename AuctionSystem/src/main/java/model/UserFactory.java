package model;
import java.util.Map;

public class UserFactory {
    public static User createUser(String role, int id, String username, String password, String email, Map<String, Object> details) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Quyền người dùng không được để trống!");
        }
        
        switch (role.toUpperCase()) {
            case "BIDDER":
                double balance = details.containsKey("balance") ? ((Number) details.get("balance")).doubleValue() : 0.0;
                return new Bidder(id, username, password, email, balance);
                
            case "SELLER":
                double sellerBalance = details.containsKey("balance") ? ((Number) details.get("balance")).doubleValue() : 0.0;
                return new Seller(id, username, password, email, sellerBalance);
                
            case "ADMIN":
                return new Admin(id, username, password, email);
                
            default:
                throw new IllegalArgumentException("Vai trò không hợp lệ: " + role);
        }
    }
}