package clientapp.controllers;

import common.MyCommandSend;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

    public String msgOut;

    public static String clientListFromServer;
    public static String message;
    public static String nickReceiver;
    public static String nickSender;

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
        Controller.linkController.setCallbackPrivateMsgReceive(()->{
            txtChat.appendText("Сообщение от " + nickSender + ": " + message + "\n");
        });
        listUser.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
               // выбираем из списка кому пишем приват
                // проверяем условие чтобы не открыть приват от себя к себе
                if (!Controller.nick.equals(newValue)) {
                    nickReceiver = newValue;
                    txtMsgSend.setText("Пишем " + nickReceiver + ":- ");
                    msgOut = "/pm&" + nickReceiver;
                    System.out.println(msgOut);
                }
            }
        });
    }

    public void sendMsg(ActionEvent actionEvent) throws IOException {
        // условие для отправки привата
        if (txtMsgSend.getText().startsWith("Пишем " + nickReceiver + ":-")) {
            String[] strSplit = txtMsgSend.getText().split(":-");
            System.out.println(strSplit.length);
            System.out.println("0 = " + strSplit[0] + " : 1 = " + strSplit[1]);
            MyCommandSend.sendCommand("/pm&" + nickReceiver + "&" + txtMsgSend.getText(), Controller.currentChannel);
            Controller.linkController.setCallbackConfirmReceivePrivate(()->{
//                txtChat.setStyle("-fx-text-inner-color: red;");
//                txtChat.setStyle("-fx-text-fill: #00ff00;");
                txtChat.appendText(txtMsgSend.getText() + "\n");
                txtMsgSend.clear();
            });
        } else {    // иначе отправляем сообщения всем активным пользователям
            MyCommandSend.sendCommand("/msgAll&" + txtMsgSend.getText(), Controller.currentChannel);
            Controller.linkController.setCallbackConfirm(() -> {
                txtChat.appendText("Я пишу: " + txtMsgSend.getText() + "\n");
                txtMsgSend.clear();
            });
        }
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
        });
    }


}
