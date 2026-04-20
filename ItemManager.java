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
        // 1. Lấy dữ liệu mới nhất từ DB thay vì chỉ tin vào RAM (Tránh Lost Update)
        Item item = itemDAO.findById(itemId); // Bạn nên viết thêm hàm findById trong DAO
        
        if (item == null || item.getEndTime().isBefore(LocalDateTime.now()) || 
            item.getStatus() == AuctionStatus.FINISHED) {
            return false;
        }

        if (bid.getAmount() <= item.getCurrentPrice()) {
            return false;
        }

        // 2. Thực hiện cập nhật Database
        if (itemDAO.updateCurrentPrice(itemId, bid.getAmount())) {
            item.updatePrice(bid.getAmount());

            // 3. Xử lý Anti-sniping (Đã bổ sung update DB)
            long secondsLeft = Duration.between(LocalDateTime.now(), item.getEndTime()).toSeconds();
            if (secondsLeft < 30 && secondsLeft > 0) {
                LocalDateTime newEndTime = item.getEndTime().plusMinutes(1);
                item.setEndTime(newEndTime);
                // Quan trọng: Cập nhật thời gian mới vào Database!
                itemDAO.updateEndTime(itemId, newEndTime); 
            }

            // 4. Đồng bộ lại danh sách RAM của Manager
            updateItemInCache(item); 

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
    /**
     * Cập nhật lại đối tượng trong danh sách RAM (cache) của Manager.
     * Phương thức này giúp đồng bộ dữ liệu ngay lập tức mà không cần load lại toàn bộ DB.
     */
    private void updateItemInCache(Item updatedItem) {
        if (updatedItem == null) return;

        for (int i = 0; i < allItems.size(); i++) {
            // Tìm item cũ trong danh sách dựa trên ID
            if (allItems.get(i).getId().equals(updatedItem.getId())) {
                // Thay thế bằng đối tượng mới đã cập nhật giá/thời gian
                allItems.set(i, updatedItem);
                System.out.println("[Cache] Đã đồng bộ sản phẩm: " + updatedItem.getName());
                break;
            }
        }
    }
}