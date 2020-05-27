package clientapp.controllers;

import clientapp.*;
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
import javafx.scene.control.*;
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
    public boolean isBtnReg = false;

    public ChannelFuture f;     // ???
    public static Channel currentChannel;
    public static String nick;
    public static String freeText;


    // колбэк для входящего списка файлов
    public void setCallbackReceive(Callback callbackReceive) {
        currentChannel.pipeline().get(ClientHandler.class).setCallbackReceived(callbackReceive);
    }

    // колбэк для входящего пакета аутентификации
    public void setCallbackAuth(CallbackAuth callbackAuth) {
        currentChannel.pipeline().get(ClientHandler.class).setCallbackAuth(callbackAuth);
    }

    // колбэк для входящего пакета регистрации нового пользователя
    public void setCallbackReg(CallbackReg callbackReg) {
        currentChannel.pipeline().get(ClientHandler.class).setCallbackReg(callbackReg);
    }

    // колбэк для входящего пакета со списком активных пользователей
    public void setCallbackClientList(CallbackClientList callbackClientList) {
        currentChannel.pipeline().get(ClientHandler.class).setCallbackClientList(callbackClientList);
    }

    // колбэк для потверждений о получении сообщений
    public void setCallbackConfirm(CallbackConfirm callbackConfirm) {
        currentChannel.pipeline().get(ClientHandler.class).setCallbackConfirm(callbackConfirm);
    }

    public void setCallbackMsgAll(CallbackMsgAll callbackMsgAll) {
        currentChannel.pipeline().get(ClientHandler.class).setCallbackMsgAll(callbackMsgAll);
    }

    public void setCallbackConfirmDelete(CallbackConfirmDelete callbackConfirmDelete) {
        currentChannel.pipeline().get(ClientHandler.class).setCallbackConfirmDelete(callbackConfirmDelete);
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
        linkController = fxmlLoader.getController();     // берем ссылку на текущий экземпляр контроллера
        linkController.setpStage(primaryStage);         // сеттером передаем primaryStage для использования вне метода start()
    }


//    public Stage getpStage() {
//        return pStage;
//    }

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
                f = b.connect().sync();
                if (!isBtnReg) {
                    authorization(f, txtLoginEnter.getText(), txtPasswordEnter.getText());
                }
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

    public void btnEnter(ActionEvent actionEvent) {
        isBtnReg = false;
        connect();
    }

    public void authorization(ChannelFuture ctx, String login, String password) throws IOException, InterruptedException {
        // блок отправки данных авторизации пользователя
        String str = new String("/auth" + " " + login + " " + password);
        MyCommandSend.sendCommand(str, ctx.channel());
        // ждем ответа сервера
        setCallbackAuth(()->{
            System.out.println("nick = " + nick);
            if (nick != null) {
                pStage.close();
                new ControllerChat().run();
            } else {
                Alert errorAuth = new Alert(Alert.AlertType.ERROR);
                errorAuth.setTitle("Ошибка аутентификации!");
                errorAuth.setHeaderText("Результат:");
                errorAuth.setContentText(freeText);
                errorAuth.showAndWait();
            }
        });
    }

    public void registrationNewUser(ActionEvent actionEvent) throws IOException, InterruptedException {
        isBtnReg = true;
        connect();
        pStage.hide();      // прячем окно авторизации
        new ControllerRegistration().run();  // запускаем контроллер для регистрации нового пользователя
        pStage.show();
    }
}
