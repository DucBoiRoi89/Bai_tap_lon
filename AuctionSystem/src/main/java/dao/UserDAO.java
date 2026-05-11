package dao;

import config.DatabaseConnection;
import model.User;
import model.UserFactory;
import java.sql.*;
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
                    double balance = rs.getDouble("balance");
                    
                    Map<String, Object> details = new HashMap<>();
                    if ("BIDDER".equals(role)) details.put("balance", balance);
                    else if ("SELLER".equals(role)) details.put("shopName", fullName);
                
                    return UserFactory.createUser(role, id, username, password, fullName, details);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
    public boolean register(String username, String password, String fullName, String role) {
        String sql = "INSERT INTO USERS (username, password, full_name, role, balance) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullName);
            ps.setString(4, role);
            ps.setDouble(5, role.equals("BIDDER") ? 10000000.0 : 0.0); // Tặng 10tr cho Bidder mới
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean isUsernameExists(String username) {
        String sql = "SELECT 1 FROM USERS WHERE username = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { return false; }
    }
    public boolean updateUserFullName(int userId, String newName) {
    String sql = "UPDATE USERS SET full_name = ? WHERE user_id = ?";
    try (Connection conn = config.DatabaseConnection.getInstance().getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        ps.setString(1, newName);
        ps.setInt(2, userId);
        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        System.err.println("Lỗi khi cập nhật Profile: " + e.getMessage());
        return false;
    }
}
}