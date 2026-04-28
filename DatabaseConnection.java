import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/auction_db" ;
    private static final String USER = "root" ;
    private static final String PASSWORD = "admin" ;

    // Load Driver một lần duy nhất khi class được nạp vào bộ nhớ bằng khối static
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver") ; 
            System.out.println("--- Load JDBC Driver thành công! ---") ;
        } catch (ClassNotFoundException e) {
            System.err.println("Lỗi: Không tìm thấy Driver MySQL!") ;
            e.printStackTrace() ;
        }
    }

    private DatabaseConnection() {}

    /**
     * Mỗi lần gọi hàm này sẽ trả về một kết nối mới 
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD) ;
    }
    
}