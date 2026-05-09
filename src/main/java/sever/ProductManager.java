package sever;
import common.entities.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ProductManager {
    private static ProductManager instance;
    private ConcurrentHashMap<Integer, Product> products = new ConcurrentHashMap<>();
    private AtomicInteger idCounter = new AtomicInteger(1);

    private ProductManager() {}

    public static synchronized ProductManager getInstance() {
        if (instance == null) {
            instance = new ProductManager();
        }
        return instance;
    }

    public void addProduct(Product product) throws Exception {
        if (products.containsKey(product.getId())) {
            throw new Exception("Lỗi: Product ID đã tồn tại!");
        }
        products.put(product.getId(), product);
    }

    // SỬA: Thêm 'User requester' để phân quyền (Authorization)
    public void removeProduct(int productId, User requester) throws Exception {
        Product p = products.get(productId);
        if (p == null) {
            throw new Exception("Lỗi: Không tìm thấy sản phẩm!");
        }

        // CHỈ XÓA NẾU: Là Admin HOẶC Là người tạo ra sản phẩm (sellerId == userId)
        if (requester instanceof Admin || requester.getId().equals(p.getSellerId())) {
            products.remove(productId);
            System.out.println("Đã xóa sản phẩm: " + productId);
        } else {
            throw new Exception("Từ chối: Bạn không có quyền xóa sản phẩm của người khác!");
        }
    }
    
    // SỬA: Thêm 'User requester' để phân quyền (Authorization)
    public void updateProduct(Product updatedProduct, User requester) throws Exception {
        Product p = products.get(updatedProduct.getId());
        if (p == null) {
            throw new Exception("Lỗi: Không tìm thấy sản phẩm!");
        }

        if (requester instanceof Admin || requester.getId().equals(p.getSellerId())) {
            products.put(updatedProduct.getId(), updatedProduct);
            System.out.println("Đã cập nhật sản phẩm: " + updatedProduct.getId());
        } else {
            throw new Exception("Từ chối: Bạn không có quyền sửa sản phẩm của người khác!");
        }
    }
}