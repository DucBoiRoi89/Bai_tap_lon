package com.uet.auction.core;
import com.uet.auction.model.AuctionEvent;
import java.io.*;
import java.net.*;

public class AuctionClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 1234);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            System.out.println("Da ket noi Auction Server!");
            
            // Giả lập gửi yêu cầu xem phiên đấu giá số 1
            out.writeObject(1); 
            out.flush();

            // Chờ nhận dữ liệu realtime từ Server
            while (true) {
                Object event = in.readObject();
                if (event instanceof AuctionEvent) {
                    AuctionEvent e = (AuctionEvent) event;
                    System.out.println("Thon bao moi tu sever: " + e.getType());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
