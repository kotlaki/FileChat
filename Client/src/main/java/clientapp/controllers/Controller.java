package clientapp.controllers;

import clientapp.ClientHandler;
import common.Callback;
import common.MyCommandSend;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Controller extends Application {

    public static Controller linkController;
    public Stage pStage;

    public TextField txtLoginEnter;
    public PasswordField txtPasswordEnter;
    public Button btnEnter;
    public Hyperlink linkRegistration;

    public static Channel currentChannel;


    public void setCallbackReceive(Callback callbackReceive) {
        currentChannel.pipeline().get(ClientHandler.class).setCallbackReceived(callbackReceive);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/enter.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Вход");
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setScene(scene);
        primaryStage.show();
        // берем ссылку на текущий экземпляр контроллера
        linkController = fxmlLoader.getController();
        // сеттером передаем primaryStage для использования вне метода start()
        linkController.setpStage(primaryStage);
    }


    public Stage getpStage() {
        return pStage;
    }

    public void setpStage(Stage pStage) {
        this.pStage = pStage;
    }

    public void connect() {
        EventLoopGroup work = new NioEventLoopGroup();
        Thread thread = new Thread(() -> {
            try {
                Bootstrap b = new Bootstrap();
                b.group(work)
                        .channel(NioSocketChannel.class)
                        .remoteAddress(new InetSocketAddress("localhost", 8189))
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new ClientHandler());
                                currentChannel = ch;
                                System.out.println(currentChannel);
                            }
                        });
                ChannelFuture f = b.connect().sync();
                authorization(f, txtLoginEnter.getText(), txtPasswordEnter.getText());
//                setAuthorized(true);
                f.channel().closeFuture().sync();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    work.shutdownGracefully().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
//        thread.setDaemon(true);
        thread.start();
    }

    public void btnEnter(ActionEvent actionEvent) throws IOException {
        connect();
        pStage.close();
        new ControllerChat().run();
    }

    public static void authorization(ChannelFuture ctx, String login, String password) throws IOException {
        // блок отправки данных авторизации пользователя
        String str = new String("/auth" + " " + login + " " + password);
        MyCommandSend.sendCommand(str, ctx.channel());
    }

    public void registrationNewUser(ActionEvent actionEvent) throws IOException, InterruptedException {
        pStage.hide();      // прячем окно авторизации
        new ControllerRegistration().run();  // запускаем контроллер для регистрации нового пользователя
        pStage.show();
    }
}
