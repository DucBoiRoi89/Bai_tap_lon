import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // 1. Khởi tạo luồng vào/ra
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 2. ĐĂNG KÝ VỚI AUCTION MANAGER (Để nhận tin nhắn realtime)
            AuctionManager.getInstance().addClientWriter(out);
            System.out.println("[Server] Một Client mới đã kết nối và đăng ký nhận thông báo.");

            // 3. Vòng lặp lắng nghe lệnh từ Client (VD: BID, LOGIN...)
            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("[Client gửi]: " + request);
                // Xử lý logic dựa trên request ở đây
                // Ví dụ: if(request.startsWith("BID")) { ... }
            }

        } catch (IOException e) {
            System.out.println("[Server] Client đã ngắt kết nối.");
        } finally {
            // 4. HỦY ĐĂNG KÝ KHI THOÁT (Quan trọng để tránh lỗi gửi tin vào socket đã đóng)
            AuctionManager.getInstance().removeClientWriter(out);
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}