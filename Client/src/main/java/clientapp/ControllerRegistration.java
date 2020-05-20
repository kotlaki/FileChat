package clientapp;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ControllerRegistration extends Stage {

    public PasswordField pswConfirmReg;
    public PasswordField pswEnterReg;
    public TextField txtLoginReg;
    public TextField txtNickReg;
    public Button btnEndReg;
    public Button btnCancelReg;


    public void regNewUser(ActionEvent actionEvent) throws IOException {
        Controller.registration(Controller.currentChannel, txtLoginReg.getText(), pswConfirmReg.getText(), txtNickReg.getText(), "test");
    }
}
