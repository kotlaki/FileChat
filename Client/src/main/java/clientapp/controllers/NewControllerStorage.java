package clientapp.controllers;

import common.MyCommandSend;
import common.MyFileList;
import common.MyFileSend;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import java.nio.file.Paths;
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
    public Button btnRefreshFile;
    public ProgressBar progressBar;

    public List<String> fileList = new ArrayList<>();
    public static String msgFromServer;
    public String getNameFileToServer;
    public String getNameFileFromServer;

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
        MyFileSend.sendFile(Paths.get("client_storage/"+ getNameFileToServer), NewController.currentChannel, future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }

            if (future.isSuccess()) {
                System.out.println("Файл успешно передан...");
            }
        });
    }

    public void receiveFileFromServer(ActionEvent actionEvent) {
        NewController.currentChannel.writeAndFlush(Unpooled.copiedBuffer("/fr " + "server_storage/" + getNameFileFromServer, CharsetUtil.UTF_8));
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
        // устанавливаем фокус на первый элемент в списке
        listFileClient.getFocusModel().focus(1);
        getNameFileToServer = listFileClient.focusModelProperty().getValue().getFocusedItem();
        // отслеживаем и вытаскиваем название имени файла при его выделении в списке
        listFileClient.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("to Storage from client = " + newValue);
                getNameFileToServer = newValue;
            }
        });
    }

    public void refreshListServer() throws IOException {
        //            IntStream.range(0, strSplit.length)
//                    .filter(i -> i != 0)
//                    .map(i -> Integer.parseInt(strSplit[i]))
//                    .toArray();
//            System.out.println(Arrays.toString(strSplit));
        Platform.runLater(() -> {
            // сплитим полученный массив
            String[] strSplit = msgFromServer.split(" ");
           // т.к. у нас первый элемент будет содержать служебную команду /req_list переносим все элементы в новый массив
            String[] result = new String[strSplit.length - 1];
            for (int i = 1; i < strSplit.length; i++) {
                result[i - 1] = strSplit[i];
            }
            ObservableList<String> files = FXCollections.observableArrayList(Arrays.asList(result));
            listFileServer.setItems(files);

            // отслеживаем и вытаскиваем название имени файла при его выделении в списке
            listFileServer.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    System.out.println("to Storage from server = " + newValue);
                    getNameFileFromServer = newValue;
                }
            });
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
