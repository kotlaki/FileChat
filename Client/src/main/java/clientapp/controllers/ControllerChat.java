package clientapp.controllers;

import common.MyCommandSend;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ControllerChat implements Initializable {
    public TextArea txtMsgSend;
    public TextArea txtChat;
    public Button btnMsgSend;
    public Button btnOpenStorage;
    public Button btnExitChat;
    public ListView listUser;

    public ControllerChat() {

    }


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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtChat.appendText("Добро пожаловать, " + Controller.nick + "!!!");
    }

    public void sendMsg(ActionEvent actionEvent) {
    }

    public void openStorage(ActionEvent actionEvent) throws IOException {
        new ControllerStorage().run();
    }

    public void exitChat(ActionEvent actionEvent) throws IOException {
        // посылаем на сервер команду об отключении пользователя
        MyCommandSend.sendCommand("/authOFF", Controller.currentChannel);
        System.out.println(Controller.nick + " послал команду на сервер об отключении...");
        // закрываем канал
        Controller.currentChannel.close();
        // закрываем текущее окно
        Stage stage = (Stage) btnExitChat.getScene().getWindow();
        stage.close();
    }


}
