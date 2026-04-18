import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;
    private int watchingAuctionId = -1;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void setWatchingAuctionId(int auctionId) {
        this.watchingAuctionId = auctionId;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            while (true) {
                Object request = in.readObject();
                if (request instanceof Integer) {
                    this.watchingAuctionId = (Integer) request;
                }
            }
        } catch (Exception e) {
            AuctionServer.removeClient(this);
        }
    }

    public void sendEvent(AuctionEvent event) {
        try {
            if (event.getAuctionId() == this.watchingAuctionId && out != null) {
                out.writeObject(event);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}