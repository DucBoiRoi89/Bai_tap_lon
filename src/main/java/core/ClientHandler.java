package core;

import com.google.gson.Gson;
import exception.AuctionClosedException;
import exception.InvalidBidException;
import model.AuctionEvent;
import model.ClientRequest;
import service.AuctionService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private int watchingAuctionId = -1;
    private int userId = -1; 
    private Gson gson = new Gson();
    private AuctionService auctionService = new AuctionService();

    public ClientHandler(Socket socket) { 
        this.socket = socket; 
    }
    
    public int getUserId() { 
        return this.userId; 
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(new java.io.OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true); 
BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String requestLine;
            while ((requestLine = in.readLine()) != null) {
                try {
                    ClientRequest req = gson.fromJson(requestLine, ClientRequest.class);
                    if (req != null) {
                        this.userId = req.getUserId();
                        
                        if ("WATCH".equals(req.getAction())) {
                            this.watchingAuctionId = req.getAuctionId();
                            System.out.println("[SERVER] User #" + this.userId + " đang xem sản phẩm #" + this.watchingAuctionId);
                            
                        } else if ("BID".equals(req.getAction())) {
                            try {
                                auctionService.processBid(req.getAuctionId(), req.getUserId(), req.getAmount());
                            } catch (InvalidBidException | AuctionClosedException e) {
                                // Gửi thẳng thông báo lỗi về cho người vừa đặt giá sai
                                sendError(req.getAuctionId(), e.getMessage());
                            } catch (Exception e) {
                                sendError(req.getAuctionId(), "Lỗi hệ thống: " + e.getMessage());
                            }
                        }
                    }
                } catch (Exception ex) { 
                    System.err.println("Lỗi xử lý request JSON: " + ex.getMessage()); 
                }
            }
        } catch (IOException e) { 
            System.out.println("[SERVER] Client #" + this.userId + " bị mất kết nối mạng."); 
        } finally {
            cleanUp();
        }
    }
    public void sendEvent(AuctionEvent event) {
        if (out != null && event.getItemId() == this.watchingAuctionId) {
            out.println(gson.toJson(event)); 
        }
    }
    private void sendError(int auctionId, String message) {
        if (out != null) {
            out.println(gson.toJson(new AuctionEvent(AuctionEvent.Type.ERROR, auctionId, message)));
        }
    }

    private void cleanUp() {
        AuctionServer.removeClient(this);
        System.out.println("[SERVER] Đã dọn dẹp tài nguyên của User #" + this.userId);
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}