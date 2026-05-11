package service;

import dao.AuctionDAO;
import model.AuctionEvent;
import core.AuctionServer;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionMonitorService {
    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void startMonitoring() {
        // Quét Database mỗi 5 giây để tìm sản phẩm hết hạn
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Integer> expiredItemIds = auctionDAO.getExpiredAuctions();
                
                for (int itemId : expiredItemIds) {
                    // 1. Chốt trạng thái CLOSED
                    auctionDAO.updateAuctionStatus(itemId, "CLOSED");
                    
                    // 2. Tìm người thắng
                    int winnerId = auctionDAO.getHighestBidderId(itemId);
                    
                    // 3. Thông báo Realtime cho mọi người
                    String message = (winnerId != -1) 
                        ? "Sản phẩm #" + itemId + " đã kết thúc. Người thắng: User #" + winnerId
                        : "Sản phẩm #" + itemId + " đã kết thúc mà không có người mua.";
                        
                    AuctionServer.broadcast(new AuctionEvent(AuctionEvent.Type. AUCTION_FINISHED, itemId, message));
                    
                    System.out.println("[MONITOR] " + message);
                }
            } catch (Exception e) {
                System.err.println("[MONITOR ERROR] " + e.getMessage());
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
public void stopMonitoring() {
    if (scheduler != null && !scheduler.isShutdown()) {
        scheduler.shutdown();
        System.out.println("[MONITOR] Đã dừng bộ giám sát phiên đấu giá.");
    }
}
}