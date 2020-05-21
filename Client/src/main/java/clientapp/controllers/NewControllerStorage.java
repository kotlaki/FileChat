package clientapp.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.io.IOException;

public class NewControllerStorage {
    public ListView listFileLeft;
    public ListView listFileRight;
    public Button btnCancelStorage;
    public Button btnLeftToRight;
    public Button btnRightToLeft;
    public ProgressBar progressBar;

    public void run() throws IOException {
        FXMLLoader fxmlLoaderRegistration = new FXMLLoader();
        fxmlLoaderRegistration.setLocation(getClass().getResource("/new/storage.fxml"));
        Stage stage = new Stage();
        Parent root = fxmlLoaderRegistration.load();
        Scene scene = new Scene(root);
        stage.setTitle("Регистрация");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.showAndWait();
    }

    public void sendFileToServer(ActionEvent actionEvent) {
    }

    public void receiveFileFromServer(ActionEvent actionEvent) {
    }

    public void cancelStorage(ActionEvent actionEvent) {
        // закрываем текущее окно
        Stage stage = (Stage) btnCancelStorage.getScene().getWindow();
        stage.close();
    }
}
