package common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MyCommandSend {
    public static void sendCommand(String msg, Channel channel) throws IOException {
        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
        int lengthMsg = msgBytes.length;


        // сигнальный байт
        ByteBuf buf = null;
        byte[] flag = "/message".getBytes(StandardCharsets.UTF_8);     // [47, 109, 101, 115, 115, 97, 103, 101]
        buf = ByteBufAllocator.DEFAULT.directBuffer(flag.length);
        buf.writeBytes(flag);
        channel.write(buf);

        // шлем длинну сообщения
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(lengthMsg);
        channel.write(buf);

        // шлем сообщение в байтах
        buf = ByteBufAllocator.DEFAULT.directBuffer(lengthMsg);
        buf.writeBytes(msgBytes);
        channel.writeAndFlush(buf);
    }
}
