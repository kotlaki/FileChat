package serverapp;

import common.MyCommandReceive;
import common.MyFileReceive;
import common.MyFileSend;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;

/*
    /fr - file receive - получение файла
    /fs - file send - отправка файла
    /reg login password nickname description - регистрация нового пользователя

*/

public class MainServerHandler extends ChannelInboundHandlerAdapter {

    private String nickName;
    private String str;
    private String prev = "";
    private int count = 0;


    public MainServerHandler(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // прошли авторизацию, сообщили клиенту отправкой служебного сообщения
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(nickName, CharsetUtil.UTF_8));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf in = (ByteBuf) msg;

        if (checkFlag(in, "/file") || MyFileReceive.currentState == MyFileReceive.State.FILE) {
            System.out.println("count file = " + count++);
            MyFileReceive.receiveFile(in, "server_storage/");
        }

        if (in.isReadable() && checkFlag(in, "/message") || MyCommandReceive.currentState == MyCommandReceive.State.MESSAGE) {
            MyCommandReceive.receiveCommand(in);
        }


//        if (firstByte == 25 || MyFileReceive.currentState == MyFileReceive.State.FILE && firstByte != 88) {
//            System.out.println("count file = " + count++);
//            in.readerIndex(0);
//            MyFileReceive.receiveFile(in, "server_storage/");
//        }

//        if (firstByte == 88 || MyCommandReceive.currentState == MyCommandReceive.State.MESSAGE &&
//                MyFileReceive.currentState != MyFileReceive.State.FILE) {
//            in.readerIndex(0);
//            MyCommandReceive.receiveCommand(in);
//        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public boolean checkFlag(ByteBuf in, String command) {
        byte[] signal = command.getBytes(StandardCharsets.UTF_8);
        byte[] firstByte = new byte[signal.length];
        in.readBytes(firstByte);
        int count = 0;
        System.out.println("serv hand = " + Arrays.toString(firstByte));
        for (int i = 0; i < signal.length; i++) {
            if (signal[i] == firstByte[i]) {
                count++;
            }
        }
        in.readerIndex(0);
        return count == signal.length;
    }

}
