package clientapp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.io.IOException;

public class MainClientHandler extends ChannelInboundHandlerAdapter {

    private Chat chat = new Chat();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // принимаем служебные данные от сервера
        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteBuf buf = (ByteBuf) msg;
                String str = buf.toString(CharsetUtil.UTF_8);
                System.out.println(str);
                try {
                    chat.сhat(ctx, buf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
