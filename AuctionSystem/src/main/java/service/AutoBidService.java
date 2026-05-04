<<<<<<< HEAD:src/main/java/service/AutoBidService.java
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
=======
package service;
import model.AutoBidConfig;
import dao.AutoBidDAO;
import dao.AuctionDAO;
import model.AuctionEvent;
import core.AuctionServer;
import java.util.List;
import java.util.PriorityQueue;
public class AutoBidService {
    private AutoBidDAO autoBidDAO = new AutoBidDAO();
    private AuctionDAO auctionDAO = new AuctionDAO();
    public void triggerAutoBids(int auctionId, double currentAmount, int lastBidderId) {
        List<AutoBidConfig> configs = autoBidDAO.getActiveConfigs(auctionId);
        if (configs.isEmpty()) return;
        PriorityQueue<AutoBidConfig> queue = new PriorityQueue<>(configs);

        while (!queue.isEmpty()) {
            AutoBidConfig bot = queue.poll();
            if (bot.getUserId() != lastBidderId) {
                double nextBid = currentAmount + bot.getIncrement();
                if (nextBid <= bot.getMaxBidAmount()) {
                    int status = auctionDAO.placeSingleBid(nextBid, bot.getUserId(), auctionId);                  
                    if (status == 1) {
                        System.out.println("Bot (User " + bot.getUserId() + ") đã đặt giá thành công: " + nextBid);
                        AuctionServer.broadcast(new AuctionEvent(AuctionEvent.Type.NEW_BID, auctionId, nextBid));
                        break; 
                    }
                }
            }
        }
    }
>>>>>>> 9dae7d294058ef9a9d4806967b3a4466fd0dd667:AuctionSystem/src/main/java/service/AutoBidService.java
}