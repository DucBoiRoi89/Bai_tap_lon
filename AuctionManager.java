import java.util.HashMap;
import java.util.Map;

public class AuctionManager {
    // Singleton instance
    private static AuctionManager instance;
    // Quản lý các phiên đang diễn ra
    private Map<String, Auction> activeAuctions = new HashMap<>();

    private AuctionManager() {}

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    // Xử lý đấu giá đồng thời (Concurrency - Mục 3.2.2)
    public synchronized void processBid(BidTransaction bid) {
        Auction auction = activeAuctions.get(bid.getItemId());
        
        if (auction == null || auction.getItem().getStatus() != AuctionStatus.RUNNING) {
            System.out.println("Lỗi: Phiên đấu giá không tồn tại hoặc đã đóng.");
            return;
        }

        // Kiểm tra giá thầu có hợp lệ không
        if (bid.getAmount() > auction.getItem().getCurrentPrice()) {
            auction.addBid(bid);
            System.out.println("Đặt giá thành công! Giá mới: " + bid.getAmount());
            notifyBidders(auction.getItem());
        } else {
            System.out.println("Lỗi: Giá thầu phải cao hơn giá hiện tại.");
        }
    }

    // Observer Pattern: Thông báo Realtime
    public void notifyBidders(Item item) {
        System.out.println("[Realtime Update] Sản phẩm " + item.getName() + " đã có giá mới: " + item.getCurrentPrice());
    }

    public void addAuction(Auction auction) {
        activeAuctions.put(auction.getItem().getId(), auction);
    }
}