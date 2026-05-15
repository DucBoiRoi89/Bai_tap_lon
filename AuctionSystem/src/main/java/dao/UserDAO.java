package dao;

import config.DatabaseConnection;
import model.User;
import model.UserFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDAO {

    /**
     * Lấy danh sách toàn bộ người dùng trong hệ thống (Dành cho Admin)
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM USERS";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("user_id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String fullName = rs.getString("full_name");
                String role = rs.getString("role");
                double balance = rs.getDouble("balance");
                
                // Chuẩn bị dữ liệu bổ sung cho Factory
                Map<String, Object> details = new HashMap<>();
                details.put("balance", balance);
                // Tạo đối tượng User đúng loại (Admin/Bidder/Seller) thông qua Factory
                users.add(UserFactory.createUser(role, id, username, password, fullName, details));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách người dùng: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

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
            ps.setDouble(5, "BIDDER".equalsIgnoreCase(role) ? 50000000.0 : 0.0); 
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

    public double getUserBalance(int userId) {
        String sql = "SELECT balance FROM USERS WHERE user_id = ?";
        try (Connection conn = config.DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("balance");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }
}