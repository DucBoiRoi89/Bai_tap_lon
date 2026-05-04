package service;

import model.AutoBidConfig;
import dao.AutoBidDAO;
import java.util.List;
import java.util.PriorityQueue;

public class AutoBidService {
    private AutoBidDAO autoBidDAO = new AutoBidDAO();

    public void triggerAutoBids(int auctionId, double currentAmount, int lastBidderId, AuctionService auctionService) {
        List<AutoBidConfig> configs = autoBidDAO.getActiveConfigs(auctionId);
        if (configs.isEmpty()) return;
        
        PriorityQueue<AutoBidConfig> queue = new PriorityQueue<>(configs);
        while (!queue.isEmpty()) {
            AutoBidConfig bot = queue.poll();
            if (bot.getUserId() != lastBidderId) {
                double nextBid = currentAmount + bot.getIncrement();
                if (nextBid <= bot.getMaxBidAmount()) {
                    try {
                        auctionService.processBid(auctionId, bot.getUserId(), nextBid);
                        break;
                    } catch (Exception e) {
                        System.err.println("Bot " + bot.getUserId() + " đặt giá thất bại: " + e.getMessage());
                    }
                }
            }
        }
    }
}