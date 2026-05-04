package dao;
import config.DatabaseConnection;
import model.AutoBidConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AutoBidDAO {
    
    /**
     * Lấy danh sách các cấu hình Auto-bid đang hoạt động của một phiên đấu giá.
     * @param auctionId Mã phiên đấu giá
     * @return Danh sách các đối tượng AutoBidConfig
     */
    public List<AutoBidConfig> getActiveConfigs(int auctionId) {
        List<AutoBidConfig> configs = new ArrayList<>();
        String query = "SELECT * FROM AUTO_BID_CONFIGS WHERE auction_id = ? AND is_active = TRUE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, auctionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AutoBidConfig config = new AutoBidConfig(
                        rs.getInt("config_id"),
                        rs.getInt("auction_id"),
                        rs.getInt("user_id"),
                        rs.getDouble("max_bid_amount"),
                        rs.getDouble("bid_increment"),
                        rs.getTimestamp("created_at")
                    );
                    configs.add(config);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn cấu hình AutoBid: " + e.getMessage());
        }
        
        return configs;
    }
}
