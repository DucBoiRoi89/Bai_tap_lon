package database;
import java.sql.*;

// import common.entities.Bidder;
// import common.entities.Seller;
// import common.entities.User;
// import common.entities.Admin;
import common.entities.*;

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
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("id");
                    String name = rs.getString("name");
                    String emailDb = rs.getString("email");
                    String pass = rs.getString("password");
                    String role = rs.getString("role");

                    User user;
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        user = new Admin(id, name, emailDb);
                    } else if ("SELLER".equalsIgnoreCase(role)) {
                        user = new Seller(id, name, emailDb);
                    } else {
                        user = new Bidder(id, name, emailDb);
                    }
                    user.setPassword(pass); // Cần khôi phục mật khẩu từ DB
                    return user;
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return null;
    }

    // THÊM: Lấy ID cao nhất để đồng bộ counter khi khởi động server
    public int getMaxUserId() {
        String sql = "SELECT MAX(CAST(id AS UNSIGNED)) FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            // Có thể bảng trống, trả về 0
        }
        return 0;
    }
}