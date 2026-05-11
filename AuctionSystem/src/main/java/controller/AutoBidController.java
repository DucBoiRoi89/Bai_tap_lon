package controller;

import dao.AuctionDAO;
import model.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

public class AutoBidController {
    @FXML private TextField txtMaxBid, txtIncrement;
    private int currentAuctionId;
    private AuctionDAO auctionDAO = new AuctionDAO();

    public void setAuctionId(int auctionId) { this.currentAuctionId = auctionId; }

    @FXML
    private void handleActivate() {
        try {
            double maxBid = Double.parseDouble(txtMaxBid.getText());
            double increment = Double.parseDouble(txtIncrement.getText());
            int userId = UserSession.getLoggedInUser().getUserId();

            if (auctionDAO.saveAutoBid(currentAuctionId, userId, maxBid, increment)) {
                new Alert(Alert.AlertType.INFORMATION, "Đã kích hoạt robot đấu giá cho bạn!").show();
                ((Stage) txtMaxBid.getScene().getWindow()).close();
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Vui lòng nhập số hợp lệ!").show();
        }
    }

    @FXML private void handleCancel() { ((Stage) txtMaxBid.getScene().getWindow()).close(); }
}