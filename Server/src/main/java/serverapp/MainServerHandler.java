package serverapp;

import common.MyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class MainServerHandler extends ChannelInboundHandlerAdapter {

    private String nickName;
    private MyMessage mess = new MyMessage();

    public MainServerHandler(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // прошли авторизацию, сообщили клиенту отправкой служебного сообщения
        System.out.println("User " + nickName + " good!");
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(nickName, CharsetUtil.UTF_8));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("read");
        ByteBuf in = (ByteBuf) msg;
        String str = mess.formReceiveMsg(in);
        System.out.println(nickName + " : " + str);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
