package clientapp.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;

public class ControllerChat {
    public TextArea txtMsgSend;
    public TextArea txtChat;
    public Button btnMsgSend;
    public Button btnOpenStorage;
    public Button btnExitChat;

    public void run() throws IOException {
        FXMLLoader fxmlLoaderRegistration = new FXMLLoader();
        fxmlLoaderRegistration.setLocation(getClass().getResource("/chat.fxml"));
        Stage stage = new Stage();
        Parent root = fxmlLoaderRegistration.load();
        Scene scene = new Scene(root);
        stage.setTitle("Чат & Файловое хранилище");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.showAndWait();
    }

    public void sendMsg(ActionEvent actionEvent) {
    }

    public void openStorage(ActionEvent actionEvent) throws IOException {
        new ControllerStorage().run();
    }

    public void exitChat(ActionEvent actionEvent) {
        // закрываем канал
        Controller.currentChannel.close();
        // закрываем текущее окно
        Stage stage = (Stage) btnExitChat.getScene().getWindow();
        stage.close();
    }
}
