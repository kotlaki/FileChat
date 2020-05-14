package serverapp;

import common.MyFileReceive;
import common.MyFileSend;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class MainServerHandler extends ChannelInboundHandlerAdapter {

    private String nickName;
    private String str;
    private String prev = "";

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
        ByteBuf in = (ByteBuf) msg;
        // обрабатываем первую отправку чтобы узнать наличие /file в строке
        if (!prev.equals("/file")) {
            str = in.toString(CharsetUtil.UTF_8);
            System.out.println(nickName + " написал: " + str);
        }
        // т.к предидущая строка содержала /file то обрабатываем прием файла, после чего скидываем предидущий показатель на ""
        if(prev.equals("/file")) {
            MyFileReceive.reciveFile(in);
            prev = "";
        }
        // после получении строки проверяем начинается ли она с /file
        if (str.startsWith("/file")) {
            // помечаем что предидущая строка содержала /file
            prev = "/file";
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
