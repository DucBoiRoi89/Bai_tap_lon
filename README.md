 Hệ Thống Đấu Giá Trực Tuyến (Auction System)

 1. Mô tả bài toán và phạm vi hệ thống
Hệ thống Đấu giá trực tuyến là một ứng dụng Client-Server cho phép nhiều người dùng (Bidder) tham gia đấu giá các sản phẩm theo thời gian thực. Hệ thống hỗ trợ người dùng đăng bán sản phẩm (Seller), theo dõi giá thầu và đặc biệt tích hợp tính năng **Đấu giá tự động (Auto-Bid Bot)** giúp người dùng tự động trả giá dựa trên ngân sách thiết lập sẵn.
Phạm vi hệ thống: Phục vụ mạng nội bộ (LAN) hoặc qua Internet thông qua giao thức TCP/IP (Java Socket).

2. Công nghệ sử dụng và yêu cầu cài đặt
Ngôn ngữ:** Java 17
Giao diện:** JavaFX 17.0.2
Cơ sở dữ liệu:** MySQL (sử dụng `mysql-connector-j` 8.0.33)
Xử lý JSON: Gson 2.10.1
Quản lý dự án & Build: Maven
Môi trường chạy: Cần cài đặt tối thiểu JRE/JDK 17 và có sẵn MySQL Server chạy ở port 3306.

 3. Cấu trúc thư mục / Module chính
Dự án tuân theo mô hình MVC, bao gồm các package chính trong `src/main/java`:
 `core/`: Chứa các thành phần mạng (AuctionServer, AuctionSocketClient).
 `controller/`: Điều khiển giao diện JavaFX (AuctionController, AutoBidController, v.v.).
 `dao/`: Các lớp tương tác trực tiếp với cơ sở dữ liệu MySQL (ItemDAO, AuctionDAO, UserDAO...).
 `model/`: Các thực thể dữ liệu (User, Item, BidRecord, ClientRequest, AuctionEvent).
 `service/`: Chứa logic nghiệp vụ lõi (AuctionService, AutoBidService xử lý luồng Bot).
 `exception/`: Chứa các ngoại lệ tùy chỉnh.
 Thư mục `src/main/resources/views/`: Chứa các file giao diện `.fxml` và `.css`.

 4. Vị trí các file .jar
Sau khi sử dụng lệnh Maven (`mvn clean package`) để build theo cơ chế Uber JAR/Fat JAR, các file thực thi có kèm sẵn thư viện sẽ nằm trong thư mục `target/`:
* **Server:** `target/AuctionSystem-1.0-SNAPSHOT-Server.jar`
* **Client:** `target/AuctionSystem-1.0-SNAPSHOT-Client.jar`

## 5. Hướng dẫn chạy hệ thống


Bước 1: Chuẩn bị Database
Import file script SQL vào MySQL Server.
 Đảm bảo thông tin kết nối database trong code (`URL`, `USER`, `PASSWORD`) trùng khớp với môi trường.

Bước 2: Khởi động Server
Mở terminal và chạy lệnh sau để khởi động máy chủ:
```bash
java -jar target/AuctionSystem-1.0-SNAPSHOT-Server.jar
```
*(Lưu ý: Server phải được chạy trước để mở cổng Socket lắng nghe kết nối)*

Bước 3: Khởi động Client
Mở một terminal khác (có thể chạy nhiều lần để tạo ra nhiều Client đấu giá với nhau) và chạy:
```bash
java -jar target/AuctionSystem-1.0-SNAPSHOT-Client.jar
```

## 6. Danh sách chức năng đã hoàn thành
-  Đăng nhập/Đăng ký người dùng với phân quyền (BIDDER, SELLER, ADMIN).
-  Hiển thị danh sách sản phẩm đang đấu giá.
-  Socket theo thời gian thực (Real-time): Cập nhật giá ngay lập tức cho mọi Client đang xem phiên đấu giá (Watching) mà không cần reload.
-  Thuật toán Bot Auto-Bid: Tự động đè giá bằng đa luồng (Multi-threading), xét ưu tiên theo Max Bid và thời gian đăng ký.
-  Luồng kiểm tra an toàn luồng (Polling & Thread-safe): Xử lý tránh ngập lụt Socket khi Bot tự động nhảy số.
-  Xem chi tiết sản phẩm và lịch sử đấu giá cá nhân.
-  Quản lý số dư người dùng (trừ tiền hợp lệ khi đấu giá).

## 7. Tài liệu liên quan
Báo cáo PDF: `[Chèn link Google Drive hoặc đường dẫn tới file báo cáo PDF]`
Video Demo: `[Chèn link YouTube hoặc Google Drive video chạy thử hệ thống]`
