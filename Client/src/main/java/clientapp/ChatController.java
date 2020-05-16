package clientapp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;

public class ChatController {
    // вход
    AnchorPane anchorePaneEnter;
    public TextField txtFieldLogin;
    public PasswordField txtFieldPassword;
    public Button btnEnter;
    public Button btnRegistration;
    // регистрация
    public TextField txtNicknameReg;
    public TextField txtLoginReg;
    public PasswordField txtPassReg;
    public PasswordField txtPassConfirmReg;
    public TextArea txtDescriptionReg;
    public Button btnRegistrationReg;
    public Label txtLabelReg;
    // основной чат
    public TextArea txtChatWindow;
    public TextArea txtChatSend;
    public Button btnSend;
    public Button btnFileStorage;
    // file storage
    public ListView listServer;
    public ListView listClient;
    public Button btnBack;
    public Button btnSendToServer;
    public Button btnReceiveFromServer;

    private boolean goRegister = false;


    public void initialize() {


    }

    public void enter() {

        Task<Channel> task = new Task<Channel>() {
            @Override
            protected Channel call() throws Exception {
                EventLoopGroup work = new NioEventLoopGroup();
                try {
                    Bootstrap b = new Bootstrap();
                    b.group(work)
                            .channel(NioSocketChannel.class)
                            .remoteAddress(new InetSocketAddress("localhost", 8189))
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    ch.pipeline().addLast(new MainClientHandler());
                                }
                            });
                    ChannelFuture f = b.connect().sync();

                    if (!goRegister) {
                        // шлем данные на авторизацию
                        Chat.authorization(f, txtFieldLogin.getText(), txtFieldPassword.getText());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    workChat();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        // шлем данные на регистрацию
                        Chat.registration(f, txtLoginReg.getText(), txtPassReg.getText(), txtNicknameReg.getText(), txtDescriptionReg.getText());
                        goRegister = false;
                    }
                    f.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        work.shutdownGracefully().sync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }
        };
        new Thread(task).start();
    }

    public void workChat() throws IOException {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/chat.fxml"));
                Parent root = fxmlLoader.load();
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setOpacity(1);
                stage.setTitle("Чат");
                stage.setScene(new Scene(root));
                stage.showAndWait();
    }

    public void registration(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/registration.fxml"));
        Parent root = fxmlLoader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setOpacity(1);
        stage.setTitle("Регистрация нового пользователя");
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }

    public void sendRegister(ActionEvent actionEvent) {
        goRegister = true;
        enter();
    }

    public void fileStorage(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fileExplorer.fxml"));
        Parent root = fxmlLoader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setOpacity(1);
        stage.setTitle("Хранилище файлов");
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }

    public void closeWindow(ActionEvent actionEvent) {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }
}
