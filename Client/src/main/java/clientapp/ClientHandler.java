package clientapp;

import clientapp.controllers.Callback;
import clientapp.controllers.Controller;
import clientapp.controllers.ControllerChat;
import clientapp.controllers.ControllerStorage;
import common.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import javafx.application.Platform;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    public Callback callbackReceived;

    public void setCallbackReceived(Callback callbackReceived) {
        this.callbackReceived = callbackReceived;
    }

    public Callback callbackAuth;

    public void setCallbackAuth(Callback callbackAuth) {
        this.callbackAuth = callbackAuth;
    }

    public Callback callbackReg;

    public void setCallbackReg(Callback callbackReg) {
        this.callbackReg = callbackReg;
    }

    public Callback callbackClientList;

    public void setCallbackClientList(Callback callbackClientList) {
        this.callbackClientList = callbackClientList;
    }

    public Callback callbackConfirm;

    public void setCallbackConfirm(Callback callbackConfirm) {
        this.callbackConfirm = callbackConfirm;
    }

    public Callback callbackMsgAll;

    public void setCallbackMsgAll(Callback callbackMsgAll) {
        this.callbackMsgAll = callbackMsgAll;
    }

    public Callback callbackConfirmDelete;

    public void setCallbackConfirmDelete(Callback callbackConfirmDelete) {
        this.callbackConfirmDelete = callbackConfirmDelete;
    }

    public Callback callbackConfirmReceiveFile;

    public void setCallbackConfirmReceiveFile(Callback callbackConfirmReceiveFile) {
        this.callbackConfirmReceiveFile = callbackConfirmReceiveFile;
    }

    public Callback callbackConfirmReceivePrivate;

    public void setCallbackConfirmReceivePrivate(Callback callbackConfirmReceivePrivate) {
        this.callbackConfirmReceivePrivate = callbackConfirmReceivePrivate;
    }

    public Callback callbackPrivateMsgReceive;

    public void setCallbackPrivateMsgReceive(Callback callbackPrivateMsgReceive) {
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
                        try {
                            callbackMsgAll.callback();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
                            callbackPrivateMsgReceive.callback();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                // обрабатываем подверждение о получении сервером файла
                if (message.equals("/receiveFileOK")) {
                    Platform.runLater(()-> {
                        try {
                            callbackConfirmReceiveFile.callback();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                // обрабатываем подтверждение получения сервером сообщения
                if (message.equals("/confReceiveAllMsg")) {
                    Platform.runLater(()-> {
                        try {
                            callbackConfirm.callback();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                // обрабатываем подтверждение получение приватного сообщения
                if (message.equals("/confReceivePrivate")) {
                    Platform.runLater(()-> {
                        try {
                            callbackConfirmReceivePrivate.callback();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                // обрабатываем подврждение об удалении файла на сервере
                if (message.equals("/deleteOK")) {
                    Platform.runLater(()-> {
                        try {
                            callbackConfirmDelete.callback();
                        } catch (Exception e) {
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
                            } catch (Exception e) {
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
                            try {
                                callbackClientList.callback();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
                            callbackAuth.callback();
                        } catch (Exception e) {
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
                            callbackAuth.callback();
                        } catch (Exception e) {
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
                            callbackReg.callback();
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
