import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class BidDAO {
    private static BidDAO instance;

    private BidDAO() {}

    public static BidDAO getInstance() {
        if (instance == null) {
            instance = new BidDAO();
        }
        return instance;
    }

    // Hàm lưu lịch sử đấu giá (Chỉ thao tác với bảng bids)
    public boolean saveBid(BidTransaction bid, Connection conn) throws DatabaseException {
        String sql = "INSERT INTO bids (bidder_id, item_id, amount, timestamp) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bid.getBidderId()); 
            pstmt.setString(2, bid.getItemId());
            pstmt.setDouble(3, bid.getAmount());
            pstmt.setTimestamp(4, Timestamp.valueOf(bid.getTimestamp()));

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Lỗi truy xuất cơ sở dữ liệu khi lưu lịch sử đấu giá", e);
        }
    }
}