package service;

import dao.AuctionDAO;
import model.AuctionEvent;
import core.AuctionServer;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionMonitorService {
    private ScheduledExecutorService scheduler;
    private AuctionDAO auctionDAO;
    public AuctionMonitorService() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        auctionDAO = new AuctionDAO();
    }
    public void startMonitoring() {
        scheduler.scheduleAtFixedRate(this::checkAndCloseAuctions, 0, 5, TimeUnit.SECONDS);
    }
    private void checkAndCloseAuctions() {
        List<Integer> expiredAuctions = auctionDAO.getExpiredAuctions();
        for (int auctionId : expiredAuctions) {
            auctionDAO.updateAuctionStatus(auctionId, "FINISHED");
            int winnerId = auctionDAO.getHighestBidderId(auctionId);
            String message = (winnerId != -1) ? "Phiên kết thúc! Người thắng là User: " + winnerId : "Phiên kết thúc! Không có ai đặt giá.";
            AuctionServer.broadcast(new AuctionEvent(AuctionEvent.Type.AUCTION_FINISHED, auctionId, message));
        }
    }

    public void stopMonitoring() {
        if (scheduler != null && !scheduler.isShutdown()) scheduler.shutdown();
    }
}