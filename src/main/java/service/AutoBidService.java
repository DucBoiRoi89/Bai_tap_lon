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
}