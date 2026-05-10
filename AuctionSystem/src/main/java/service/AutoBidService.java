
package service;
import model.AutoBidConfig;
import dao.AutoBidDAO;
import java.util.List;
import java.util.PriorityQueue;

public class AutoBidService {
    private AutoBidDAO autoBidDAO = new AutoBidDAO();
    public void triggerAutoBids(int auctionId, double currentAmount, int lastBidderId, AuctionService auctionService) {
        List<AutoBidConfig> configs = autoBidDAO.getActiveConfigs(auctionId);
        if (configs.size() < 2) return; // Cần ít nhất 2 bot hoặc 1 bot đấu với người thật
        PriorityQueue<AutoBidConfig> queue = new PriorityQueue<>(configs);
        AutoBidConfig topBot = queue.poll();     
        if (topBot != null && topBot.getUserId() != lastBidderId) {
            double nextBid = currentAmount + topBot.getIncrement();
            if (nextBid <= topBot.getMaxBidAmount()) {
                try {
                    int status = auctionService.getAuctionDAO().placeSingleBid(nextBid, topBot.getUserId(), auctionId);
                    if (status == 1) {
                        core.AuctionServer.broadcast(new model.AuctionEvent(model.AuctionEvent.Type.NEW_BID, auctionId, nextBid));
                    }
                } catch (Exception e) {
                    System.err.println("Bot " + topBot.getUserId() + " đặt giá thất bại.");
                }
            }
        }
    }
}