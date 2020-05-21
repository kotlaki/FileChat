package clientapp;

import clientapp.controllers.NewControllerStorage;
import common.MyCommandReceive;
import common.MyFileReceive;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.Arrays;

public class ClientHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // принимаем служебные данные от сервера
        ByteBuf buf = (ByteBuf) msg;
        String str = buf.toString(CharsetUtil.UTF_8);
//        Controller.currentController.txtChat.appendText(str + "\n");
        if (str.startsWith("/file") || MyFileReceive.currentState == MyFileReceive.State.FILE) {
            MyFileReceive.receiveFile(buf, "client_storage/");
        } else
            // приемка сообщений
            if (str.startsWith("/message") || MyCommandReceive.currentState == MyCommandReceive.State.MESSAGE) {
                String message = MyCommandReceive.receiveCommand(buf);
                System.out.println("From server = " + message);
                if (message.startsWith("/req_list")) {
                    // TO DO от сюда надо передать строку в NewControllerStorage
                    NewControllerStorage.msgFromServer = message;
                }
            }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
