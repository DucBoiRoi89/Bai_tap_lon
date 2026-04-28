import java.sql.Connection;
import java.sql.SQLException ;

public class AuctionService {
    private ItemDAO itemDao ;
    private BidDAO bidDao = BidDAO.getInstance() ;

    public synchronized boolean processBid(BidTransaction newBid) throws AuctionBaseException {
        Connection conn = null ;
        try {
            // Lấy kết nối trực tiếp từ phương thức static
            conn = DatabaseConnection.getConnection() ;

            // Tắt auto-commit để bắt đầu Transaction an toàn
            conn.setAutoCommit(false) ;

            // Logic nghiệp vụ
            Item currentItem = itemDao.findById(newBid.getItemId()) ;
            
            // Kiểm tra phiên có đang mở không 
            if (currentItem.getStatus() != AuctionStatus.RUNNING) {
                throw new AuctionClosedException( "Không thể đặt giá! Phiên đấu giá đang ở trạng thái: " + currentItem.getStatus() ) ;
            }

            // Kiểm tra giá 
            double currentHighestPrice = currentItem.getCurrentPrice() ;
            if (newBid.getAmount() <= currentHighestPrice) {
                throw new InvalidBidException("Giá đặt phải lớn hơn " + currentHighestPrice) ;
            }

            // Cập nhật giá trên Object Item
            currentItem.updatePrice(newBid.getAmount()) ;

            // Cập nhật giá xuống Database
            boolean isPriceUpdated = itemDao.updateCurrentPrice(conn, currentItem.getId(), newBid.getAmount()) ;
            boolean isBidSaved = bidDao.saveBid(newBid, conn) ;

            if (!isPriceUpdated || !isBidSaved) {
                // Nếu 1 trong 2 thất bại, chủ động ném lỗi để nhảy xuống catch và rollback
                throw new SQLException("Cập nhật Database thất bại không rõ nguyên nhân.") ;
            }

            // Nếu mọi thứ trót lọt, xác nhận (commit) Transaction
            conn.commit() ;
            return true ;
            
        } catch (SQLException e) {
            // Nếu có lỗi (DB chết, đứt mạng, lỗi SQL...) -> rollback lại toàn bộ 
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Đã rollback giao dịch đặt giá để tránh sai lệch dữ liệu.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new DatabaseException("Lỗi hệ thống khi xử lý giao dịch đặt giá", e) ;
            
        } finally {
            // Trả lại cài đặt gốc và đóng kết nối
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}