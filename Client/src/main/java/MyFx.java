import clientapp.ChatController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;

public class MyFx extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        URL urlFxml = getClass().getResource("/enter.fxml");
        ChatController controller = loader.getController();
        loader.setLocation(urlFxml);
        Parent root = loader.load();
//        primaryStage.resizableProperty().setValue(false);
        primaryStage.resizableProperty().setValue(false);
        primaryStage.setTitle("Авторизация");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
