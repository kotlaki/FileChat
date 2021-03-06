package clientapp.controllers;

import common.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ControllerStorage implements Initializable {
    public ListView<String> listFileClient;
    public ListView<String> listFileServer;
    public Button btnCancelStorage;
    public Button btnLeftToRight;
    public Button btnRightToLeft;
    public Button btnRefreshFile;
    public Button btnDeleteFile;
    public Button btnRemoveFile;

    public static String msgFromServer;
    public String getNameFileToServer;
    public String getNameFileFromServer;

    public void run() throws IOException {
        FXMLLoader fxmlLoaderRegistration = new FXMLLoader();
        fxmlLoaderRegistration.setLocation(getClass().getResource("/storage.fxml"));
        Stage stage = new Stage();
        Parent root = fxmlLoaderRegistration.load();
        Scene scene = new Scene(root);
        stage.setTitle("Моё файловое хранилище");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnRightToLeft.setTooltip(new Tooltip("Копировать на компьютер"));
        btnLeftToRight.setTooltip(new Tooltip("Копировать в хранилище"));
        btnDeleteFile.setTooltip(new Tooltip("Удалить файл"));
        btnRefreshFile.setTooltip(new Tooltip("Обновить список файлов"));
        btnRemoveFile.setTooltip(new Tooltip("Перенести файл"));

        try {
            refreshFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // отслеживаем и вытаскиваем название имени файла при его выделении в списке клиента
        listFileClient.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                getNameFileToServer = newValue;
                getNameFileFromServer = null;
            }
        });

        // отслеживаем и вытаскиваем название имени файла при его выделении в списке сервера
        listFileServer.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            getNameFileFromServer = newValue;
            getNameFileToServer = null;
        });
    }

    public void sendFileToServer() throws IOException, InterruptedException {

        MyFileSend.sendFile(Paths.get("client_storage/" + getNameFileToServer), Controller.currentChannel);
        Controller.linkController.setCallbackConfirmReceiveFile(this::refreshFile);
    }

    public void receiveFileFromServer(ActionEvent actionEvent) throws IOException, InterruptedException {
        MyCommandSend.sendCommand("/fr " + "server_storage/" + Controller.nick + "/" + getNameFileFromServer, Controller.currentChannel);
        // как только получаем полностью файл вызывается обновление списков файлов с помощью callback
        Controller.linkController.setCallbackReceive(this::refreshFile);
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
            String[] strSplit = msgFromServer.split("&&");
            // т.к. у нас первый элемент будет содержать служебную команду /req_list переносим все элементы в новый массив
            String[] result = new String[strSplit.length - 1];
            System.arraycopy(strSplit, 1, result, 0, strSplit.length - 1);
            ObservableList<String> files = FXCollections.observableArrayList(Arrays.asList(result));
            listFileServer.setItems(files);
        });
    }

    public void requestListServer() throws IOException {
        MyCommandSend.sendCommand("/req_list", Controller.currentChannel);
    }

    public void refreshFile() throws IOException {
        requestListServer();    // посылаем запрос на список файлов и ждем получения
        Controller.linkController.setCallbackReceive(() -> {      // при получении списка файлов с сервера обновляем списки в приложении
            refreshListServer();
            refreshListClient();
        });
    }

    public void removeFile(ActionEvent actionEvent) throws IOException, InterruptedException {
        // переносим файл с клиента на сервер
        if (getNameFileToServer != null) {
            MyFileSend.sendFile(Paths.get("client_storage/" + getNameFileToServer), Controller.currentChannel);
            Controller.linkController.setCallbackConfirmReceiveFile(()-> {
                deleteFile();
                Controller.linkController.setCallbackConfirmDelete(this::refreshFile);
            });
        }
        // переносим файл с сервера на клиента
        if (getNameFileFromServer != null) {
           MyCommandSend.sendCommand("/fr " + "server_storage/" + Controller.nick + "/" + getNameFileFromServer, Controller.currentChannel);
            Controller.linkController.setCallbackReceive(this::deleteFile);
        }
    }

    public void deleteFile() throws IOException {
        // выясняем где удалить файл, на сервере или локально на клиенте
        if (getNameFileFromServer != null) {
            MyCommandSend.sendCommand("/delete " + getNameFileFromServer, Controller.currentChannel);
            Controller.linkController.setCallbackConfirmDelete(this::refreshFile);
        }
        if (getNameFileToServer != null) {
            Files.delete(Paths.get("client_storage/" + getNameFileToServer));
            refreshFile();
        }
    }
}
