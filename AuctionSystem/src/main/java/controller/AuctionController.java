package controller;

import java.io.IOException;

import dao.AuctionDAO;
import model.UserSession;
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

    private AuctionDAO auctionDAO = new AuctionDAO();
    private int currentAuctionId; 
    private double currentMaxPrice = 0.0;

    public void setAuctionDetails(int auctionId, double currentPrice) {
        this.currentAuctionId = auctionId;
        this.currentMaxPrice = currentPrice;
        // Cập nhật giao diện ngay khi load trang chi tiết
        lblCurrentPrice.setText(String.format("%,.0f VNĐ", currentPrice));
    }

    @FXML
    private void onPlaceBidClick(ActionEvent event) {
        try {
            double bidAmount = Double.parseDouble(txtBidAmount.getText());
            
            // LẤY ID NGƯỜI DÙNG TỪ SESSION: Fix lỗi không đặt được giá
            int loggedInUserId = UserSession.getLoggedInUser().getUserId();
            
            // Gọi Stored Procedure với ID thực tế
            int statusCode = auctionDAO.placeSingleBid(bidAmount, loggedInUserId, currentAuctionId);

            if (statusCode == 1) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Bạn đã đặt giá thành công!");
                this.currentMaxPrice = bidAmount;
                lblCurrentPrice.setText(String.format("%,.0f VNĐ", currentMaxPrice));
                txtBidAmount.clear();
            } else if (statusCode == 0) {
                showAlert(Alert.AlertType.WARNING, "Thất bại", "Giá đặt phải cao hơn " + String.format("%,.0f", currentMaxPrice));
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Phiên đấu giá không hợp lệ hoặc đã đóng.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập số tiền hợp lệ!");
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null);
        alert.setContentText(content); alert.showAndWait();
    }
    @FXML
private void handleOpenAutoBid() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AutoBidDialog.fxml"));
        Parent root = loader.load();
        
        AutoBidController ctrl = loader.getController();
        ctrl.setAuctionId(this.currentAuctionId); 
        
        Stage stage = new Stage();
        stage.setTitle("Cấu hình Auto Bid");
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) { e.printStackTrace(); }
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
    } catch (IOException e) {
        new Alert(Alert.AlertType.ERROR, "Không thể mở giao diện Auto Bid!").show();
        e.printStackTrace();
    }
}
}