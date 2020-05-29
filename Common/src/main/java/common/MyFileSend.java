package common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MyFileSend {

    public static void sendFile(Path path, Channel channel) throws IOException {
        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
        byte[] filenameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);  // переводим имя файла в байты
        ByteBuf buf = null;
        byte[] flag = "/file".getBytes(StandardCharsets.UTF_8);     // [47, 102, 105, 108, 101]
        buf = ByteBufAllocator.DEFAULT.directBuffer(flag.length + 4 + filenameBytes.length + 8);
        buf.writeBytes(flag);           // передаем сигнальный байт в buf
        buf.writeInt(filenameBytes.length);          // передаем длинну имени файла в buf
        buf.writeBytes(filenameBytes);              // передаем имя файла в buf
        buf.writeLong(Files.size(path));           // передаем размер файла в buf
        channel.writeAndFlush(buf);
        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);  // передаем файл в канал
    }
}
