import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

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

    // 2. Lưu sản phẩm mới (Sửa lỗi ép kiểu và thiếu trường)
    public boolean saveItem(Item item, String sellerId) {
        // Câu lệnh SQL bao gồm tất cả các trường đặc thù của các lớp con
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

            // Xử lý đa hình: Kiểm tra item thuộc lớp con nào để lưu dữ liệu riêng
            if (item instanceof Vehicle) {
                Vehicle v = (Vehicle) item;
                pstmt.setString(9, v.getLicensePlate());
                pstmt.setLong(10, v.getMileage());
                // Các trường khác để null
                pstmt.setNull(11, Types.VARCHAR); pstmt.setNull(12, Types.INTEGER);
                pstmt.setNull(13, Types.VARCHAR); pstmt.setNull(14, Types.INTEGER);
            } else if (item instanceof Electronics) {
                Electronics e = (Electronics) item;
                pstmt.setNull(9, Types.VARCHAR); pstmt.setNull(10, Types.BIGINT);
                pstmt.setString(11, e.getBrand());
                pstmt.setInt(12, e.getWarrantyMonths());
                pstmt.setNull(13, Types.VARCHAR); pstmt.setNull(14, Types.INTEGER);
            } else if (item instanceof Art) {
                Art a = (Art) item;
                pstmt.setNull(9, Types.VARCHAR); pstmt.setNull(10, Types.BIGINT);
                pstmt.setNull(11, Types.VARCHAR); pstmt.setNull(12, Types.INTEGER);
                pstmt.setString(13, a.getArtist());
                pstmt.setInt(14, a.getYearCreated());
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. Cập nhật giá hiện tại
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

    // 4. Chuyển đổi dữ liệu từ SQL sang đối tượng Java (Sửa lỗi Constructor)
    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        String id = rs.getString("item_id");
        String name = rs.getString("item_name");
        String desc = rs.getString("description");
        String category = rs.getString("category");
        double startPrice = rs.getDouble("start_price");
        double currentPrice = rs.getDouble("current_price");
        
        Timestamp ts = rs.getTimestamp("end_time");
        LocalDateTime end = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();

        Item item = null;

        // Khởi tạo đúng lớp con với đầy đủ tham số
        if ("Vehicle".equalsIgnoreCase(category)) {
            item = new Vehicle(id, name, desc, startPrice, end, 
                               rs.getString("license_plate"), rs.getLong("mileage"));
        } else if ("Electronics".equalsIgnoreCase(category)) {
            item = new Electronics(id, name, desc, startPrice, end, 
                                   rs.getString("brand"), rs.getInt("warranty_months"));
        } else if ("Art".equalsIgnoreCase(category)) {
            item = new Art(id, name, desc, startPrice, end, 
                           rs.getString("artist"), rs.getInt("year_created"));
        }

        if (item != null) {
            item.updatePrice(currentPrice);
        }
        return item;
    }
}