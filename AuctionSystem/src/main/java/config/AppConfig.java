package config;

public class AppConfig {
    // Quản lý IP tập trung. Đổi IP ở đây thì toàn bộ Client và Database sẽ tự động ăn theo.
    // Đặt mặc định là "localhost" để Thầy/Cô có thể chạy ngay cả Server và Client trên cùng 1 máy tính.
    public static final String SERVER_IP = "localhost"; 
}