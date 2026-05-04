<<<<<<< HEAD:src/main/java/controller/AuctionController.java
package controller;

import core.AuctionSocketClient;
import model.AuctionEvent;
import model.ClientRequest;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AuctionController {
    @FXML private Label lblCurrentPrice;
    @FXML private TextField txtBidAmount;
    
    // Giả lập trạng thái session người dùng (Thay vì hardcode cứng vào các hàm)
    private int currentAuctionId = 1; 
    private int loggedInUserId = 2; 

    @FXML
    public void initialize() {
        // Lắng nghe sự kiện từ Socket Server
        AuctionSocketClient.getInstance().setOnEventReceived(this::handleServerEvent);
        
        // Gửi yêu cầu theo dõi phiên đấu giá hiện tại khi vừa mở form
        watchAuction(currentAuctionId);
    }

    private void watchAuction(int auctionId) {
        ClientRequest req = new ClientRequest("WATCH", auctionId, loggedInUserId, 0.0);
        AuctionSocketClient.getInstance().sendRequest(req);
    }

    @FXML
    private void onPlaceBidClick() {
        try {
            double amount = Double.parseDouble(txtBidAmount.getText());
            
            // Gửi request đặt giá lên Server
            ClientRequest req = new ClientRequest("BID", currentAuctionId, loggedInUserId, amount);
            AuctionSocketClient.getInstance().sendRequest(req);
            
            // Xóa trắng ô nhập sau khi gửi thành công
            txtBidAmount.clear(); 
            
        } catch (NumberFormatException e) {
            // Báo lỗi ngay lập tức nếu người dùng nhập chữ hoặc bỏ trống
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Vui lòng nhập số tiền hợp lệ!");
        }
    }

    private void handleServerEvent(AuctionEvent event) {
        Platform.runLater(() -> {
            switch (event.getType()) {
                case NEW_BID:
                    // Cập nhật giá mới lên màn hình
                    lblCurrentPrice.setText("Giá hiện tại: " + event.getData().toString());
                    break;
                    
                case OUTBID:
                    // Thông báo bị người khác vượt giá
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", event.getData().toString());
                    break;
                    
                case TIME_EXTENDED:
                    // Thông báo Anti-sniping 
                    showAlert(Alert.AlertType.INFORMATION, "Gia hạn thời gian", event.getData().toString());
                    break;
                    
                case ERROR:
                    // Bắt các ngoại lệ Custom Exception từ Server 
                    showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", event.getData().toString());
                    break;
                    
                case AUCTION_FINISHED:
                    // Thông báo kết thúc phiên
                    showAlert(Alert.AlertType.INFORMATION, "Kết thúc", event.getData().toString());
                    break;
            }
        });
    }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
=======
package controller;
import com.google.gson.Gson;
import core.AuctionSocketClient;
import model.AuctionEvent;
import model.ClientRequest;
import javafx.fxml.FXML;
import javafx.scene.control.*;
public class AuctionController {
    @FXML private Label lblCurrentPrice;
    @FXML private TextField txtBidAmount;
    private Gson gson = new Gson();
    @FXML
    public void initialize() {
        AuctionSocketClient.getInstance().setOnEventReceived(this::handleServerEvent);
        watchAuction(1);
    }
    private void watchAuction(int auctionId) {
        ClientRequest req = new ClientRequest("WATCH", auctionId, 0, 0.0);
        AuctionSocketClient.getInstance().sendRequest(gson.toJson(req));
    }

    @FXML
    private void onPlaceBidClick() {
        try {
            double amount = Double.parseDouble(txtBidAmount.getText());
            int currentAuctionId = 1; 
            int currentUserId = 2;
            ClientRequest req = new ClientRequest("BID", currentAuctionId, currentUserId, amount);
            String jsonRequest = gson.toJson(req);
            AuctionSocketClient.getInstance().sendRequest(jsonRequest);
            
            System.out.println("Đã gửi yêu cầu đặt giá: " + jsonRequest);
            
        } catch (NumberFormatException e) {
            showError("Vui lòng nhập số tiền hợp lệ!");
        }
    }
    private void handleServerEvent(AuctionEvent event) {
        switch (event.getType()) {
            case NEW_BID:
                lblCurrentPrice.setText("Giá hiện tại: " + event.getData());
                break;
            case OUTBID:
                showWarning(event.getData().toString());
                break;
            case TIME_EXTENDED:
                showInfo(event.getData().toString());
                break;
        }
    }
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
>>>>>>> 9dae7d294058ef9a9d4806967b3a4466fd0dd667:AuctionSystem/src/main/java/controller/AuctionController.java
}