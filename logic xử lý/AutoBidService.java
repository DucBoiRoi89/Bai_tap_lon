package com.uet.auction.service;

import com.uet.auction.model.AutoBidConfig;
import com.uet.auction.dao.AutoBidDAO;
import com.uet.auction.dao.AuctionDAO;
import com.uet.auction.model.AuctionEvent;
import com.uet.auction.core.AuctionServer;
import java.util.List;
import java.util.PriorityQueue;

public class AutoBidService {
    private AutoBidDAO autoBidDAO = new AutoBidDAO();
    private AuctionDAO auctionDAO = new AuctionDAO();

    /**
     * Kích hoạt chuỗi đặt giá tự động dựa trên mức giá hiện tại.
     */
    public void triggerAutoBids(int auctionId, double currentAmount, int lastBidderId) {
        // Lấy danh sách bot đang hoạt động cho phiên này
        List<AutoBidConfig> configs = autoBidDAO.getActiveConfigs(auctionId);
        if (configs.isEmpty()) return;

        // Đưa vào hàng đợi ưu tiên theo thời gian tạo (createdAt)
        PriorityQueue<AutoBidConfig> queue = new PriorityQueue<>(configs);

        while (!queue.isEmpty()) {
            AutoBidConfig bot = queue.poll();

            // Bot chỉ đặt giá nếu nó không phải là người đang giữ giá cao nhất
            if (bot.getUserId() != lastBidderId) {
                double nextBid = currentAmount + bot.getIncrement();

                // Kiểm tra giới hạn ngân sách của bot
                if (nextBid <= bot.getMaxBidAmount()) {
                    // Thực hiện đặt giá thông qua DAO
                    int status = auctionDAO.placeSingleBid(nextBid, bot.getUserId(), auctionId);
                    
                    if (status == 1) {
                        System.out.println("Bot (User " + bot.getUserId() + ") đã đặt giá thành công: " + nextBid);
                        // Thông báo giá mới cho toàn bộ Client
                        AuctionServer.broadcast(new AuctionEvent(AuctionEvent.Type.NEW_BID, auctionId, nextBid));
                        
                        // Sau khi bot đặt giá thành công, nó trở thành lastBidderId. 
                        // Chúng ta thoát ra để tránh việc 1 bot tự đấu với chính mình vô hạn.
                        break; 
                    }
                }
            }
        }
    }
}