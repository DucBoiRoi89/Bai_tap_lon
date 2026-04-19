import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemManager {
    private static ItemManager instance;
    private List<Item> allItems; // Danh sách toàn bộ sản phẩm trong hệ thống

    private ItemManager() {
        allItems = new ArrayList<>();
        // Có thể load dữ liệu từ Database lên đây khi khởi tạo
    }

    public static synchronized ItemManager getInstance() {
        if (instance == null) {
            instance = new ItemManager();
        }
        return instance;
    }

    // Chức năng 3.1.2: Thêm sản phẩm mới (từ Seller gửi lên)
    public void createNewItem(Item item) {
        allItems.add(item);
        // Gọi DAO để lưu vào SQL
        // ItemDAO.save(item);
    }

    // Chức năng tìm kiếm sản phẩm
    public List<Item> searchByName(String keyword) {
        return allItems.stream()
            .filter(i -> i.getName().contains(keyword))
            .collect(Collectors.toList());
    }

    // Lấy các sản phẩm theo loại (Electronics, Art, Vehicle)
    public <T extends Item> List<T> getItemsByType(Class<T> type) {
        return allItems.stream()
            .filter(type::isInstance)
            .map(type::cast)
            .collect(Collectors.toList());
    }

}
