package service;
import model.AutoBidConfig;
import dao.AutoBidDAO;
import java.util.List;
import java.util.PriorityQueue;

public class AutoBidService {
    private AutoBidDAO autoBidDAO = new AutoBidDAO();
    
    public void triggerAutoBids(int auctionId, double currentAmount, int lastBidderId, AuctionService auctionService) {
        System.out.println("\n [BOT-WAR] Đang quét hệ thống tìm Robot cho phiên #" + auctionId + "...");
        List<AutoBidConfig> configs = autoBidDAO.getActiveConfigs(auctionId);
        
        if (configs == null || configs.isEmpty()) {
            System.out.println(" [BOT-WAR] Không tìm thấy Robot nào đang được kích hoạt cho phiên này.");
            return; 
        }
        
        System.out.println(" [BOT-WAR] TÌM THẤY " + configs.size() + " ROBOT ĐANG CANH GIÁ!");
        PriorityQueue<AutoBidConfig> queue = new PriorityQueue<>(configs);
        
        while (!queue.isEmpty()) {
            AutoBidConfig bot = queue.poll();
            System.out.println("   -> Đang xét Robot ID: " + bot.getUserId() + " | Giới hạn MAX: " + bot.getMaxBidAmount() + " | Bước giá: " + bot.getIncrement());
            
            if (bot.getUserId() == lastBidderId) {
                System.out.println("      (Robot này vừa mới đặt giá nên bỏ qua)");
                continue;
            }
            
            double nextBid = currentAmount + bot.getIncrement();
            System.out.println("      + Tính toán giá tiếp theo: " + nextBid);
            
            if (nextBid <= bot.getMaxBidAmount()) {
                System.out.println("      + QUYẾT ĐỊNH ĐÈ GIÁ! Gửi yêu cầu đặt " + nextBid + "...");
                try {
                    // Kích hoạt đệ quy "Bot-War"
                    auctionService.processBid(auctionId, bot.getUserId(), nextBid);
                    System.out.println("      + ROBOT " + bot.getUserId() + " ĐÃ ĐÈ GIÁ THÀNH CÔNG!");
                    return; // Thành công 1 hit thì thoát ra để vòng đệ quy mới từ processBid tự xử lý tiếp
                } catch (Exception e) {
                    System.err.println("      + LỖI: Robot " + bot.getUserId() + " đặt giá thất bại: " + e.getMessage());
                }
            } else {
                System.out.println("      + BỎ CUỘC: Mức giá mới (" + nextBid + ") đã vượt quá giới hạn tài chính của Robot.");
            }
        }
    }
}