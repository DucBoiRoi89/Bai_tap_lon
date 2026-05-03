import core.AuctionSocketClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        AuctionSocketClient.getInstance().connect("localhost", 1234);
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
