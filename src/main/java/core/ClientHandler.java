package core;
import com.google.gson.Gson;
import model.AuctionEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private int watchingAuctionId = -1;
    private Gson gson = new Gson(); 
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }
    public void setWatchingAuctionId(int auctionId) {
        this.watchingAuctionId = auctionId;
    }
    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true); // true = auto-flush
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            String requestLine;
            while ((requestLine = in.readLine()) != null) {
                try {
                    this.watchingAuctionId = Integer.parseInt(requestLine.trim());
                } catch (NumberFormatException ex) {
                    System.err.println("Dữ liệu client gửi không phải số ID hợp lệ");
                }
            }
        } catch (Exception e) {
            AuctionServer.removeClient(this);
        }
    }
    public void sendEvent(AuctionEvent event) {
        if (event.getAuctionId() == this.watchingAuctionId && out != null) {
            String jsonEvent = gson.toJson(event);
            out.println(jsonEvent); 
        }
    }
}