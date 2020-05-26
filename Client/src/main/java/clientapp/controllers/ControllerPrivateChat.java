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
import java.util.ResourceBundle;

public class ControllerPrivateChat implements Initializable {

    public TextArea txtPrivateChat;
    public TextArea txtPrivateSend;
    public Button btnPrivateSend;
    public Button btnPrivateExit;
    public Button btnPrivateSendFile;

   // переменные отправки
    private String nickTo;

    // переменные приемки
    public static String message;
    public static String nickFrom;

    public void run(String nickTo) {
        FXMLLoader fxmlLoaderRegistration = new FXMLLoader();
        fxmlLoaderRegistration.setLocation(getClass().getResource("/private.fxml"));
        Stage stage = new Stage();
        Parent root = null;
        try {
            root = fxmlLoaderRegistration.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        stage.setTitle("Личный чат с " + nickTo + ".");
        this.nickTo = nickTo;
        stage.setResizable(false);
        stage.setScene(scene);
        stage.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // принимаем сообщение
        ControllerChat.setCallbackMsgPrivate(()->
                txtPrivateChat.appendText("Вам написал " + nickFrom + ": " + message + "\n"));
    }


    public void privateSend(ActionEvent actionEvent) throws IOException {
        // отправляем сообщение
        MyCommandSend.sendCommand("/pm&" + ControllerChat.nickTo + "&" + txtPrivateSend.getText(), Controller.currentChannel);
        txtPrivateChat.appendText("Я пишу: " + txtPrivateSend.getText() + "\n");
        txtPrivateSend.clear();
    }

    public void privateExit(ActionEvent actionEvent) {
        // закрываем текущее окно
        Stage stage = (Stage) btnPrivateExit.getScene().getWindow();
        stage.close();
    }

    public void privateSendFile(ActionEvent actionEvent) {
    }

}
