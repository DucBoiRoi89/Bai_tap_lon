package core;

import com.google.gson.Gson;
import exception.AuctionClosedException;
import exception.InvalidBidException;
import model.AuctionEvent;
import model.ClientRequest;
import service.AuctionService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private int watchingAuctionId = -1;
    private int userId = -1; 
    private Gson gson = new Gson();
    private AuctionService auctionService = new AuctionService();

    public ClientHandler(Socket socket) { this.socket = socket; }
    public int getUserId() { return this.userId; }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));           
            String requestLine;
            while ((requestLine = in.readLine()) != null) {
                try {
                    ClientRequest req = gson.fromJson(requestLine, ClientRequest.class);
                    if (req != null) {
                        this.userId = req.getUserId();
                        if ("WATCH".equals(req.getAction())) {
                            this.watchingAuctionId = req.getAuctionId();
                        } else if ("BID".equals(req.getAction())) {
                            try {
                                auctionService.processBid(req.getAuctionId(), req.getUserId(), req.getAmount());
                            } catch (InvalidBidException | AuctionClosedException e) {
                                out.println(gson.toJson(new AuctionEvent(AuctionEvent.Type.ERROR, req.getAuctionId(), e.getMessage())));
                            } catch (Exception e) {
                                out.println(gson.toJson(new AuctionEvent(AuctionEvent.Type.ERROR, req.getAuctionId(), "Lỗi hệ thống: " + e.getMessage())));
                            }
                        }
                    }
                } catch (Exception ex) { System.err.println("Lỗi xử lý request: " + ex.getMessage()); }
            }
        } catch (Exception e) { AuctionServer.removeClient(this); }
    }

    public void sendEvent(AuctionEvent event) {
        if (event.getAuctionId() == this.watchingAuctionId && out != null) {
            out.println(gson.toJson(event)); 
        }
    }
}