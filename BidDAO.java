// import java.sql.*;

// public class BidDAO {
//     // Lưu một lượt đặt giá mới
//     public void saveBid(BidTransaction bid) {
//         String sql = "INSERT INTO bids (bid_id, bidder_id, item_id, amount, timestamp) VALUES (?, ?, ?, ?, ?)";
//         try (Connection conn = DatabaseConnection.getConnection();
//              PreparedStatement pstmt = conn.prepareStatement(sql)) {
//             pstmt.setString(1, bid.getBidId());
//             pstmt.setString(2, bid.getBidderId());
//             pstmt.setString(3, bid.getItemId());
//             pstmt.setDouble(4, bid.getAmount());
//             pstmt.setTimestamp(5, Timestamp.valueOf(bid.getTimestamp()));
            
//             // Sử dụng Transaction để đảm bảo tính nhất quán (Atomic)
//             conn.setAutoCommit(false);
//             pstmt.executeUpdate();
            
//             // Cập nhật luôn giá hiện tại của sản phẩm trong bảng items
//             String updateItemSql = "UPDATE items SET current_price = ? WHERE id = ?";
//             PreparedStatement pstmt2 = conn.prepareStatement(updateItemSql);
//             pstmt2.setDouble(1, bid.getAmount());
//             pstmt2.setString(2, bid.getItemId());
//             pstmt2.executeUpdate();
            
//             conn.commit();
//         } catch (SQLException e) {
//             e.printStackTrace();
//         }
//     }
// }