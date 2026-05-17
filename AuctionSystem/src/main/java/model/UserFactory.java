package model;
import java.util.Map;

public class UserFactory {
    public static User createUser(String role, int id, String username, String password, Map<String, Object> details) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Quyền người dùng không được để trống!");
        }
        
        switch (role.toUpperCase()) {
            case "BIDDER":
                double balance = details.containsKey("balance") ? ((Number) details.get("balance")).doubleValue() : 0.0;
                return new Bidder(id, username, password, balance);
                
            case "SELLER":
                return new Seller(id, username, password, role);
                
            case "ADMIN":
                return new Admin(id, username, password);
                
            default:
                throw new IllegalArgumentException("Vai trò không hợp lệ: " + role);
        }
    }
}