import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class ItemManager {
    private static ItemManager instance;
    private List<Item> allItems; 
    private ItemDAO itemDAO;

    private ItemManager() {
        this.itemDAO = new ItemDAO();
        // Load lại danh sách từ DB để đảm bảo đồng bộ
        this.allItems = itemDAO.getAllItems(); 
    }

    public static synchronized ItemManager getInstance() {
        if (instance == null) {
            instance = new ItemManager();
        }
        return instance;
    }

    // Logic đặt giá an toàn (Thread-safe)
    public synchronized boolean placeBid(String itemId, BidTransaction bid) {
        Item item = findById(itemId);
        
        // Kiểm tra điều kiện thời gian và tồn tại
        if (item == null || item.getEndTime().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Kiểm tra giá đặt phải lớn hơn giá hiện tại
        if (bid.getAmount() <= item.getCurrentPrice()) {
            return false;
        }

        // Thực hiện cập nhật Database trước
        if (itemDAO.updateCurrentPrice(itemId, bid.getAmount())) {
            // Cập nhật RAM sau khi DB thành công
            item.updatePrice(bid.getAmount());
            
            // THUẬT TOÁN ANTI-SNIPING (Cộng điểm nâng cao)
            long secondsLeft = Duration.between(LocalDateTime.now(), item.getEndTime()).getSeconds();
            if (secondsLeft < 30 && secondsLeft > 0) {
                item.setEndTime(item.getEndTime().plusMinutes(1));
            }

            // Gọi thông báo Realtime (Observer Pattern)
            // Nếu AuctionManager báo lỗi đỏ ở đây, hãy chắc chắn bạn đã tạo class đó
            AuctionManager.getInstance().notifyBidders(item);
            return true;
        }
        return false;
    }

    public Item findById(String id) {
        for (Item item : allItems) {
            if (item.getId().equals(id)) return item;
        }
        return null;
    }
}