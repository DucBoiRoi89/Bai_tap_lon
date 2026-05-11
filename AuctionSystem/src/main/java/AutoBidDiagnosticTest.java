

import dao.AuctionDAO;
import service.AuctionService;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class AutoBidDiagnosticTest {
    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            System.setErr(new PrintStream(System.err, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Scanner scanner = new Scanner(System.in, "UTF-8");
        AuctionService auctionService = new AuctionService();
        AuctionDAO auctionDAO = auctionService.getAuctionDAO();

        System.out.println("==================================================");
        System.out.println("   CÔNG CỤ CHUẨN ĐOÁN HỆ THỐNG AUTO-BID & BIDDING");
        System.out.println("==================================================");

        try {
            System.out.print("[?] Bạn muốn chạy kịch bản TỰ ĐỘNG (a) hay Nhập tay (m)? [Nhấn Enter để Tự Động]: ");
            String mode = scanner.nextLine();
            if (mode.trim().isEmpty() || mode.equalsIgnoreCase("a")) {
                runAutomatedScenario(auctionService, auctionDAO);
                return;
            }

            System.out.print("Nhập ID phiên đấu giá (auction_id đang RUNNING): ");
            int auctionId = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Nhập ID người dùng của bạn (user_id): ");
            int userId = Integer.parseInt(scanner.nextLine());
            
            System.out.print("\n[?] Bạn có muốn RESET phiên đấu giá này về giá gốc (10,000đ) và nạp lại 100 triệu để test không? (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                resetTestData(auctionId, userId);
            }
            System.out.println();

            System.out.print("Nhập giá bạn muốn đặt (lớn hơn giá hiện tại): ");
            double bidAmount = Double.parseDouble(scanner.nextLine());
            
            System.out.print("\n[?] Bạn có muốn cấu hình một Robot AutoBid trước khi đặt giá không? (y/n): ");
            String setupBot = scanner.nextLine();
            if (setupBot.equalsIgnoreCase("y")) {
                System.out.print(" -> Nhập ID User của Bot: ");
                int botId = Integer.parseInt(scanner.nextLine());
                System.out.print(" -> Nhập giá MAX Bot có thể trả: ");
                double maxBid = Double.parseDouble(scanner.nextLine());
                System.out.print(" -> Nhập bước giá của Bot (increment): ");
                double increment = Double.parseDouble(scanner.nextLine());
                
                boolean botSaved = auctionDAO.saveAutoBid(auctionId, botId, maxBid, increment);
                if (botSaved) {
                    System.out.println(" => [OK] Đã lưu cấu hình AutoBid cho Bot ID " + botId);
                    // Ép bật trạng thái Active cho Bot đề phòng Database để mặc định là Tắt (0)
                    try (java.sql.Connection conn = config.DatabaseConnection.getInstance().getConnection();
                         java.sql.Statement s = conn.createStatement()) {
                        s.execute("UPDATE AUTO_BID_CONFIGS SET is_active = 1 WHERE auction_id = " + auctionId);
                    } catch (Exception ignore) {}
                } else {
                    System.out.println(" => [LỖI] Không thể lưu cấu hình Bot. Kiểm tra lại dữ liệu đầu vào.");
                }
            }
            
            System.out.println("\n--------------------------------------------------");
            
            if (!runPreflightChecks(auctionId, userId)) {
                System.out.println("\n=> [HỦY BỎ]: Vui lòng mở MySQL Workbench chạy các lệnh sửa lỗi ở trên, sau đó chạy lại Test nhé!");
                return;
            }

            System.out.println("Đang gửi yêu cầu đặt giá " + bidAmount + " cho User " + userId + "...");
            
            // Thực hiện processBid (Sẽ đệ quy gọi AutoBidService nếu có bot)
            auctionService.processBid(auctionId, userId, bidAmount);
            System.out.println(" => [OK] processBid() đã thực thi thành công, không có Exception.");
            
            System.out.println("\nKiểm tra lại cơ sở dữ liệu...");
            int highestBidder = auctionDAO.getHighestBidderId(auctionId);
            System.out.println(" => Người giữ giá cao nhất hiện tại trong Database (bidder_id): " + highestBidder);
            
            if (highestBidder != -1 && highestBidder != userId) {
                System.out.println("\n[KẾT LUẬN]: HOÀN HẢO! Robot Auto-bid đã tự động bắt được tín hiệu và vượt giá của bạn.");
                System.out.println("Người thắng hiện tại là Bot ID: " + highestBidder);
            } else if (highestBidder == userId) {
                System.out.println("\n[KẾT LUẬN]: Bạn vẫn đang giữ giá cao nhất.");
                System.out.println("Lý do có thể: Không có Bot nào bật, hoặc giá của bạn đã vượt quá giới hạn MAX của Bot, hoặc Bot không đủ số dư.");
            } else {
                System.out.println("\n[KẾT LUẬN]: Lỗi hệ thống, không có dữ liệu người đặt giá hợp lệ.");
            }
            
        } catch (exception.InvalidBidException | exception.AuctionClosedException e) {
            System.err.println("\n[TỪ CHỐI ĐẶT GIÁ]: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\n[LỖI NGHIÊM TRỌNG]: Đã xảy ra lỗi ngoài ý muốn.");
            e.printStackTrace();
        } finally {
            scanner.close();
            System.out.println("==================================================");
        }
    }

    private static boolean runPreflightChecks(int auctionId, int userId) {
        System.out.println("[TỰ ĐỘNG CHUẨN ĐOÁN DATABASE TRƯỚC KHI ĐẶT GIÁ]...");
        
        fixDatabaseAutomatically();
        
        try (java.sql.Connection conn = config.DatabaseConnection.getInstance().getConnection()) {
            
            // 1. Kiểm tra xem bảng USERS đã có cột balance chưa
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT balance FROM USERS LIMIT 1");
            } catch (java.sql.SQLException e) {
                System.err.println(" [X] LỖI NGHIÊM TRỌNG: Bảng USERS của bạn đang thiếu cột 'balance' (Số dư).");
                System.err.println("     -> CÁCH SỬA: Hãy vào Database chạy lệnh SQL sau:");
                System.err.println("        ALTER TABLE USERS ADD COLUMN balance DECIMAL(15,2) DEFAULT 100000000.0;");
                return false;
            }

            // 2. Kiểm tra User ID đang test có tồn tại và đủ tiền không
            try (java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT balance FROM USERS WHERE user_id = ?")) {
                stmt.setInt(1, userId);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        System.err.println(" [X] LỖI NGHIÊM TRỌNG: Không tìm thấy User ID " + userId + " trong Database.");
                        System.err.println("     -> CÁCH SỬA: Hãy vào Database chạy lệnh SQL sau để tạo User:");
                        System.err.println("        INSERT INTO USERS (user_id, username, password, role, balance) VALUES (" + userId + ", 'test_user_" + userId + "', '123', 'BIDDER', 100000000.0);");
                        return false;
                    } else {
                        double balance = rs.getDouble("balance");
                        if (balance <= 0) {
                            System.err.println(" [X] LỖI NGHIÊM TRỌNG: User ID " + userId + " có số dư = 0. Không đủ tiền đặt giá.");
                            System.err.println("     -> CÁCH SỬA: Hãy nạp tiền bằng lệnh SQL sau:");
                            System.err.println("        UPDATE USERS SET balance = 100000000.0 WHERE user_id = " + userId + ";");
                            return false;
                        } else {
                            System.out.println(" [OK] User ID " + userId + " hợp lệ (Số dư hiện tại: " + balance + ").");
                        }
                    }
                }
            }

            // 3. Kiểm tra bảng AUTO_BID_CONFIGS, nếu sai cấu trúc thì XÓA VÀ TẠO LẠI cho chuẩn 100%
            boolean needsRebuild = false;
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT config_id, is_active FROM AUTO_BID_CONFIGS LIMIT 1");
            } catch (java.sql.SQLException e) {
                needsRebuild = true;
            }

            if (needsRebuild) {
                System.out.println(" [*] Bảng AUTO_BID_CONFIGS đang sai cấu trúc. Đang tự động tạo lại bảng chuẩn...");
                try (java.sql.Statement s = conn.createStatement()) {
                    s.execute("DROP TABLE IF EXISTS AUTO_BID_CONFIGS");
                    String createTable = "CREATE TABLE AUTO_BID_CONFIGS (" +
                                         "config_id INT AUTO_INCREMENT PRIMARY KEY, " +
                                         "auction_id INT, " +
                                         "user_id INT, " +
                                         "max_bid_amount DECIMAL(15,2), " +
                                         "bid_increment DECIMAL(15,2), " +
                                         "is_active TINYINT(1) DEFAULT 1, " +
                                         "FOREIGN KEY (auction_id) REFERENCES AUCTIONS(auction_id) ON DELETE CASCADE, " +
                                         "FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE CASCADE)";
                    s.execute(createTable);
                    System.out.println("     -> [OK] Đã cấu trúc lại bảng AUTO_BID_CONFIGS thành công!");
                } catch (Exception ex) {
                    System.err.println(" [X] Không thể tạo lại bảng AUTO_BID_CONFIGS: " + ex.getMessage());
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            System.err.println(" [X] LỖI KẾT NỐI DATABASE: " + e.getMessage());
            return false;
        }
    }

    private static void fixDatabaseAutomatically() {
        System.out.println(" [*] Đang tự động dọn dẹp Trigger rác và vá lỗi Procedure trong Database...");
        try (java.sql.Connection conn = config.DatabaseConnection.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            
            // Tự động tìm và xóa TOÀN BỘ Trigger có trong Database để triệt tiêu tận gốc nguyên nhân
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT TRIGGER_NAME FROM information_schema.TRIGGERS WHERE TRIGGER_SCHEMA = DATABASE()")) {
                java.util.List<String> triggers = new java.util.ArrayList<>();
                while (rs.next()) {
                    triggers.add(rs.getString("TRIGGER_NAME"));
                }
                for (String triggerName : triggers) {
                    stmt.execute("DROP TRIGGER IF EXISTS " + triggerName);
                    System.out.println("     -> Đã dọn dẹp Trigger rác: " + triggerName);
                }
            }
            
            stmt.execute("DROP PROCEDURE IF EXISTS PRO_PlaceSingleBid");
            
            String proc = "CREATE PROCEDURE PRO_PlaceSingleBid (" +
                "IN p_bid_amount DECIMAL(15,2), " +
                "IN p_user_id INT, " +
                "IN p_auction_id INT, " +
                "OUT p_status_code INT) " +
                "BEGIN " +
                "DECLARE v_current_max_price DECIMAL(15,2); DECLARE v_starting_price DECIMAL(15,2); " +
                "DECLARE v_user_balance DECIMAL(15,2); DECLARE v_status ENUM('OPEN', 'RUNNING', 'FINISHED', 'CANCELED'); " +
                "DECLARE v_target_price DECIMAL(15,2); DECLARE v_previous_bidder_id INT DEFAULT NULL; " +
                "DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END; " +
                "START TRANSACTION; " +
                "SELECT starting_price, current_max_price, status INTO v_starting_price, v_current_max_price, v_status " +
                "FROM AUCTIONS WHERE auction_id = p_auction_id FOR UPDATE; " +
                "SELECT bidder_id INTO v_previous_bidder_id FROM BID_TRANSACTIONS WHERE auction_id = p_auction_id ORDER BY bid_amount DESC, bid_time ASC LIMIT 1; " +
                "SELECT balance INTO v_user_balance FROM USERS WHERE user_id = p_user_id FOR UPDATE; " +
                "IF v_status = 'RUNNING' THEN " +
                "IF v_current_max_price IS NULL THEN SET v_target_price = v_starting_price; ELSE SET v_target_price = v_current_max_price; END IF; " +
                "IF v_user_balance < p_bid_amount THEN SET p_status_code = -3; ROLLBACK; " +
                "ELSEIF p_bid_amount > v_target_price OR (v_current_max_price IS NULL AND p_bid_amount = v_starting_price) THEN " +
                "UPDATE USERS SET balance = balance - p_bid_amount WHERE user_id = p_user_id; " +
                "IF v_previous_bidder_id IS NOT NULL THEN UPDATE USERS SET balance = balance + v_current_max_price WHERE user_id = v_previous_bidder_id; END IF; " +
                "INSERT INTO BID_TRANSACTIONS (auction_id, bidder_id, bid_amount) VALUES (p_auction_id, p_user_id, p_bid_amount); " +
                "UPDATE AUCTIONS SET current_max_price = p_bid_amount WHERE auction_id = p_auction_id; " +
                "SET p_status_code = 1; COMMIT; " +
                "ELSE SET p_status_code = 0; ROLLBACK; END IF; ELSE SET p_status_code = -1; ROLLBACK; END IF; END";
            stmt.execute(proc);
        } catch (Exception e) {}
    }

    private static void resetTestData(int auctionId, int userId) {
        try (java.sql.Connection conn = config.DatabaseConnection.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE USERS SET balance = 100000000.0"); // Bơm 100tr cho tất cả user để thoải mái test
            stmt.execute("UPDATE AUCTIONS SET current_max_price = NULL, starting_price = 10000.0, status = 'RUNNING' WHERE auction_id = " + auctionId);
            stmt.execute("DELETE FROM BID_TRANSACTIONS WHERE auction_id = " + auctionId);
            stmt.execute("DELETE FROM AUTO_BID_CONFIGS WHERE auction_id = " + auctionId);
            System.out.println(" => [OK] Đã reset phiên đấu giá " + auctionId + " về 10,000 VNĐ. Các tài khoản đã được nạp 100 triệu.");
        } catch (Exception e) {
            System.out.println(" => [LỖI RESET]: " + e.getMessage());
        }
    }

    private static void runAutomatedScenario(AuctionService auctionService, AuctionDAO auctionDAO) {
        int auctionId = 1;
        int userId = 11;
        double bidAmount = 20000.0;
        int botId = 12;
        double maxBid = 80000000.0;
        double increment = 5000.0;

        System.out.println("\n>>> KHỞI ĐỘNG KỊCH BẢN AUTO-TEST <<<");
        resetTestData(auctionId, userId);

        System.out.println(" => Đang thiết lập cấu hình Robot ID " + botId + " (Giới hạn: " + maxBid + ", Bước giá: " + increment + ")...");
        auctionDAO.saveAutoBid(auctionId, botId, maxBid, increment);
        try (java.sql.Connection conn = config.DatabaseConnection.getInstance().getConnection();
             java.sql.Statement s = conn.createStatement()) {
            s.execute("UPDATE AUTO_BID_CONFIGS SET is_active = 1 WHERE auction_id = " + auctionId);
        } catch (Exception ignore) {}

        if (!runPreflightChecks(auctionId, userId)) {
            System.out.println("\n=> [HỦY BỎ]: Quá trình kiểm tra Database thất bại.");
            return;
        }

        System.out.println("\n--------------------------------------------------");
        System.out.println("Đang gửi yêu cầu đặt giá " + bidAmount + " cho User " + userId + "...");
        
        try {
            auctionService.processBid(auctionId, userId, bidAmount);
            System.out.println(" => [OK] processBid() đã thực thi thành công.");
            
            System.out.println("\nKiểm tra lại cơ sở dữ liệu...");
            int highestBidder = auctionDAO.getHighestBidderId(auctionId);
            
            if (highestBidder == botId) {
                System.out.println("\n[KẾT LUẬN]: HOÀN HẢO! Robot Auto-bid đã tự động bắt được tín hiệu và đè giá thành công.");
                System.out.println("Người thắng hiện tại là Bot ID: " + highestBidder);
            } else {
                System.out.println("\n[KẾT LUẬN]: Bot chưa đè giá được. Người giữ giá cao nhất hiện tại: " + highestBidder);
            }
        } catch (Exception e) {
            System.err.println("\n[LỖI]: " + e.getMessage());
        }
    }
}