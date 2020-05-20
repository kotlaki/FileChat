package clientapp;

import common.MyCommandSend;
import common.MyFileReceive;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.nio.file.Paths;
import java.util.Scanner;

public class ClientInboundHandler extends ChannelInboundHandlerAdapter {

    private Controller controller = new Controller();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // принимаем служебные данные от сервера
        ByteBuf buf = (ByteBuf) msg;
        String str = buf.toString(CharsetUtil.UTF_8);
//        System.out.println("hh = " + str);
        if (str.startsWith("/file") || MyFileReceive.currentState == MyFileReceive.State.FILE) {
            MyFileReceive.receiveFile(buf, "client_storage/");
        }
//        System.out.println(str);
        if (MyFileReceive.currentState != MyFileReceive.State.FILE) {
            controller.chat(ctx, buf, str);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
