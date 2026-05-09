package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp DatabaseConnection áp dụng Singleton Pattern với Double-Checked Locking.
 * Đảm bảo an toàn đa luồng và hiệu suất cao.
 */
public class DatabaseConnection {
    // 1. Dùng volatile để đảm bảo các luồng luôn thấy giá trị mới nhất của instance
    private static volatile DatabaseConnection instance;
    private static Connection connection = null;

    // Các thông số cấu hình - Bạn hãy điều chỉnh cho khớp với máy của mình
    private final String url = "jdbc:mysql://localhost:3306/thanh"; 
    private final String user = "root"; 
    private final String password = ""; // Hoặc "admin" tùy máy bạn

    // 2. Private constructor ngăn việc khởi tạo bằng từ khóa 'new'
    private DatabaseConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Lỗi: Không tìm thấy Driver MySQL JDBC!");
            throw new SQLException("NOT FOUND MYSQL JDBC DRIVER", e);
        }
    }

    // 3. Phương thức getInstance theo cơ chế Double-Checked Locking
    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Lấy kết nối tới Database.
     * Kiểm tra và khởi tạo lại nếu kết nối đã bị đóng.
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("--- Kết nối Database thành công! ---");
            } catch (SQLException e) {
                System.err.println("Lỗi: Không thể kết nối tới SQL. Hãy kiểm tra URL/User/Pass!");
                throw e;
            }
        }
        return connection;
    }

    /**
     * Phương thức đóng kết nối an toàn khi tắt Server.
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