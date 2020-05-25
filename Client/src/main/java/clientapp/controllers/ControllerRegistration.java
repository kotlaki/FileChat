package clientapp.controllers;

import common.MyCommandSend;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class ControllerRegistration {

    public TextField txtNickRegistration;
    public TextField txtLoginRegistration;
    public PasswordField txtPasswordRegistration;
    public PasswordField txtPasswordConfirmRegistration;
    public TextArea txtAbout;
    public Button btnAddNewUser;
    public Button btnCancelRegistration;


    public void run() throws IOException {
        FXMLLoader fxmlLoaderRegistration = new FXMLLoader();
        fxmlLoaderRegistration.setLocation(getClass().getResource("/registration.fxml"));
        Stage stage = new Stage();
        Parent root = fxmlLoaderRegistration.load();
        Scene scene = new Scene(root);
        stage.setTitle("Регистрация");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.showAndWait();
    }

    public void btnAddNewUser(ActionEvent actionEvent) throws IOException {
        MyCommandSend.sendCommand("/regNewUser " + txtLoginRegistration.getText() + " " + txtPasswordConfirmRegistration.getText()
                + " " + txtNickRegistration.getText() + " " + txtAbout.getText(), Controller.currentChannel);
        Controller.linkController.setCallbackReg(()->{
            Alert errorAuth = new Alert(Alert.AlertType.INFORMATION);
            errorAuth.setTitle("Регистрация нового пользователя!");
            errorAuth.setHeaderText("Результат:");
            errorAuth.setContentText(Controller.freeText);
            errorAuth.showAndWait();
        });
    }

    public void btnCancelRegistration(ActionEvent actionEvent) throws Exception {
        Stage stage = (Stage) btnCancelRegistration.getScene().getWindow();
        stage.close();
    }
}
