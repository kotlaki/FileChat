package clientapp.controllers;

import common.MyCommandSend;
import common.MyFileList;
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
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

public class NewControllerStorage implements Initializable {
    public ListView<String> listFileClient;
    public ListView<String> listFileServer;
    public Button btnCancelStorage;
    public Button btnLeftToRight;
    public Button btnRightToLeft;
    public Button btnRefreshFile;
    public ProgressBar progressBar;

    public List<String> fileList = new ArrayList<>();
    public static String msgFromServer;
//    public static NewControllerStorage testController;

    public void run() throws IOException {
        FXMLLoader fxmlLoaderRegistration = new FXMLLoader();
        fxmlLoaderRegistration.setLocation(getClass().getResource("/new/storage.fxml"));
//        testController = fxmlLoaderRegistration.getController();
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

    public void refreshListServer() throws IOException {
        Platform.runLater(() -> {
            // сплитим полученный массив
            String[] strSplit = msgFromServer.split(" ");
//            IntStream.range(0, strSplit.length)
//                    .filter(i -> i != 0)
//                    .map(i -> Integer.parseInt(strSplit[i]))
//                    .toArray();
//            System.out.println(Arrays.toString(strSplit));
           // т.к. у нас первый элемент будет содержать служебную команду /req_list переносим все элементы в новый массив
            String[] result = new String[strSplit.length - 1];
            for (int i = 1; i < strSplit.length; i++) {
                result[i - 1] = strSplit[i];
            }
            ObservableList<String> files = FXCollections.observableArrayList(Arrays.asList(result));
            listFileServer.setItems(files);
        });
    }

    public void requestListServer() throws IOException {
        MyCommandSend.sendCommand("/req_list", NewController.currentChannel);
    }

    public void refreshFile(ActionEvent actionEvent) throws IOException {
        refreshListClient();
        requestListServer();
        refreshListServer();
    }
}
