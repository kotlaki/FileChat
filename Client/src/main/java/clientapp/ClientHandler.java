package clientapp;

import clientapp.callback.*;
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

    public CallbackMsgAll callbackMsgAll;

    public void setCallbackMsgAll(CallbackMsgAll callbackMsgAll) {
        this.callbackMsgAll = callbackMsgAll;
    }

    public CallbackConfirmDelete callbackConfirmDelete;

    public void setCallbackConfirmDelete(CallbackConfirmDelete callbackConfirmDelete) {
        this.callbackConfirmDelete = callbackConfirmDelete;
    }

    public CallbackConfirmReceiveFile callbackConfirmReceiveFile;

    public void setCallbackConfirmReceiveFile(CallbackConfirmReceiveFile callbackConfirmReceiveFile) {
        this.callbackConfirmReceiveFile = callbackConfirmReceiveFile;
    }

    public CallbackConfirmReceivePrivate callbackConfirmReceivePrivate;

    public void setCallbackConfirmReceivePrivate(CallbackConfirmReceivePrivate callbackConfirmReceivePrivate) {
        this.callbackConfirmReceivePrivate = callbackConfirmReceivePrivate;
    }

    public CallbackPrivateMsgReceive callbackPrivateMsgReceive;

    public void setCallbackPrivateMsgReceive(CallbackPrivateMsgReceive callbackPrivateMsgReceive) {
        this.callbackPrivateMsgReceive = callbackPrivateMsgReceive;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        String str = buf.toString(CharsetUtil.UTF_8);
        // обрабатываем получение файла от сервера
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
                // получаем сооющение предназначенное для всех
                if (message.startsWith("/all")) {
                    String[] strSplit = message.split("&");
                    System.out.println("in handler = " + strSplit[1]);
                    ControllerChat.message = strSplit[1] + " пишет: " + strSplit[2];
                    Platform.runLater(()->{
                        callbackMsgAll.callbackMsgAll();
                    });
                }
                // обрабатываем получение приватных сообщений
                if (message.startsWith("/pm")) {
                    String[] strSplit = message.split("&");
                    System.out.println("nickSender = " + strSplit[1]);
                    ControllerChat.nickSender = strSplit[1];
                    System.out.println("message = " + strSplit[2]);
                    ControllerChat.message = strSplit[2];
                            Platform.runLater(()-> {
                        try {
                            callbackPrivateMsgReceive.callbackPrivateMsgReceive();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
                // обрабатываем подверждение о получении сервером файла
                if (message.equals("/receiveFileOK")) {
                    Platform.runLater(()-> {
                        try {
                            callbackConfirmReceiveFile.callbackConfirmReceiveFile();
                        } catch (InterruptedException | IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
                // обрабатываем подтверждение получения сервером сообщения
                if (message.equals("/confReceiveAllMsg")) {
                    Platform.runLater(()-> {
                        callbackConfirm.callbackConfirm();
                    });
                }
                // обрабатываем подтверждение получение приватного сообщения
                if (message.equals("/confReceivePrivate")) {
                    Platform.runLater(()-> {
                        callbackConfirmReceivePrivate.callbackConfirmReceivePrivate();
                    });
                }
                // обрабатываем подврждение об удалении файла на сервере
                if (message.equals("/deleteOK")) {
                    Platform.runLater(()-> {
                        try {
                            callbackConfirmDelete.callbackConfirmDelete();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
                // обрабатываем получение списка файлов
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
                // обрабатываем получение списка активных пользователей
                if (message.startsWith("/respClientList")) {
                    if (MyCommandReceive.currentState == MyCommandReceive.State.IDLE) {
                        Platform.runLater(() -> {
                                callbackClientList.callbackClientList();
                            ControllerChat.clientListFromServer = message;
                        });
                    }
                }
                // обрабатываем пришедший ник в ответ на авторизацию
                if (message.startsWith("/authOK")) {
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
                // обрабатываем пришедшие ошибки от сервера возникшие при авторизации
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
                // обрабатываем ответ сервера при регистрации пользователя
                if (message.startsWith("/respReg")) {
                    String[] strSplit = message.split("&");
                    Controller.freeText = strSplit[1];
                    Platform.runLater(()->{
                        try {
                            callbackReg.callbackReg();
                        } catch (Exception e) {
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
