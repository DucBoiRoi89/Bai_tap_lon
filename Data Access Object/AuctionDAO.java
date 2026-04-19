package com.uet.auction.dao;

import com.uet.auction.config.DatabaseConnection; // Chỉ import 1 lần
import com.uet.auction.model.AutoBidConfig;
// Import lớp Item của bạn (giả sử nó nằm trong package com.uet.auction.model)
import com.uet.auction.model.Item; 
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement; // Thêm import này
import java.sql.SQLException;
import java.sql.Types;

public class AuctionDAO {

    /**
     * Gọi Stored Procedure để thực hiện đặt giá
     * @return statusCode: 1 (Thành công), 0 (Giá thấp), -1 (Phiên đóng), -2 (Lỗi SQL)
     */
    public int placeSingleBid(double bidAmount, int userId, int auctionId) {
        String sql = "{CALL PRO_PlaceSingleBid(?, ?, ?, ?)}";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            // Set các tham số đầu vào (IN)
            cstmt.setDouble(1, bidAmount);
            cstmt.setInt(2, userId);
            cstmt.setInt(3, auctionId);
            
            // Đăng ký tham số đầu ra (OUT) cho p_status_code
            cstmt.registerOutParameter(4, Types.INTEGER);
            
            cstmt.execute();
            
            // Trả về kết quả từ Procedure
            return cstmt.getInt(4);
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi thực thi Procedure PRO_PlaceSingleBid: " + e.getMessage());
            return -2; // Mã lỗi hệ thống
        }
    }

    // Đưa updateItem VÀO TRONG class
    public boolean updateItem(Item item) {
        String sql = "UPDATE ITEMS SET item_name = ?, description = ? WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, item.getName());
            
            // GIẢ ĐỊNH: Lớp Item của bạn có phương thức getDescription(). 
            // Nếu không có, bạn cần thêm nó vào lớp Item.
            // ps.setString(2, item.getDescription()); 
            ps.setString(2, "Mô tả cập nhật"); // Tạm thời dùng giá trị cứng nếu chưa có getDescription

            // Chú ý: Đảm bảo item.getId() có thể chuyển đổi an toàn sang số nguyên.
            try {
                ps.setInt(3, Integer.parseInt(item.getId())); 
            } catch(NumberFormatException e) {
                 System.err.println("Lỗi: ID của Item không phải là số hợp lệ.");
                 return false;
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }
    }

    // Đưa deleteItem VÀO TRONG class
    public boolean deleteItem(int itemId) {
        String sql = "DELETE FROM ITEMS WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            // Kiểm tra lỗi vi phạm khóa ngoại (Mã lỗi phổ biến của MySQL cho vi phạm constraint là 1451)
            if (e.getErrorCode() == 1451) {
                 System.err.println("Không thể xóa: Sản phẩm này đang được liên kết với một phiên đấu giá hoặc dữ liệu khác.");
                 // Trong môi trường thực tế, thay vì trả về false, 
                 // bạn nên ném một Custom Exception (ví dụ: ForeignConstraintViolationException)
                 // để tầng UI có thể hiển thị thông báo chính xác cho người dùng.
                 return false; 
            } else {
                 e.printStackTrace(); 
                 return false;
            }
        }
    }
}
public boolean updateItem(Item item) {
    String sqlUpdateBase = "UPDATE ITEMS SET item_name = ?, description = ? WHERE item_id = ?";
    Connection conn = null;
    
    try {
        conn = DatabaseConnection.getInstance().getConnection();
        conn.setAutoCommit(false); // Bắt đầu Transaction

        // 1. Cập nhật bảng ITEMS (Thông tin chung)
        try (PreparedStatement psBase = conn.prepareStatement(sqlUpdateBase)) {
            psBase.setString(1, item.getName());
            // Lưu ý: Bạn cần thêm field 'description' vào class Item.java nếu chưa có
            psBase.setString(2, "Mô tả sản phẩm"); 
            psBase.setInt(3, Integer.parseInt(item.getId()));
            psBase.executeUpdate();
        }

        // 2. Cập nhật bảng con tùy theo loại (Polymorphism)
        if (item instanceof Electronics) {
            updateElectronics(conn, (Electronics) item);
        } else if (item instanceof Art) {
            updateArt(conn, (Art) item);
        } else if (item instanceof Vehicle) {
            updateVehicle(conn, (Vehicle) item);
        }

        conn.commit(); // Hoàn tất nếu không có lỗi
        return true;
    } catch (SQLException e) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        }
        e.printStackTrace();
        return false;
    } finally {
        if (conn != null) {
            try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}

private void updateElectronics(Connection conn, Electronics e) throws SQLException {
    String sql = "UPDATE ELECTRONICS SET brand = ?, warranty_months = ? WHERE item_id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, e.getBrand());
        ps.setInt(2, e.getWarrantyMonths());
        ps.setInt(3, Integer.parseInt(e.getId()));
        ps.executeUpdate();
    }
}

private void updateArt(Connection conn, Art a) throws SQLException {
    String sql = "UPDATE ART SET author = ?, creation_year = ? WHERE item_id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, a.getArtist());
        ps.setInt(2, a.getYearCreated());
        ps.setInt(3, Integer.parseInt(a.getId()));
        ps.executeUpdate();
    }
}

private void updateVehicle(Connection conn, Vehicle v) throws SQLException {
    String sql = "UPDATE VEHICLES SET brand = ?, license_plate = ?, mileage = ? WHERE item_id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, v.getBrand());
        ps.setString(2, v.getLicensePlate());
        ps.setLong(3, v.getMileage());
        ps.setInt(4, Integer.parseInt(v.getId()));
        ps.executeUpdate();
    }
}
