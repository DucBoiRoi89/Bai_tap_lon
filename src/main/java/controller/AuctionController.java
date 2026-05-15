package controller;

import java.io.IOException;
import core.AuctionSocketClient;
import model.AuctionEvent;
import model.ClientRequest;
import model.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AuctionController {
    @FXML private Label lblCurrentPrice;
    @FXML private TextField txtBidAmount;
    @FXML private Label lblTimeRemaining;

    private int currentAuctionId; 
    private double currentMaxPrice = 0.0;

    public void setAuctionDetails(int auctionId, double currentPrice) {
        this.currentAuctionId = auctionId;
        this.currentMaxPrice = currentPrice;
        updatePriceUI(currentPrice);

        // 1. Đăng ký nhận sự kiện realtime từ Server
        int userId = UserSession.getLoggedInUser() != null ? UserSession.getLoggedInUser().getUserId() : -1;
        AuctionSocketClient.getInstance().sendRequest(new ClientRequest("WATCH", auctionId, userId, 0));
        AuctionSocketClient.getInstance().setOnEventReceived(this::handleServerEvent);
    }

    // 2. Lắng nghe Server trả kết quả về (Gồm cả Auto-bid của Robot)
    private void handleServerEvent(AuctionEvent event) {
        if (event.getItemId() != this.currentAuctionId) return;

        Platform.runLater(() -> {
            switch (event.getType()) {
               case NEW_BID:
                    double newPrice = ((Number) event.getData()).doubleValue();
                    this.currentMaxPrice = newPrice;
                    updatePriceUI(newPrice);
                    if (UserSession.getLoggedInUser() instanceof model.Bidder bidder) {
                        dao.UserDAO uDao = new dao.UserDAO();
                        double newBalance = uDao.getUserBalance(bidder.getUserId());
                        bidder.setBalance(newBalance);
                    }
                    
                    System.out.println("Giá mới cập nhật: " + newPrice);
                    break;
                case ERROR:
                    showAlert(Alert.AlertType.ERROR, "Lỗi", String.valueOf(event.getData()));
                    break;
                case OUTBID:
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", String.valueOf(event.getData()));
                    break;
                case AUCTION_FINISHED:
                    txtBidAmount.setDisable(true);
                    showAlert(Alert.AlertType.INFORMATION, "Kết thúc", String.valueOf(event.getData()));
                    break;
            }
        });
    }

    @FXML
    private void onPlaceBidClick(ActionEvent event) {
        try {
            double bidAmount = Double.parseDouble(txtBidAmount.getText());
            int loggedInUserId = UserSession.getLoggedInUser().getUserId();
            
            // Tích hợp trực tiếp AuctionService (giống như file Test CLI) để đảm bảo luôn chạy
            service.AuctionService auctionService = new service.AuctionService();
            try {
                auctionService.processBid(currentAuctionId, loggedInUserId, bidAmount);
                
                // Lấy lại giá cao nhất ngay lập tức từ DB (Bao gồm cả giá Bot tự động đè nếu có)
                double newPrice = auctionService.getAuctionDAO().getCurrentMaxPrice(currentAuctionId);
                this.currentMaxPrice = newPrice > 0 ? newPrice : bidAmount;
                updatePriceUI(this.currentMaxPrice);
                
                // Cập nhật lại số dư trên Giao diện
                if (UserSession.getLoggedInUser() instanceof model.Bidder bidder) {
                    bidder.setBalance(new dao.UserDAO().getUserBalance(loggedInUserId));
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã ghi nhận lượt đặt giá của bạn!");
                
            } catch (exception.InvalidBidException | exception.AuctionClosedException e) {
                showAlert(Alert.AlertType.WARNING, "Từ chối", e.getMessage());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
            }
            
            txtBidAmount.clear();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập số tiền hợp lệ!");
        }
    }
    
    private void updatePriceUI(double price) {
        lblCurrentPrice.setText(String.format("%,.0f VNĐ", price));
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null);
        alert.setContentText(content); alert.showAndWait();
    }

    @FXML
    private void handleOpenAutoBid(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AutoBidDialog.fxml"));
            Parent root = loader.load();
            AutoBidController ctrl = loader.getController();
            ctrl.setAuctionId(this.currentAuctionId); 
            
            Stage stage = new Stage();
            stage.setTitle("Cấu hình Đấu giá tự động");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); 
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}