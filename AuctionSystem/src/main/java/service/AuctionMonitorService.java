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
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Integer> expiredAuctionIds = auctionDAO.getExpiredAuctions();
                
                for (int auctionId : expiredAuctionIds) {
                    auctionDAO.updateAuctionStatus(auctionId, "FINISHED");
                    int winnerId = auctionDAO.getHighestBidderId(auctionId);
                    
                    String message = (winnerId != -1) 
                        ? "Phiên đấu giá #" + auctionId + " đã kết thúc. Người thắng: User #" + winnerId
                        : "Phiên đấu giá #" + auctionId + " đã kết thúc mà không có người mua.";
                        
                    AuctionServer.broadcast(new AuctionEvent(AuctionEvent.Type.AUCTION_FINISHED, auctionId, message));
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
        }
    }
}