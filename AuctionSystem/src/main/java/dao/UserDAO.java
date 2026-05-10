package dao;

import config.DatabaseConnection;
import model.User;
import model.UserFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserDAO {
    public User login(String username, String password) {
        String sql = "SELECT * FROM USERS WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ps.setString(2, password); 
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("user_id");
                    String fullName = rs.getString("full_name");
                    String role = rs.getString("role");
                    
                    Map<String, Object> details = new HashMap<>();
                    if ("BIDDER".equals(role)) details.put("balance", 0.0);
                    else if ("SELLER".equals(role)) {
                        details.put("shopName", fullName);
                        details.put("reputationScore", 5.0);
                    }
                
                    return UserFactory.createUser(role, id, username, password, fullName, details);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi đăng nhập: " + e.getMessage());
        }
        return null;
    }
}