package controller;

import dao.AuctionDAO;
import dao.ItemDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class AuctionListController {

    @FXML private FlowPane itemContainer;
    @FXML private Label lblFullNameHeader, lblItemName, lblBrandE, lblWarranty, lblAuthor, lblYear, lblBrandV, lblLicense, lblMileage;
    @FXML private TextArea txtDescription;
    @FXML private GridPane gridElectronics, gridArt, gridVehicle;
    @FXML private Button btnManageInventory;
   @FXML private TableView<BidRecord> historyTable;
    @FXML private Label lblUserRole; 
    @FXML private TextField txtUserId, txtFullName, txtBalance; // Đã thêm txtBalance
    @FXML private TableColumn<BidRecord, String> colItemName, colBidAmount, colBidTime;
    @FXML private AuctionController liveAuctionViewController;

    private ItemDAO itemDAO = new ItemDAO();

    @FXML
    public void initialize() {
        User user = UserSession.getLoggedInUser();
        if (historyTable != null && user != null) {
        colItemName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("itemName"));
        
        // Custom hiển thị giá tiền có VNĐ
        colBidAmount.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedPrice()));
            
        // Custom hiển thị thời gian định dạng Việt Nam
        colBidTime.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedTime()));

        // Nạp dữ liệu từ Database
        AuctionDAO dao = new AuctionDAO();
        historyTable.setItems(javafx.collections.FXCollections.observableArrayList(
            dao.getBidHistoryByUser(user.getUserId())
        ));
    }
        if (user != null) {
            String role = user.getRole().toUpperCase();

            if (lblFullNameHeader != null) lblFullNameHeader.setText(user.getUsername());

            if (lblUserRole != null) lblUserRole.setText("Vai trò: " + role);
            if (txtUserId != null) txtUserId.setText(String.valueOf(user.getUserId()));
            if (txtFullName != null) txtFullName.setText(user.getUsername());
            
            // Xử lý hiển thị số dư cho BIDDER
            if (txtBalance != null) {
                if (user instanceof Bidder bidder) {
                    txtBalance.setText(String.format("%,.0f VNĐ", bidder.getBalance()));
                } else {
                    txtBalance.setText("Không áp dụng");
                }
            }

            if (btnManageInventory != null) {
                switch (role) {
                    case "BIDDER":
                        btnManageInventory.setVisible(false);
                        btnManageInventory.setManaged(false);
                        break;
                    case "SELLER":
                    case "ADMIN":
                        btnManageInventory.setVisible(true);
                        btnManageInventory.setManaged(true);
                        break;
                    default:
                        btnManageInventory.setVisible(false);
                        btnManageInventory.setManaged(false);
                        break;
                }
            }
        }
        
        if (itemContainer != null) {
            loadAuctionItems();
        }
    }   
    private void loadAuctionItems() {
        itemContainer.getChildren().clear();
        List<Item> items = itemDAO.getAllItems();
        for (Item item : items) {
            itemContainer.getChildren().add(createItemCard(item));
        }
    }

    private VBox createItemCard(Item item) {
        VBox card = new VBox(3); 
        card.setPrefSize(175, 200);
        card.setAlignment(Pos.TOP_CENTER);
        
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 5); " +
                      "-fx-padding: 10;");

        Label lblName = new Label(item.getName());
        lblName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        Label lblPrice = new Label(String.format("%,.0f VNĐ", item.getCurrentPrice()));
        lblPrice.setStyle("-fx-background-color: #dca742; -fx-text-fill: white; " +
                          "-fx-padding: 5 15 5 15; -fx-background-radius: 3;");
        lblPrice.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        Label lblTime = new Label();
        lblTime.setFont(Font.font("Segoe UI", 13));
        lblTime.setStyle("-fx-text-fill: #7f8c8d;");

        Button btnBid = new Button("BID NOW");
        btnBid.setPrefWidth(175);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = item.getEndTime();
        
        if (endTime != null) {
            Duration duration = Duration.between(now, endTime);

            if (duration.isNegative() || duration.isZero()) {
                int winnerId = new dao.AuctionDAO().getHighestBidderId(item.getAuctionId());
                if (winnerId != -1) {
                    String winnerName = "User #" + winnerId;
                    for (model.User u : new dao.UserDAO().getAllUsers()) {
                        if (u.getUserId() == winnerId) {
                            winnerName = u.getUsername();
                            break;
                        }
                    }
                    lblTime.setText("KẾT THÚC - Thắng: " + winnerName);
                } else {
                    lblTime.setText("KẾT THÚC - Không có người mua");
                }
                lblTime.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                btnBid.setText("ĐÃ ĐÓNG");
                btnBid.setDisable(true); 
                btnBid.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; -fx-background-radius: 3;");
            } else {
                long hours = duration.toHours();
                long minutes = duration.toMinutes() % 60;
                long seconds = duration.getSeconds() % 60;
                
                lblTime.setText(String.format("Còn lại: %02dh %02dm %02ds", hours, minutes, seconds));
                btnBid.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-background-radius: 3; -fx-cursor: hand;");
                btnBid.setOnAction(event -> handleViewDetail(item, event));
            }
        }

        card.getChildren().addAll(lblName, lblPrice, lblTime, btnBid);
        return card;
    }

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
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) { 
            e.printStackTrace(); 
        }
    }

    public void setItemData(Item item) {
        lblItemName.setText(item.getName());
        txtDescription.setText(item.getDescription());
        
        if (gridElectronics != null) { gridElectronics.setVisible(item instanceof Electronics); gridElectronics.setManaged(item instanceof Electronics); }
        if (gridArt != null) { gridArt.setVisible(item instanceof Art); gridArt.setManaged(item instanceof Art); }
        if (gridVehicle != null) { gridVehicle.setVisible(item instanceof Vehicle); gridVehicle.setManaged(item instanceof Vehicle); }

        if (item instanceof Electronics e) { lblBrandE.setText(e.getBrand()); lblWarranty.setText(e.getWarrantyMonths() + " tháng"); }
        else if (item instanceof Art a) { lblAuthor.setText(a.getArtist()); lblYear.setText(String.valueOf(a.getYearCreated())); }
        else if (item instanceof Vehicle v) { lblBrandV.setText(v.getBrand()); lblLicense.setText(v.getLicensePlate()); lblMileage.setText(v.getMileage() + " km"); }
    }

    @FXML private void handleGoToHome(ActionEvent e) { switchScene(e, "BiddingView.fxml"); }
    @FXML private void handleGoToProfile(ActionEvent e) { switchScene(e, "AccountProfile.fxml"); }
    @FXML private void handleGoToHistory(ActionEvent e) { switchScene(e, "BidHistory.fxml"); }

    @FXML 
    private void handleShowAddDialog(ActionEvent e) { 
        User user = UserSession.getLoggedInUser();
        if (user != null && "BIDDER".equalsIgnoreCase(user.getRole())) {
            new Alert(Alert.AlertType.WARNING, "Bạn không có quyền truy cập khu vực này! Hãy đăng nhập tài khoản Người bán.").show();
            return;
        }
        switchScene(e, "MyProductsView.fxml"); 
    }

    private void switchScene(ActionEvent event, String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/" + fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) { 
            System.err.println("Lỗi nạp file FXML: " + fxml);
            e.printStackTrace(); 
        }
    }
    @FXML
    private void handleLogout(ActionEvent event) {
        // 1. Xóa phiên đăng nhập hiện tại
        UserSession.cleanUserSession();
        
        // 2. Chuyển hướng về màn hình Đăng nhập
        switchScene(event, "Login.fxml");}
}