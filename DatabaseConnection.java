public class DatabaseConnection {
    private static volatile DatabaseConnection instance;
    private final String url = "jdbc:mysql://localhost:3306/thanh"; 
    private final String user = "root"; 
    private final String password = "";

    private DatabaseConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("NOT FOUND MYSQL JDBC DRIVER", e);
        }
    }

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

    // Thay vì trả về 1 biến connection dùng chung, ta tạo kết nối mới
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}