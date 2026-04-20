package com.uet.auction;
import com.uet.auction.core.AuctionSocketClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // 1. Kết nối mạng trước
        AuctionSocketClient.getInstance().connect("localhost", 1234);
        
        // 2. Load View (FXML)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AuctionView.fxml"));
        Scene scene = new Scene(loader.load());
        
        stage.setTitle("UET Auction System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
