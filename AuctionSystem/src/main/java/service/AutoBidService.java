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
        // FIX: Thêm Comparator cho PriorityQueue để ưu tiên xử lý Bot có ngân sách (MaxBid) cao nhất,
        // đồng thời tránh lỗi sập ngầm ClassCastException nếu AutoBidConfig chưa cài đặt Comparable.
        PriorityQueue<AutoBidConfig> queue = new PriorityQueue<>((b1, b2) -> Double.compare(b2.getMaxBidAmount(), b1.getMaxBidAmount()));
        queue.addAll(configs);
        
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
                System.out.println("      + QUYẾT ĐỊNH ĐÈ GIÁ! Đang lên lịch đặt " + nextBid + "...");
                
                final int botId = bot.getUserId();
                final double targetBid = nextBid;
                
                // FIX: Chuyển tiến trình đấu giá của Bot sang một Luồng (Thread) độc lập.
                // Tránh lỗi đệ quy StackOverflow, đồng thời thêm độ trễ 0.5s để UI hiển thị nảy số chân thực.
                new Thread(() -> {
                    try {
                        Thread.sleep(500); // Tạm dừng 0.5 giây tạo hiệu ứng suy nghĩ
                        auctionService.processBid(auctionId, botId, targetBid);
                        System.out.println("      + ROBOT " + botId + " ĐÃ ĐÈ GIÁ THÀNH CÔNG LÊN " + targetBid + "!");
                    } catch (Exception e) {
                        System.err.println("      + LỖI: Robot " + botId + " đặt giá thất bại: " + e.getMessage());
                    }
                }).start();
                
                return; // Ngắt vòng lặp ngay để chờ luồng Thread phía trên chạy
            } else {
                System.out.println("      + BỎ CUỘC: Mức giá mới (" + nextBid + ") đã vượt quá giới hạn tài chính của Robot.");
            }
        }
    }
}