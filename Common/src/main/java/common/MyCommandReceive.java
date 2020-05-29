package common;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MyCommandReceive {
    public enum State {
        IDLE, MESSAGE_LENGTH, MESSAGE
    }

    public static State currentState = State.IDLE;
    private static byte[] messageByte;
    private static int msgLength;
    private static int receivedMsgLength;


    public static String receiveCommand(ByteBuf buf) throws IOException {
        while (buf.readableBytes() > 0) {
            byte[] signal = "/message".getBytes();
            if (currentState == State.IDLE) {
                currentState = State.MESSAGE_LENGTH;
                receivedMsgLength = 0;
                buf.readerIndex(signal.length);
            }

            // получаем длинну сообщения
            if (currentState == State.MESSAGE_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    msgLength = buf.readInt();
                    System.out.println("STATE: Message length received - " + msgLength);
                    currentState = State.MESSAGE;
                }
            }

            // получаем тело сообщения
            int index = 0;
            messageByte = new byte[msgLength];
            if (currentState == State.MESSAGE) {
                while (buf.readableBytes() > 0) {
                    messageByte[index++] = buf.readByte();
                    receivedMsgLength++;
                    if (msgLength == receivedMsgLength) {
                        receivedMsgLength = msgLength - 1;
                        currentState = State.IDLE;
                        break;
                    }
                }
            }
        }

        if (buf.readableBytes() == 0) {
            buf.release();
        }

        return new String(messageByte, StandardCharsets.UTF_8);
    }
}
