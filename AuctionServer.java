import java.io.*;
import java.net.*;

public class AuctionServer {
    private static final int PORT = 1234;

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("--- SERVER ĐẤU GIÁ ĐANG CHẠY TẠI CỔNG " + PORT + " ---");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Mỗi khi có người vào, tạo một Handler riêng
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) { this.socket = socket; }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // BƯỚC 1: Đăng ký với AuctionManager để nhận thông báo Realtime
                // Đây là chỗ giúp sửa lỗi "Realtime Update"
                AuctionManager.getInstance().addClientWriter(out);

                String request;
                // BƯỚC 2: Dùng vòng lặp để giữ kết nối liên tục
                while ((request = in.readLine()) != null) {
                    System.out.println("Lệnh từ Client: " + request);
                    
                    // BƯỚC 3: Phân tích giao thức (Protocol)
                    // Ví dụ Client gửi: "BID:item01:500.0"
                    if (request.startsWith("BID:")) {
                        String[] parts = request.split(":");
                        String itemId = parts[1];
                        double amount = Double.parseDouble(parts[2]);
                        
                        // Giả sử bạn có bidderId từ lúc login, ở đây tạm gọi là "user123"
                        BidTransaction bid = new BidTransaction("user123", itemId, amount);
                        
                        // Gọi ItemManager xử lý logic
                        boolean success = ItemManager.getInstance().placeBid(itemId, bid);
                        
                        if (success) {
                            out.println("BID_SUCCESS:Đặt giá thành công!");
                        } else {
                            out.println("BID_FAILED:Giá không hợp lệ hoặc hết giờ!");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Một client đã thoát.");
            } finally {
                // BƯỚC 4: Khi client thoát, phải xóa PrintWriter khỏi danh sách
                AuctionManager.getInstance().removeClientWriter(out);
                try { socket.close(); } catch (IOException e) {}
            }
        }
    }

    public static void main(String[] args) {
        new AuctionServer().start();
    }
}