package serverapp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class AuthorizationHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            ByteBuf in = (ByteBuf) msg;
            String str = in.toString(CharsetUtil.UTF_8);
            if (str.startsWith("/auth /reg")) {
                ctx.channel().writeAndFlush(Unpooled.copiedBuffer(Authorization.addUser(str, ctx), CharsetUtil.UTF_8));
            } else {
                Authorization.checkUser(str, ctx);

            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
