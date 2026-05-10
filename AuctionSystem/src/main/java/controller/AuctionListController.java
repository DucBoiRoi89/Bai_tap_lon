package controller;

import dao.ItemDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.*;
import java.io.IOException;
import java.util.List;

public class AuctionListController {
    @FXML private FlowPane itemContainer;
    @FXML private Label lblFullNameHeader, lblItemName, lblBrandE, lblWarranty, lblAuthor, lblYear, lblBrandV, lblLicense, lblMileage;
    @FXML private TextArea txtDescription;
    @FXML private GridPane gridElectronics, gridArt, gridVehicle;
    @FXML private Button btnManageInventory;

    private ItemDAO itemDAO = new ItemDAO();

    @FXML
    public void initialize() {
        User user = UserSession.getLoggedInUser();
        if (user != null && lblFullNameHeader != null) {
            lblFullNameHeader.setText(user.getUsername());
            if (btnManageInventory != null) {
                boolean isSeller = !"BIDDER".equalsIgnoreCase(user.getRole());
                btnManageInventory.setVisible(isSeller);
                btnManageInventory.setManaged(isSeller);
            }
        }
        if (itemContainer != null) loadAuctionItems();
    }

    private void loadAuctionItems() {
        itemContainer.getChildren().clear();
        List<Item> items = itemDAO.getAllItems();
        for (Item item : items) {
            itemContainer.getChildren().add(createItemCard(item));
        }
    }

    private VBox createItemCard(Item item) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setPrefWidth(220);
        Label name = new Label(item.getName());
        name.setStyle("-fx-font-weight: bold;");
        Label price = new Label(String.format("%,.0f VNĐ", item.getCurrentPrice()));
        price.setStyle("-fx-text-fill: #dca742; -fx-font-weight: bold;");
        Button btn = new Button("XEM CHI TIẾT");
        btn.setOnAction(e -> handleViewDetail(item, e));
        card.getChildren().addAll(name, price, btn);
        return card;
    }
@FXML private AuctionController liveAuctionViewController;
    private void handleViewDetail(Item item, ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ProductDetail.fxml"));
        Parent root = loader.load();
        
        AuctionListController detailCtrl = loader.getController();
        detailCtrl.setItemData(item);
        if (detailCtrl.liveAuctionViewController != null) {
            detailCtrl.liveAuctionViewController.setAuctionDetails(
                item.getAuctionId(), 
                item.getCurrentPrice()
            );
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    } catch (IOException e) { e.printStackTrace(); }
}
    public void setItemData(Item item) {
        lblItemName.setText(item.getName());
        txtDescription.setText(item.getDescription());
        
        // Ẩn/Hiện các Grid chi tiết dựa trên loại sản phẩm
        if (gridElectronics != null) { gridElectronics.setVisible(item instanceof Electronics); gridElectronics.setManaged(item instanceof Electronics); }
        if (gridArt != null) { gridArt.setVisible(item instanceof Art); gridArt.setManaged(item instanceof Art); }
        if (gridVehicle != null) { gridVehicle.setVisible(item instanceof Vehicle); gridVehicle.setManaged(item instanceof Vehicle); }

        if (item instanceof Electronics e) { lblBrandE.setText(e.getBrand()); lblWarranty.setText(e.getWarrantyMonths() + " tháng"); }
        else if (item instanceof Art a) { lblAuthor.setText(a.getArtist()); lblYear.setText(String.valueOf(a.getYearCreated())); }
        else if (item instanceof Vehicle v) { lblBrandV.setText(v.getBrand()); lblLicense.setText(v.getLicensePlate()); lblMileage.setText(v.getMileage() + " km"); }
    }

    @FXML private void handleGoToHome(ActionEvent e) { switchScene(e, "BiddingView.fxml"); }
    @FXML private void handleGoToProfile(ActionEvent e) { switchScene(e, "AccountProfile.fxml"); }
    @FXML private void handleShowAddDialog(ActionEvent e) { switchScene(e, "MyProductsView.fxml"); }

    @FXML 
private void handleGoToHistory(ActionEvent e) { 
    switchScene(e, "BidHistory.fxml"); // Hoặc tên file FXML lịch sử của bạn
}

// Đảm bảo hàm chuyển cảnh trỏ đúng thư mục views
private void switchScene(ActionEvent event, String fxml) {
    try {
        // Kiểm tra đường dẫn: nếu file FXML nằm trong thư mục /views/
        Parent root = FXMLLoader.load(getClass().getResource("/views/" + fxml));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    } catch (IOException e) { 
        System.err.println("Không tìm thấy file FXML: " + fxml);
        e.printStackTrace(); 
    }
}
}