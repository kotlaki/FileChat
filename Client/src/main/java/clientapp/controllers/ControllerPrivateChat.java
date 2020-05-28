package clientapp.controllers;

import common.MyCommandSend;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ControllerPrivateChat implements Initializable {
    public TextArea txtPrivateChat;
    public TextArea txtPrivateSend;
    public Button btnPrivateSend;
    public Button btnPrivateExit;
    public Button btnPrivateSendFile;

    public static List<ControllerPrivateChat> listPrivateChat = new ArrayList<>();

    public void run(String nickReceiver) throws IOException {
        FXMLLoader fxmlLoaderPrivate = new FXMLLoader();
        fxmlLoaderPrivate.setLocation(getClass().getResource("/private.fxml"));
        Stage stage = new Stage();
        Parent root = fxmlLoaderPrivate.load();
        Scene scene = new Scene(root);
        stage.setTitle("Приватный чат с " + nickReceiver);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listPrivateChat.add(this);  // добавляем текущий чат в список активных чатов
    }

    public void privateExit(ActionEvent actionEvent) {
    }

    public void privateSend(ActionEvent actionEvent) throws IOException {
        MyCommandSend.sendCommand("/pm&" + ControllerChat.nickReceiver + "&" + txtPrivateSend.getText(), Controller.currentChannel);
        Controller.linkController.setCallbackConfirmReceivePrivate(()->{
            txtPrivateChat.appendText("Я пишу: " + txtPrivateSend.getText() + "\n");
            txtPrivateSend.clear();
        });
    }

    public void privateSendFile(ActionEvent actionEvent) {
    }

}
