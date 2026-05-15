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
                String shopName = details.containsKey("shopName") ? (String) details.get("shopName") : username;
                double rating = details.containsKey("reputationScore") ? ((Number) details.get("reputationScore")).doubleValue() : 5.0;
                return new Seller(id, username, password, email, shopName, rating);
                
            case "ADMIN":
                String accessLevel = details.containsKey("accessLevel") ? (String) details.get("accessLevel") : "MODERATOR";
                return new Admin(id, username, password, email, accessLevel);
                
            default:
                throw new IllegalArgumentException("Vai trò không hợp lệ: " + role);
        }
    }
}