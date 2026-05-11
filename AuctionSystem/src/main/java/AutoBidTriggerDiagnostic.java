import dao.AuctionDAO;
import service.AuctionService;
import service.AutoBidService;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AutoBidTriggerDiagnostic {
    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            System.setErr(new PrintStream(System.err, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        AuctionDAO auctionDAO = new AuctionDAO();
        AuctionService auctionService = new AuctionService();

        System.out.println("======================================================");
        System.out.println("   CÔNG CỤ TỰ ĐỘNG TEST AUTO-BID (KHÔNG CẦN NHẬP TAY) ");
        System.out.println("======================================================");

        int auctionId = -1;
        int userId = -1;

        System.out.println("[*] Đang tự động tìm kiếm dữ liệu phù hợp trong Database...");
        try (Connection conn = config.DatabaseConnection.getInstance().getConnection()) {
            // 1. Tìm 1 phiên đấu giá đang mở
            try (PreparedStatement ps = conn.prepareStatement("SELECT auction_id FROM AUCTIONS WHERE status = 'RUNNING' LIMIT 1");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    auctionId = rs.getInt("auction_id");
                    System.out.println("    -> Đã tìm thấy phiên đấu giá đang mở: ID " + auctionId);
                } else {
                    System.err.println("[X] LỖI: Không có phiên đấu giá nào đang RUNNING. Hãy tạo 1 phiên trước!");
                    return;
                }
            }
            
            int highestBidderId = auctionDAO.getHighestBidderId(auctionId);
            
            // 2. Tìm 1 user (BIDDER) không phải là người đang giữ giá cao nhất để đóng vai trò Bot
            try (PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM USERS WHERE user_id != ? AND role = 'BIDDER' LIMIT 1")) {
                ps.setInt(1, highestBidderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("user_id");
                        System.out.println("    -> Đã chọn User để cài Bot: ID " + userId);
                    } else {
                        System.err.println("[X] LỖI: Không tìm thấy User phù hợp. Hệ thống cần ít nhất 1 User khác người đang giữ top 1.");
                        return;
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); return; }

        double currentPrice = auctionDAO.getCurrentMaxPrice(auctionId);
        double maxBid = currentPrice + 100000.0; // Tự cho ngân sách = giá hiện tại + 100k
        double increment = 5000.0;               // Bước giá 5k

        System.out.println("\n[*] BƯỚC 1: Xóa cấu hình cũ và lưu cấu hình Bot mới...");
        boolean isSaved = auctionDAO.saveAutoBid(auctionId, userId, maxBid, increment);
        if (!isSaved) {
            System.err.println("[X] LỖI: Không thể lưu cấu hình Bot vào Database. Hãy kiểm tra lại kết nối hoặc khoá ngoại.");
            return;
        }
        System.out.println("[+] Đã lưu cấu hình thành công!");

        System.out.println("\n[*] BƯỚC 2: Lấy thông tin giá hiện tại từ Database giống hệt Giao diện...");
        int highestBidderId = auctionDAO.getHighestBidderId(auctionId);

        System.out.println("    -> Giá hiện tại đang hiển thị: " + currentPrice + " VNĐ");
        System.out.println("    -> ID người đang nắm top 1  : " + highestBidderId);

        if (highestBidderId == userId) {
            System.out.println("\n[!] CẢNH BÁO NGUYÊN NHÂN LỖI TẠI ĐÂY:");
            System.out.println("Bạn (User " + userId + ") ĐÃ LÀ NGƯỜI GIỮ GIÁ CAO NHẤT (Top 1).");
            System.out.println("Robot Auto-bid được lập trình KHÔNG ĐƯỢC PHÉP tự nhảy giá đè lên giá của chính chủ.");
            System.out.println("Điều này giải thích tại sao khi bạn bấm nút trên Giao diện, giá không cộng dồn lên.");
            System.out.println("=> ĐỂ TEST BOT NHẢY GIÁ: Bạn phải mượn 1 tài khoản khác (User khác) đặt giá thủ công để cướp Top 1, Bot sẽ lập tức thức dậy và nhảy số.");
        } else {
            System.out.println("\n[*] BƯỚC 3: Bạn chưa phải Top 1, Kích hoạt đánh thức Bot nhảy giá ngay lập tức...");
            try {
                new AutoBidService().triggerAutoBids(auctionId, currentPrice, highestBidderId, auctionService);
                
                // FIX: Nâng cấp cơ chế chờ thông minh (Smart Wait).
                // Do Bot có độ trễ 0.5s mỗi nhịp, một cuộc chiến dài có thể tốn nhiều giây.
                // Kịch bản test sẽ liên tục theo dõi cho đến khi giá ngừng nhảy trong 2 giây liên tiếp.
                System.out.println("\n[*] Hệ thống đang chờ các Robot tiến hành chiến tranh...");
                double lastCheckPrice = currentPrice;
                int idleSeconds = 0;
                while (idleSeconds < 2) {
                    Thread.sleep(1000);
                    double checkPrice = auctionDAO.getCurrentMaxPrice(auctionId);
                    if (checkPrice > lastCheckPrice) {
                        lastCheckPrice = checkPrice;
                        idleSeconds = 0; // Đặt lại thời gian chờ vì cuộc chiến vẫn đang diễn ra
                    } else {
                        idleSeconds++; // Giá không đổi, chiến tranh đã ngã ngũ
                    }
                }

                System.out.println("\n[*] BƯỚC 4: Kiểm tra lại xem Database có nảy số chưa...");
                double newPrice = auctionDAO.getCurrentMaxPrice(auctionId);
                int newHighestBidderId = auctionDAO.getHighestBidderId(auctionId);
                
                if (newPrice > currentPrice && newHighestBidderId == userId) {
                    System.out.println("[+] KẾT LUẬN: THÀNH CÔNG! Robot đã nhảy lên giá mới: " + newPrice + " VNĐ");
                    System.out.println("Lỗi trước đây do thiếu cờ 'is_active = 1' đã được vá thành công!");
                } else if (newPrice > currentPrice && newHighestBidderId != userId) {
                    System.out.println("[+] KẾT LUẬN: THÀNH CÔNG (NHƯNG ROBOT CỦA BẠN ĐÃ THUA)!");
                    System.out.println("Robot của bạn đã kích hoạt và đẩy giá thành công, nhưng một Robot khác (User " + newHighestBidderId + ") có ngân sách lớn hơn đã đè giá ở phút chót và giành Top 1 với giá " + newPrice + " VNĐ.");
                } else if (newPrice == currentPrice) {
                    System.out.println("[X] KẾT LUẬN: THẤT BẠI. Giá không nhảy.");
                    System.out.println("Nguyên nhân có thể: Số dư của User " + userId + " không đủ, hoặc ngân sách Robot (" + maxBid + ") nhỏ hơn giá hiện tại.");
                }
                
            } catch (Exception e) {
                System.err.println("[X] Đã xảy ra lỗi Exception trong quá trình chạy Bot: ");
                e.printStackTrace();
            }
        }
        
        System.out.println("======================================================");
    }
}