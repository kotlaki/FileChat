package clientapp.controllers;

import clientapp.CallbackMsgPrivate;
import clientapp.ClientHandler;
import common.MyCommandSend;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.Arrays;
import java.util.ResourceBundle;

public class ControllerChat implements Initializable {
    public TextArea txtMsgSend;
    public TextArea txtChat;
    public Button btnMsgSend;
    public Button btnOpenStorage;
    public Button btnExitChat;
    public ListView<String> listUser;

    public static String clientListFromServer;
    public static String message;
    public static String nickTo;

    public void setCallbackMsgPrivate(CallbackMsgPrivate callbackMsgPrivate) {
        Controller.currentChannel.pipeline().get(ClientHandler.class).setCallbackMsgPrivate(callbackMsgPrivate);
    }

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
        txtChat.appendText("Добро пожаловать, " + Controller.nick + "!!!\n");
        Controller.linkController.setCallbackClientList(this::clientList);  // получаем список активных пользователей
        Controller.linkController.setCallbackMsgAll(() -> txtChat.appendText(message + "\n"));  // ждем сообщений и выводим их
    }

    public void sendMsg(ActionEvent actionEvent) throws IOException {
            MyCommandSend.sendCommand("/msgAll&" + txtMsgSend.getText(), Controller.currentChannel);
            Controller.linkController.setCallbackConfirm(() -> {
                txtChat.appendText("Я пишу: " + txtMsgSend.getText() + "\n");
                txtMsgSend.clear();
            });
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

    public void clientList(){
        Platform.runLater(()->{
            String[] strSplit = clientListFromServer.split(" ");
            String[] result = new String[strSplit.length - 1];
            System.arraycopy(strSplit, 1, result, 0, strSplit.length - 1);
            ObservableList<String> clientList = FXCollections.observableArrayList(Arrays.asList(result));
            listUser.setItems(clientList);

            listUser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.equals(Controller.nick)) {
                    nickTo = newValue;
                    new ControllerPrivateChat().run(nickTo);
                }
            });
        });
    }


}
