package clientapp;

import clientapp.common.MyMessageAuth;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.SocketAddress;

public class MainHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        MyMessageAuth str = new MyMessageAuth("Hello my frend!!! Привет мой друг!!!");
        str.formSendMsg(ctx.channel());
    }
}
