package clientapp.controllers;

import common.MyCommandSend;
import common.MyFileList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class NewControllerStorage implements Initializable {
    public ListView<String> listFileClient;
    public ListView<String> listFileServer;
    public Button btnCancelStorage;
    public Button btnLeftToRight;
    public Button btnRightToLeft;
    public ProgressBar progressBar;

    public List<String> fileList = new ArrayList<>();
    public String msgFromServer;

    public void run() throws IOException {
        FXMLLoader fxmlLoaderRegistration = new FXMLLoader();
        fxmlLoaderRegistration.setLocation(getClass().getResource("/new/storage.fxml"));
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
        try {
            refreshListClient();
            requestListServer();
            refreshListServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFileToServer(ActionEvent actionEvent) throws IOException {
//        ObservableList<String> files = FXCollections.observableArrayList(MyFileList.listFile());
//        listFileClient.setItems(files);
//        listFileClient.refresh();

//        listFileClient.getSelectionModel().selectIndices(1, 2);
//        listFileClient.getFocusModel().focus(1);
    }

    public void receiveFileFromServer(ActionEvent actionEvent) {
    }

    public void cancelStorage(ActionEvent actionEvent) {
        // закрываем текущее окно
        Stage stage = (Stage) btnCancelStorage.getScene().getWindow();
        stage.close();
    }

    public void refreshListClient() throws IOException {
        // вытаскиваем в ListView данные из List и выводим
        ObservableList<String> files = FXCollections.observableArrayList(MyFileList.listFile("client_storage"));
        listFileClient.setItems(files);
    }

    public void refreshListServer() {
        String[] strSplit = msgFromServer.split(" ");
        ObservableList<String> files = FXCollections.observableArrayList(Arrays.asList(strSplit));
        listFileServer.setItems(files);
    }

    public void requestListServer() throws IOException {
        MyCommandSend.sendCommand("/req_list", NewController.currentChannel);
    }

}
