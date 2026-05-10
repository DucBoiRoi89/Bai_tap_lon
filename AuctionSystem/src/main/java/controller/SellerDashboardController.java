package controller;

import dao.AuctionDAO;
import dao.ItemDAO;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class SellerDashboardController {
    // --- BẢNG SẢN PHẨM ---
    @FXML private TableView<Item> tableItems;
    @FXML private TableColumn<Item, String> colId, colName, colDesc;
    @FXML private TableColumn<Item, Double> colPrice;
    @FXML private TableColumn<Item, LocalDateTime> colEndTime;

    // --- POP-UP THÊM MỚI ---
    @FXML private VBox dynamicFieldsContainer; 
    @FXML private TextField txtItemName, txtStartingPrice;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cbCategory;
    @FXML private DatePicker dpEndTime;
    @FXML private Label lblDialogTitle;
    @FXML private Button btnSave;

    private TextField txtBrand, txtWarranty, txtAuthor, txtYear, txtLicense, txtMileage;
    private AuctionDAO auctionDAO = new AuctionDAO();
    private ItemDAO itemDAO = new ItemDAO();

    @FXML 
    public void initialize() {
        if (cbCategory != null) {
            cbCategory.setItems(FXCollections.observableArrayList("ELECTRONICS", "ART", "VEHICLE"));
        }
        if (tableItems != null) {
            colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
            colName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
            colPrice.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("startingPrice"));
            colDesc.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("description"));
            colEndTime.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("endTime"));
            
            // Định dạng ngày tháng hiển thị trên bảng
            colEndTime.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) setText(null);
                    else setText(item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
            });
            loadTableData();
        }
    }

    private void loadTableData() {
        tableItems.setItems(FXCollections.observableArrayList(itemDAO.getAllItems()));
    }

    @FXML
    private void handleCategoryChange() {
        dynamicFieldsContainer.getChildren().clear();
        String selected = cbCategory.getValue();
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        if ("ELECTRONICS".equals(selected)) {
            txtBrand = new TextField(); txtWarranty = new TextField();
            grid.addRow(0, new Label("Thương hiệu:"), txtBrand);
            grid.addRow(1, new Label("Bảo hành (tháng):"), txtWarranty);
        } else if ("ART".equals(selected)) {
            txtAuthor = new TextField(); txtYear = new TextField();
            grid.addRow(0, new Label("Tác giả:"), txtAuthor);
            grid.addRow(1, new Label("Năm sáng tác:"), txtYear);
        } else if ("VEHICLE".equals(selected)) {
            txtBrand = new TextField(); txtLicense = new TextField(); txtMileage = new TextField();
            grid.addRow(0, new Label("Hãng xe:"), txtBrand);
            grid.addRow(1, new Label("Biển số:"), txtLicense);
            grid.addRow(2, new Label("Số KM:"), txtMileage);
        }
        dynamicFieldsContainer.getChildren().add(grid);
    }

    @FXML 
    private void handleSaveProduct(ActionEvent event) {
        try {
            // FIX LỖI: Khai báo biến end và price đúng phạm vi
            double price = Double.parseDouble(txtStartingPrice.getText());
            LocalDateTime end = dpEndTime.getValue().atTime(23, 59);
            String cat = cbCategory.getValue();

            Map<String, Object> details = new HashMap<>();
            if ("ELECTRONICS".equals(cat)) {
                details.put("brand", txtBrand.getText());
                details.put("warrantyMonths", Integer.parseInt(txtWarranty.getText()));
            } else if ("ART".equals(cat)) {
                details.put("artist", txtAuthor.getText());
                details.put("yearCreated", Integer.parseInt(txtYear.getText()));
            } else if ("VEHICLE".equals(cat)) {
                details.put("brand", txtBrand.getText());
                details.put("licensePlate", txtLicense.getText());
                details.put("mileage", Long.parseLong(txtMileage.getText()));
            }

            Item newItem = ItemFactory.createItem(cat, "0", txtItemName.getText(), 
                        txtDescription.getText(), price, end, details);
            
            if (auctionDAO.insertItem(newItem, UserSession.getLoggedInUser().getUserId())) {
                ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
            }
        } catch (Exception e) { 
            new Alert(Alert.AlertType.ERROR, "Vui lòng nhập đúng định dạng số!").show();
        }
    }

    @FXML
    private void handleStartAuction(ActionEvent event) {
        Item selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected != null && auctionDAO.startAuction(Integer.parseInt(selected.getId()))) {
            new Alert(Alert.AlertType.INFORMATION, "Phiên đấu giá đã bắt đầu!").show();
            loadTableData();
        }
    }

    @FXML 
    private void handleDeleteProduct(ActionEvent event) {
        Item selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected != null) {
            int sellerId = UserSession.getLoggedInUser().getUserId();
            if (auctionDAO.deleteItem(Integer.parseInt(selected.getId()), sellerId)) {
                loadTableData();
            } else {
                new Alert(Alert.AlertType.ERROR, "Sản phẩm đang đấu giá, không thể xóa!").show();
            }
        }
    }

    // --- CÁC HÀM ĐIỀU HƯỚNG BẮT BUỘC ĐỂ KHÔNG LỖI NẠP FXML ---
    @FXML private void handleGoToHome(ActionEvent e) { switchScene(e, "BiddingView.fxml"); }
    @FXML private void handleGoToProfile(ActionEvent e) { switchScene(e, "AccountProfile.fxml"); }
    @FXML private void handleLoadMyProducts(ActionEvent e) { loadTableData(); }
    @FXML private void handleShowAddDialog(ActionEvent e) throws IOException { openDialog(); }
    @FXML 
private void handleShowUpdateDialog(ActionEvent e) throws IOException {
    Item selected = tableItems.getSelectionModel().getSelectedItem();
    if (selected == null) {
        new Alert(Alert.AlertType.WARNING, "Vui lòng chọn sản phẩm cần sửa!").show();
        return;
    }
    
    // Mở Dialog và truyền dữ liệu sản phẩm vào để sửa
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddItemDialog.fxml"));
    Parent root = loader.load();
    
    // Lấy controller của dialog để gán dữ liệu cũ vào các ô nhập
    SellerDashboardController dialogCtrl = loader.getController();
    dialogCtrl.setEditingData(selected); 
    
    Stage stage = new Stage();
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.setScene(new Scene(root));
    stage.showAndWait();
    loadTableData(); // Load lại bảng sau khi đóng dialog
}

// Hàm bổ trợ để hiển thị dữ liệu cũ lên Dialog
public void setEditingData(Item item) {
    lblDialogTitle.setText("SỬA THÔNG TIN SẢN PHẨM");
    btnSave.setText("CẬP NHẬT");
    txtItemName.setText(item.getName());
    txtStartingPrice.setText(String.valueOf(item.getStartingPrice()));
    txtDescription.setText(item.getDescription());
    dpEndTime.setValue(item.getEndTime().toLocalDate());
    cbCategory.setValue(item.getCategory());
    cbCategory.setDisable(true); // Không cho sửa loại sản phẩm để tránh lỗi logic
}

    private void openDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddItemDialog.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.showAndWait();
        loadTableData();
    }

    private void switchScene(ActionEvent event, String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/" + fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    @FXML private void handleCancel(ActionEvent e) { ((Stage)((Node)e.getSource()).getScene().getWindow()).close(); }
}