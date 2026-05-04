<<<<<<< HEAD:src/main/java/core/AuctionSocketClient.java
package core;

import com.google.gson.Gson;
import model.AuctionEvent;
import javafx.application.Platform;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class AuctionSocketClient {
    private static AuctionSocketClient instance;
    private PrintWriter out;
    private Consumer<AuctionEvent> onEventReceived;
    private Gson gson = new Gson(); 

    private AuctionSocketClient() {}
    public static AuctionSocketClient getInstance() {
        if (instance == null) instance = new AuctionSocketClient();
        return instance;
    }

    public void connect(String host, int port) {
        new Thread(() -> {
            try (Socket socket = new Socket(host, port)) {
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
                String responseLine;
                while ((responseLine = in.readLine()) != null) {
                    AuctionEvent event = gson.fromJson(responseLine, AuctionEvent.class);
                    if (event != null && onEventReceived != null) {
                        Platform.runLater(() -> onEventReceived.accept(event));
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    public void sendRequest(Object request) {
        if (out != null) out.println(gson.toJson(request));
    }

    public void setOnEventReceived(Consumer<AuctionEvent> handler) { this.onEventReceived = handler; }
=======
package core;
import com.google.gson.Gson;
import model.AuctionEvent;
import javafx.application.Platform;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;
public class AuctionSocketClient {
    private static AuctionSocketClient instance;
    private PrintWriter out;
    private Consumer<AuctionEvent> onEventReceived;
    private Gson gson = new Gson(); // Khởi tạo Gson
    private AuctionSocketClient() {}
    public static AuctionSocketClient getInstance() {
        if (instance == null) instance = new AuctionSocketClient();
        return instance;
    }
    public void connect(String host, int port) {
        new Thread(() -> {
            try (Socket socket = new Socket(host, port)) {
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
                String responseLine;
                while ((responseLine = in.readLine()) != null) {
                    AuctionEvent event = gson.fromJson(responseLine, AuctionEvent.class);
                    if (event != null && onEventReceived != null) {
                        Platform.runLater(() -> onEventReceived.accept(event));
                    }
                }
            } catch (Exception e) { 
                e.printStackTrace(); 
            }
        }).start();
    }
    public void sendRequest(Object request) {
        if (out != null) {
            out.println(request.toString());
        }
    }
    public void setOnEventReceived(Consumer<AuctionEvent> handler) {
        this.onEventReceived = handler;
    }
>>>>>>> 9dae7d294058ef9a9d4806967b3a4466fd0dd667:AuctionSystem/src/main/java/core/AuctionSocketClient.java
}