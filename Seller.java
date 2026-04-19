/**
 * Lớp Người bán.
 */
import java.util.ArrayList;
import java.util.List;
class Seller extends User {
    private List<Item> myItems = new ArrayList<>();

    public Seller(String id, String name, String email) {
        super(id, name, email);
    }

    public void addItem(Item item) {
        myItems.add(item);
        System.out.println("Đã đăng sản phẩm: " + item.getName());
    }

    public List<Item> getMyItems() { return myItems; }
}