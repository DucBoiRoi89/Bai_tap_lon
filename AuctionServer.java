import java.net.*;
import java.io.*;

public class AuctionServer {
    private static final int PORT = 1234;

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("--- Server Đấu Giá đã sẵn sàng tại cổng " + PORT + " ---");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Mỗi Client kết nối sẽ được xử lý bởi một Thread riêng (Multi-threading)
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler extends Thread {
        private Socket socket;
        public ClientHandler(Socket socket) { this.socket = socket; }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                
                String request = in.readLine(); // Nhận lệnh từ Client (VD: "BID;item01;150")
                // Logic phân tích request và gọi Manager tương ứng...
                // if (request.startsWith("BID")) { ... ItemManager.getInstance().placeBid(...) }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) {
        new AuctionServer().start();
    }
}