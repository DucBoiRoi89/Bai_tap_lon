import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    // 1. Lấy danh sách sản phẩm từ Database
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Item item = mapResultSetToItem(rs);
                if (item != null) items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn danh sách: " + e.getMessage());
        }
        return items;
    }

    // 2. Lưu sản phẩm mới (Sử dụng Polymorphism để xử lý trường đặc thù)
    public boolean saveItem(Item item, String sellerId) {
        String sql = "INSERT INTO items (item_id, item_name, description, category, start_price, " +
                     "current_price, end_time, seller_id, license_plate, mileage, brand, " +
                     "warranty_months, artist, year_created) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getDescription());
            pstmt.setString(4, item.getCategory());
            pstmt.setDouble(5, item.getStartPrice());
            pstmt.setDouble(6, item.getCurrentPrice());
            pstmt.setTimestamp(7, Timestamp.valueOf(item.getEndTime()));
            pstmt.setString(8, sellerId);

            // Mặc định set null cho các trường đặc thù
            for (int i = 9; i <= 14; i++) pstmt.setNull(i, Types.NULL);

            if (item instanceof Vehicle v) {
                pstmt.setString(9, v.getLicensePlate());
                pstmt.setLong(10, v.getMileage());
            } else if (item instanceof Electronics e) {
                pstmt.setString(11, e.getBrand()); 
                pstmt.setInt(12, e.getWarrantyMonths());
            } else if (item instanceof Art a) {
                pstmt.setString(13, a.getArtist());
                pstmt.setInt(14, a.getYearCreated());
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. Tìm sản phẩm theo ID (Lấy giá realtime từ DB)
    public Item findById(String itemId) {
        String sql = "SELECT * FROM items WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToItem(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 4. Cập nhật giá hiện tại
    public boolean updateCurrentPrice(String itemId, double newPrice) {
        String sql = "UPDATE items SET current_price = ? WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newPrice);
            pstmt.setString(2, itemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // 5. Cập nhật thời gian kết thúc (Cho Anti-sniping)
    public boolean updateEndTime(String itemId, LocalDateTime newEndTime) {
        String sql = "UPDATE items SET end_time = ? WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(newEndTime));
            pstmt.setString(2, itemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 6. Chuyển đổi ResultSet sang Đối tượng (SỬ DỤNG FACTORY)
    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        // Gọi Factory để tạo Object (Vehicle, Art, hoặc Electronics)
        Item item = ItemFactory.createItem(rs);

        if (item != null) {
            // Giá hiện tại luôn cập nhật từ DB
            item.updatePrice(rs.getDouble("current_price"));
            
            // Cập nhật trạng thái nếu có
            try {
                String statusStr = rs.getString("status");
                if (statusStr != null) {
                    item.setStatus(AuctionStatus.valueOf(statusStr));
                }
            } catch (Exception e) {
                // Bỏ qua nếu cột status chưa có trong DB
            }
        }
        return item;
    }
}