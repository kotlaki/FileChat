package clientapp;

import clientapp.controllers.Controller;
import clientapp.controllers.ControllerStorage;
import common.Callback;
import common.CallbackAuth;
import common.MyCommandReceive;
import common.MyFileReceive;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import javafx.application.Platform;

import java.io.IOException;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    public Callback callbackReceived;

    public void setCallbackReceived(Callback callbackReceived) {
        this.callbackReceived = callbackReceived;
    }

    public CallbackAuth callbackAuth;

    public void setCallbackAuth(CallbackAuth callbackAuth) {
        this.callbackAuth = callbackAuth;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // принимаем служебные данные от сервера
        ByteBuf buf = (ByteBuf) msg;
        String str = buf.toString(CharsetUtil.UTF_8);
//        Controller.currentController.txtChat.appendText(str + "\n");
        if (str.startsWith("/file") || MyFileReceive.currentState == MyFileReceive.State.FILE) {
            MyFileReceive.receiveFile(buf, "client_storage/");
            if (MyFileReceive.currentState == MyFileReceive.State.IDLE) {
                callbackReceived.callback();
            }
        } else
            // приемка сообщений
            if (str.startsWith("/message") || MyCommandReceive.currentState == MyCommandReceive.State.MESSAGE) {
                String message = MyCommandReceive.receiveCommand(buf);
                System.out.println("From server = " + message);
                if (message.startsWith("/req_list")) {
                    // TO DO от сюда надо передать строку в NewControllerStorage
                    if (MyCommandReceive.currentState == MyCommandReceive.State.IDLE) {
                        Platform.runLater(() -> {
                            try {
                                callbackReceived.callback();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ControllerStorage.msgFromServer = message;
                        });
                    }
                }
                if (message.startsWith("/authOK")) {
                    System.out.println("AUTH OK!!!");
                    String[] strSplit = message.split(" ");
                    Controller.nick = strSplit[1];
                    Platform.runLater(()-> {
                        try {
                            callbackAuth.callbackAuth();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
                if (message.startsWith("/errorAuth")) {
                    String[] strSplit = message.split("&");
                    Controller.freeText = strSplit[1];
                    Platform.runLater(()-> {
                        try {
                            callbackAuth.callbackAuth();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
