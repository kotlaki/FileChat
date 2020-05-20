package clientapp;

  /*
    /fr - file receive - получение файла
    /fs - file send - отправка файла
    /reg login password nickname description - регистрация нового пользователя
*/

import common.MyCommandSend;
import common.MyFileSend;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Controller extends Application implements Initializable {

    public TextArea txtChat;
    public TextArea txtSend;
    public Button btnEnter;
    public TextField txtLogin;
    public PasswordField txtPassword;
    public Button btnSend;
    public Button btnRegistration;

    public static Channel currentChannel;
    public static Controller controller; // хранит ссылку на текущий контроллер

    private boolean isAuthorized;
    private static String nickName;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        URL urlFxml = getClass().getResource("/total.fxml");
        Controller controller = loader.getController();
        loader.setLocation(urlFxml);
        Parent root = loader.load();
        primaryStage.resizableProperty().setValue(false);
        primaryStage.resizableProperty().setValue(false);
        primaryStage.setTitle("Чат");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
//        currentController = loader.getController(); // узнаем ссылку на текущий контроллер
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorized(false);
    }

    public void setAuthorized(boolean isAuthorized) {

        if (!isAuthorized) {
            txtChat.setVisible(false);
            txtSend.setVisible(false);
            txtLogin.setVisible(true);
            txtPassword.setVisible(true);
            btnEnter.setVisible(true);
            btnSend.setVisible(false);
            btnRegistration.setVisible(true);
        } else {
            txtChat.setVisible(true);
            txtSend.setVisible(true);
            txtLogin.setVisible(false);
            txtPassword.setVisible(false);
            btnEnter.setVisible(false);
            btnSend.setVisible(true);
            btnRegistration.setVisible(false);

        }
    }

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
                                ch.pipeline().addLast(new ClientHandler());
                                currentChannel = ch;
                                System.out.println(currentChannel);
                            }
                        });
                ChannelFuture f = b.connect().sync();
                authorization(f, txtLogin.getText(), txtPassword.getText());
                setAuthorized(true);
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

    public static void authorization(ChannelFuture ctx, String login, String password) throws IOException {
        // блок отправки данных авторизации пользователя
        String str = new String("/auth" + " " + login + " " + password);
        MyCommandSend.sendCommand(str, ctx.channel());
    }

    public void btnRegistration() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/registration.fxml"));
        Parent root1 = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root1));
        stage.show();
        connect();
    }

    public static void registration(Channel channel, String login, String password, String nickName, String description) throws IOException {
        // блок отправки данных регистрации пользователя
        String str = new String("/auth /reg" + " " + login + " " + password + " " + nickName + " " + description);
        MyCommandSend.sendCommand(str, channel);
//        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(str, CharsetUtil.UTF_8));
    }

//    public void chat(ChannelHandlerContext ctx, ByteBuf buf, String str) throws IOException {
//        this.ctx = ctx;
//        this.in = buf;
//        this.inMsg = str;
////        System.out.println("CHAT = " + str);
//
//    }

    public void sendMsg(String msg) throws IOException {
        if (!msg.equals("")) {

            // блок отправки файла на сервер
            if (msg.startsWith("/fs")) {
                String[] token = msg.split(" ");
                String pathToFile = "client_storage/" + token[1];
                MyFileSend.sendFile(Paths.get(pathToFile), currentChannel, future -> {
                    if (!future.isSuccess()) {
                        future.cause().printStackTrace();
                        txtChat.appendText("Произошла ошибка при передаче файла!!!\n");
                    }

                    if (future.isSuccess()) {
                        System.out.println("Файл успешно передан...");
                        txtChat.appendText("Файл успешно передан\n");
                    }
                });
            } else
                // блок отправки сообщений
                if (msg.startsWith("/fr")) {
                    currentChannel.writeAndFlush(Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
                } else {
                    // отправляем обычные сообщения
                    MyCommandSend.sendCommand(msg, currentChannel);
                }

        }
    }

    public void btnEnter(ActionEvent actionEvent) {
        connect();
    }

    public void btnSend() throws IOException {
        sendMsg(txtSend.getText());
        txtChat.appendText(txtSend.getText() + "\n");
        txtSend.clear();
    }


}