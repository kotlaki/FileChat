package clientapp;

import clientapp.controllers.ControllerStorage;
import common.Callback;
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
            }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
