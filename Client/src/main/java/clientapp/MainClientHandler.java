package clientapp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.Scanner;

public class MainClientHandler extends ChannelInboundHandlerAdapter {

    private Chat chat = new Chat();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // блок отправки данных авторизации пользователя
        System.out.print("Enter login and password: ");
        Scanner scanner = new Scanner(System.in);
        String str = new String("/auth " + scanner.nextLine());
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(str, CharsetUtil.UTF_8));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // принимаем служебные данные от сервера
        ByteBuf buf = (ByteBuf) msg;
        String str = buf.toString(CharsetUtil.UTF_8);
        System.out.println(str);
        chat.chat(ctx, buf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
