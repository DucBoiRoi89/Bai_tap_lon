public class UserManager {
    private static UserManager instance;
    private UserDAO userDAO;

    private UserManager() {
        this.userDAO = new UserDAO();
    }

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public User login(String email, String password) throws AuthenticationException {
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
        if (userDAO.getUserByEmail(newUser.getEmail()) == null) {
            userDAO.saveUser(newUser);
            System.out.println("Đăng ký thành công tài khoản: " + newUser.getEmail());
        }
    }
}