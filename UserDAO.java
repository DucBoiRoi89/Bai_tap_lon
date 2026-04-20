import java.sql.*;

public class UserDAO {
    // Lưu người dùng mới (Đăng ký)
    public boolean saveUser(User user) {
        String sql = "INSERT INTO users (id, name, email, password, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPassword());
            
            // Xác định role dựa trên instance của đối tượng
            String role = "BIDDER";
            if (user instanceof Seller) role = "SELLER";
            else if (user instanceof Admin) role = "ADMIN";
            
            pstmt.setString(5, role);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Lấy người dùng theo Email (Đăng nhập)
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String emailDb = rs.getString("email");
                String pass = rs.getString("password");
                String role = rs.getString("role");

                // KHỞI TẠO ĐÚNG LOẠI ĐỐI TƯỢNG VỚI 4 THAM SỐ
                if ("ADMIN".equalsIgnoreCase(role)) {
                    return new Admin(id, name, emailDb);
                } else if ("SELLER".equalsIgnoreCase(role)) {
                    return new Seller(id, name, emailDb);
                } else {
                    return new Bidder(id, name, emailDb);
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return null;
    }
}