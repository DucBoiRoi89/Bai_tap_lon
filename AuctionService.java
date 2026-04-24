import java.sql.SQLException ;

public class AuctionService {
    private ItemDAO itemDao = ItemDaoImpl.getInstance() ;

    public synchronized boolean processBid(BidTransaction newBid) throws AuctionBaseException {
    try {
        Item currentItem = itemDao.findById(newBid.getItemId()) ;
        
        // Kiểm tra phiên đã đóng chưa 
        if (currentItem.isClosed()) {
            throw new AuctionClosedException("Phiên đấu giá đã kết thúc.") ;
        }

        // Kiểm tra giá 
        double currentHighestPrice = currentItem.getCurrentHighestBid() ;
        if (newBid.getAmount() <= currentHighestPrice) {
            throw new InvalidBidException("Giá đặt phải lớn hơn " + currentHighestPrice) ;
        }

        // Cập nhật và lưu 
        currentItem.setCurrentHighestBid(newBid.getAmount()) ;
        itemDao.update(currentItem) ;
        
        return true ;
    } catch (SQLException e) {
        throw new DatabaseException("Lỗi truy xuất cơ sở dữ liệu", e) ;
    }
}
}