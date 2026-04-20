import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp DatabaseConnection áp dụng Singleton Pattern.
 * Đảm bảo chỉ có một kết nối duy nhất tới cơ sở dữ liệu SQL.
 */
public class DatabaseConnection {
    // 1. Các thông số cấu hình Database (Thay đổi theo máy của bạn)
    private static final String URL = "jdbc:mysql://localhost:3306/auction_db"; // Tên DB là auction_db
    private static final String USER = "root";       // Username mặc định của MySQL
    private static final String PASSWORD = "admin";   // Mật khẩu MySQL của bạn

    // 2. Biến static để lưu trữ instance duy nhất của Connection
    private static Connection connection = null;

    // 3. Private constructor để ngăn việc khởi tạo đối tượng từ bên ngoài bằng lệnh 'new'
    private DatabaseConnection() {}

    /**
     * Phương thức public static để lấy kết nối.
     * Có từ khóa 'synchronized' để đảm bảo an toàn khi chạy đa luồng (Multi-threading).
     */
    public static synchronized Connection getConnection() throws SQLException {
        // Kiểm tra nếu kết nối chưa được tạo hoặc đã bị đóng
        if (connection == null || connection.isClosed()) {
            try {
                // Đăng ký JDBC Driver cho MySQL (Phiên bản mới)
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Khởi tạo kết nối
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("--- Kết nối Database thành công! ---");
                
            } catch (ClassNotFoundException e) {
                System.err.println("Lỗi: Không tìm thấy Driver MySQL!");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("Lỗi: Không thể kết nối tới SQL. Hãy kiểm tra URL/User/Pass!");
                throw e;
            }
        }
        return connection;
    }

    /**
     * Phương thức đóng kết nối khi tắt Server.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("--- Đã đóng kết nối Database an toàn. ---");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}