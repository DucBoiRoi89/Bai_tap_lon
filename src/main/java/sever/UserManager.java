package sever;
import common.entities.*;
import common.exceptions.AuthenticationException;
import common.factory.UserFactory;
import database.UserDAO;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UserManager {
    private static UserManager instance;
    private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    
    // THÊM: Map phụ dùng để check trùng tên an toàn trong môi trường đa luồng
    private ConcurrentHashMap<String, String> usernameToIdMap = new ConcurrentHashMap<>();
    
    private AtomicInteger idCounter;

    private UserManager() {
        // SỬA: Đồng bộ ID với Database để tránh lỗi Duplicate Key
        int maxId = new UserDAO().getMaxUserId();
        this.idCounter = new AtomicInteger(maxId + 1);
    }

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    // SỬA: Sử dụng AtomicInteger đúng cách và tích hợp UserDAO để lưu vào Database
    public User registerUser(String username, String role) throws Exception {
        // Lấy và tăng ID nguyên tử (Atomic) để tránh trùng ID khi nhiều luồng chạy cùng lúc
        String newId = String.valueOf(idCounter.getAndIncrement()); 

        // Trả về null nếu chưa có ai dùng tên này, ngược lại trả về ID của người cũ
        String existingId = usernameToIdMap.putIfAbsent(username, newId);
        
        if (existingId != null) {
            throw new IllegalArgumentException("Lỗi: Username '" + username + "' đã tồn tại!");
        }

        String email = username + "@auction.com"; // Tạo email mặc định
        User newUser = UserFactory.createUser(newId, username, email, role);
        
        // SỬA: Phải đặt mật khẩu mặc định (hoặc truyền từ request) 
        // để tránh lỗi SQL NOT NULL constraint
        newUser.setPassword("123456"); 

        // LƯU VÀO DATABASE thông qua UserDAO
        UserDAO userDAO = new UserDAO();
        if (!userDAO.saveUser(newUser)) {
            // Nếu lưu DB thất bại, phải xóa khỏi Map để có thể đăng ký lại
            usernameToIdMap.remove(username);
            throw new Exception("Lỗi: Không thể lưu người dùng vào cơ sở dữ liệu!");
        }

        users.put(newId, newUser);
        return newUser;
    }
    
    public User login(String email, String password) throws AuthenticationException {
        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserByEmail(email);
        
        // user.getPassword() bây giờ đã hết lỗi nhờ Getter ở file User.java
        if (user != null && user.getPassword().equals(password)) {
            System.out.println("Đăng nhập thành công: " + user.getName());
            return user;
        } else {
            throw new AuthenticationException("Email hoặc mật khẩu không chính xác!");
        }
    }

    public void register(User newUser) {
        UserDAO userDAO = new UserDAO();
        if (userDAO.getUserByEmail(newUser.getEmail()) == null) {
            userDAO.saveUser(newUser);
            System.out.println("Đăng ký thành công tài khoản: " + newUser.getEmail());
        }
    }
}