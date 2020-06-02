package clientapp.controllers;

import common.MyCommandSend;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ControllerRegistration implements Initializable {

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // обрабатываем нажатие tab для навигации по форме
        new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                KeyCode code = event.getCode();

                if (code == KeyCode.TAB && !event.isShiftDown() && !event.isControlDown()) {
                    event.consume();
                    Node node = (Node) event.getSource();
                    KeyEvent newEvent
                            = new KeyEvent(event.getSource(),
                            event.getTarget(), event.getEventType(),
                            event.getCharacter(), event.getText(),
                            event.getCode(), event.isShiftDown(),
                            true, event.isAltDown(),
                            event.isMetaDown());

                    node.fireEvent(newEvent);
                }
            }
        };
    }

    public void btnAddNewUser(ActionEvent actionEvent) throws Exception {
        if (txtLoginRegistration.getText().equals("") || txtNickRegistration.getText().equals("")
                || txtPasswordRegistration.getText().equals("") || txtPasswordConfirmRegistration.getText().equals("")) {
            Alert errorAuth = new Alert(Alert.AlertType.WARNING);
            errorAuth.setTitle("Регистрация нового пользователя!");
            errorAuth.setHeaderText("Результат:");
            errorAuth.setContentText("Все поля поля, кроме поля 'О себе' обязательны для заполнения!!!");
            errorAuth.showAndWait();
        } else {
            MyCommandSend.sendCommand("/regNewUser " + txtLoginRegistration.getText() + " " + txtPasswordConfirmRegistration.getText()
                    + " " + txtNickRegistration.getText() + " " + txtAbout.getText(), Controller.currentChannel);
            Controller.linkController.setCallbackReg(() -> {
                Alert errorAuth = new Alert(Alert.AlertType.INFORMATION);
                errorAuth.setTitle("Регистрация нового пользователя!");
                errorAuth.setHeaderText("Результат:");
                errorAuth.setContentText(Controller.freeText);
                errorAuth.showAndWait();
                btnCancelRegistration();
            });
        }
    }

    public void btnCancelRegistration() throws Exception {
        Stage stage = (Stage) btnCancelRegistration.getScene().getWindow();
        stage.close();
    }

}
