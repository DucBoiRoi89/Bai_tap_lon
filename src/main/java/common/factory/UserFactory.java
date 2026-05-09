package common.factory;

// import common.entities.Admin;
// import common.entities.Bidder;
// import common.entities.Seller;
// import common.entities.User;
import common.entities.*;

public class UserFactory {
    public static User createUser(String id, String name, String email, String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return new Admin(id, name, email);
        } else if ("SELLER".equalsIgnoreCase(role)) {
            return new Seller(id, name, email);
        } else if ("BIDDER".equalsIgnoreCase(role)) {
            return new Bidder(id, name, email);
        } else {
            // Mặc định hoặc ném ngoại lệ nếu role không hợp lệ
            return new Bidder(id, name, email); 
        }
    }
}
