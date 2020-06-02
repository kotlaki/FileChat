package common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MyCommandSend {
    public static void sendCommand(String msg, Channel channel) throws IOException {
        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8); // переводим сообщение в кодировке UTF-8 в байты
        int lengthMsg = msgBytes.length;
        ByteBuf buf = null;
        byte[] flag = "/message".getBytes(StandardCharsets.UTF_8);     // [47, 109, 101, 115, 115, 97, 103, 101]
        buf = ByteBufAllocator.DEFAULT.directBuffer(flag.length + 4 + lengthMsg);   // выделяем место
        buf.writeBytes(flag);                // сигнальный байт
        buf.writeInt(lengthMsg);            // шлем длинну сообщения
        buf.writeBytes(msgBytes);           // шлем сообщение в байтах
        channel.writeAndFlush(buf);         // шлем все данные из buf в канал
    }
}
