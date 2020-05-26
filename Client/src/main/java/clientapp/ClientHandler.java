package clientapp;

import clientapp.controllers.Controller;
import clientapp.controllers.ControllerChat;
import clientapp.controllers.ControllerStorage;
import common.*;
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

    public CallbackReg callbackReg;

    public void setCallbackReg(CallbackReg callbackReg) {
        this.callbackReg = callbackReg;
    }

    public CallbackClientList callbackClientList;

    public void setCallbackClientList(CallbackClientList callbackClientList) {
        this.callbackClientList = callbackClientList;
    }

    public CallbackConfirm callbackConfirm;

    public void setCallbackConfirm(CallbackConfirm callbackConfirm) {
        this.callbackConfirm = callbackConfirm;
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
                if (message.startsWith("/all")) {
                    String[] strSplit = message.split("&");
                    System.out.println("in handler = " + strSplit[1]);
                    ControllerChat.message = strSplit[1] + " пишет: " + strSplit[2];
                }
                if (message.equals("/confReceive")) {
                    Platform.runLater(()-> {
                        callbackConfirm.callbackConfirm();
                    });
                }
                if (message.startsWith("/req_list")) {
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

                if (message.startsWith("/respClientList")) {
                    if (MyCommandReceive.currentState == MyCommandReceive.State.IDLE) {
                        Platform.runLater(() -> {
                                callbackClientList.callbackClientList();
                            ControllerChat.clientListFromServer = message;
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
                if (message.startsWith("/respReg")) {
                    String[] strSplit = message.split("&");
                    Controller.freeText = strSplit[1];
                    Platform.runLater(()->{
                        callbackReg.callbackReg();
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
