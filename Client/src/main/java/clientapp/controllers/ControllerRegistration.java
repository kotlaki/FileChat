package clientapp.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

    public void btnAddNewUser(ActionEvent actionEvent) {
    }

    public void btnCancelRegistration(ActionEvent actionEvent) throws Exception {
        Stage stage = (Stage) btnCancelRegistration.getScene().getWindow();
        stage.close();
    }
}
