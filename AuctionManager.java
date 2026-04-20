import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class AuctionManager {
    private static AuctionManager instance;
    
    // Danh sách các "Observer" - ở đây là các luồng xuất dữ liệu của Client
    private List<PrintWriter> clientWriters = new ArrayList<>();

    private AuctionManager() {}

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    // --- PHẦN QUAN TRỌNG: ĐĂNG KÝ VÀ HỦY ĐĂNG KÝ OBSERVER ---

    public synchronized void addClientWriter(PrintWriter writer) {
        clientWriters.add(writer);
    }

    public synchronized void removeClientWriter(PrintWriter writer) {
        clientWriters.remove(writer);
    }

    // --- CHỈNH SỬA HÀM NOTIFYBIDDERS ---

    public synchronized void notifyBidders(Item item) {
        String message = "UPDATE_PRICE|" + item.getId() + "|" + item.getCurrentPrice() + "|" + item.getEndTime();
        
        System.out.println("[Server] Đang thông báo cho " + clientWriters.size() + " người dùng...");

        // Duyệt qua danh sách các Socket đang kết nối để gửi tin nhắn
        for (PrintWriter writer : clientWriters) {
            try {
                writer.println(message); // Gửi tin nhắn về Client
            } catch (Exception e) {
                // Nếu gửi lỗi (Client mất kết nối), ta sẽ dọn dẹp sau
            }
        }
    }
}