// File: AuctionSocketClient.java
package com.uet.auction.core;

import com.uet.auction.model.AuctionEvent; //
import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class AuctionSocketClient {
    private static AuctionSocketClient instance;
    private ObjectOutputStream out;
    private Consumer<AuctionEvent> onEventReceived;

    private AuctionSocketClient() {}

    public static AuctionSocketClient getInstance() {
        if (instance == null) instance = new AuctionSocketClient();
        return instance;
    }

    public void connect(String host, int port) {
        new Thread(() -> {
            try (Socket socket = new Socket(host, port)) {
                out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                
                while (true) {
                    Object obj = in.readObject();
                    if (obj instanceof AuctionEvent && onEventReceived != null) {
                        AuctionEvent event = (AuctionEvent) obj;
                        Platform.runLater(() -> onEventReceived.accept(event));
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    public void sendRequest(Object request) {
        try {
            if (out != null) {
                out.writeObject(request);
                out.flush();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void setOnEventReceived(Consumer<AuctionEvent> handler) {
        this.onEventReceived = handler;
    }
}