
import core.AuctionSocketClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
 
        AuctionSocketClient.getInstance().connect("10.227.75.45", 1234);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
        Scene scene = new Scene(loader.load());
        
        // Bắt buộc nhúng style.css vào Scene phòng trường hợp FXML quên nhúng
        scene.getStylesheets().add(getClass().getResource("/views/style.css").toExternalForm());

        stage.setTitle("UET Auctions - Đăng nhập");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) { 
        launch(args); 
    }
}