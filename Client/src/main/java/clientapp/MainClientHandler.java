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

public class MainClientHandler extends ChannelInboundHandlerAdapter {

    private Chat chat = new Chat();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // блок отправки данных авторизации пользователя
        System.out.print("Enter login and password: ");
        Scanner scanner = new Scanner(System.in);
        String str = new String("/auth " + scanner.nextLine());
        MyCommandSend.sendCommand(str, ctx.channel());
    }

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
            chat.chat(ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
