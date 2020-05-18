package clientapp;

import common.MyFileReceive;
import common.MyFileSend;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Scanner;

public class Controller extends Application {

    public TextArea txtChat;
    public TextArea txtSend;
    public Button btnEnter;
    public TextField txtLogin;
    public PasswordField txtPassword;
    public Button btnSend;
    private String prev = "";
    private String test;
    private String str;
    private boolean isAuthorized;
    private Channel currentChannel;
    private ChannelHandlerContext ctx;
    private ByteBuf in;
    private String inMsg;

    public Controller() {
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        URL urlFxml = getClass().getResource("/total.fxml");
        Controller controller = loader.getController();
        loader.setLocation(urlFxml);
        Parent root = loader.load();
//        primaryStage.resizableProperty().setValue(false);
        primaryStage.resizableProperty().setValue(false);
        primaryStage.setTitle("Авторизация");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

//    @Override
//    public void init() throws Exception {
//        setAuthorized(false);
//    }
//
//    public void setAuthorized(boolean isAuthorized) {
//        this.isAuthorized = isAuthorized;
//        if (!isAuthorized) {
//            txtChat.setVisible(false);
//            txtSend.setVisible(false);
//            txtLogin.setVisible(true);
//            txtPassword.setVisible(true);
//            btnEnter.setVisible(true);
//            btnSend.setVisible(false);
//        } else {
//            txtChat.setVisible(true);
//            txtSend.setVisible(true);
//            txtLogin.setVisible(true);
//            txtPassword.setVisible(true);
//            btnEnter.setVisible(true);
//            btnSend.setVisible(true);
//        }
//    }

    public void connect() {
        EventLoopGroup work = new NioEventLoopGroup();
//        setAuthorized(false);
        Thread thread = new Thread(() -> {
            try {
                Bootstrap b = new Bootstrap();
                b.group(work)
                        .channel(NioSocketChannel.class)
                        .remoteAddress(new InetSocketAddress("localhost", 8189))
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new MainClientHandler());
                                currentChannel = ch;
                                System.out.println(currentChannel);
                            }
                        });
                ChannelFuture f = b.connect().sync();
                authorization(f, txtLogin.getText(), txtPassword.getText());
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
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static void authorization(ChannelFuture ctx, String login, String password) {
        // блок отправки данных авторизации пользователя
        System.out.println();
        String str = new String("/auth" + " " + login + " " + password);
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(str, CharsetUtil.UTF_8));
    }

    public static void registration(ChannelFuture ctx, String login, String password, String nickName, String description) {
        // блок отправки данных регистрации пользователя
        String str = new String("/auth /reg" + " " + login + " " + password + " " + nickName + " " + description);
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(str, CharsetUtil.UTF_8));
    }

    public void chat(ChannelHandlerContext ctx, ByteBuf buf, String str) throws IOException {
        this.ctx = ctx;
        this.in = buf;
        this.inMsg = str;
        System.out.println("CHAT = " + str);

    }

    public void sendMsg(String msg) throws IOException {

//        if (!prev.equals("/fr")) {
//            if (msg.startsWith("/close")) {
//                System.out.println("Закрываем канал!!!");
//                currentChannel.close();
//            }
//            currentChannel.write(Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
//        }
//        // блок принития файла с сервера
//        if (prev.equals("/fr")) {
//            MyFileReceive.receiveFile(in, "client_storage/");
//            prev = "";
//        }
//        if (msg.startsWith("/fr")) {
//            prev = "/fr";
//        }
//        // блок отправки файла на сервер
//        if (msg.startsWith("/fs")) {
//            String[] token = msg.split(" ");
//            String pathToFile = "client_storage/" + token[1];
//            MyFileSend.sendFile(Paths.get(pathToFile), currentChannel, future -> {
//                if (!future.isSuccess()) {
//                    future.cause().printStackTrace();
//                }
//
//                if (future.isSuccess()) {
//                    System.out.println("Файл успешно передан");
//                }
//            });
//        }
        if (!prev.equals("/fr")) {
            currentChannel.writeAndFlush(Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
//            test = buf.toString(CharsetUtil.UTF_8);
        }
        // блок принития файла с сервера
        if(prev.equals("/fr")) {
            MyFileReceive.receiveFile(in, "client_storage/");
            prev = "";
        }
        if (msg.startsWith("/fr")) {
            prev = "/fr";
        }


        // блок отправки файла на сервер
        if (msg.startsWith("/fs")) {
            String[] token = msg.split(" ");
            String pathToFile = "client_storage/" + token[1];
            MyFileSend.sendFile(Paths.get(pathToFile), currentChannel, future -> {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
                }

                if (future.isSuccess()) {
                    System.out.println("Файл успешно передан");
                }
            });
        }
//        currentChannel.writeAndFlush(Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
    }

    public void btnEnter(ActionEvent actionEvent) {
        connect();
    }

    public void btnSend() throws IOException {
        sendMsg(txtSend.getText());
//        txtSend.clear();
    }


    /*
    /fr - file receive - получение файла
    /fs - file send - отправка файла
    /reg login password nickname description - регистрация нового пользователя
*/


}
