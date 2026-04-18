import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionServer {
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static final ExecutorService clientPool = Executors.newFixedThreadPool(100);

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("Auction Server is running on port 1234...");
        
        while (true) { 
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket);
            clients.add(handler);
            clientPool.execute(handler); 
        }
    }

    public static void broadcast(AuctionEvent event) {
        for (ClientHandler client : clients) {
            client.sendEvent(event);
        }
    }

    public static void removeClient(ClientHandler handler) {
        clients.remove(handler);
    }
}