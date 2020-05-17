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


    public static void receiveCommand(ByteBuf buf) throws IOException {
        while (buf.readableBytes() > 0) {
//            if (currentState == State.IDLE) {
//                byte reade = buf.readByte();
//                if (reade == (byte) 88) {
//                    currentState = State.MESSAGE_LENGTH;
//                    receivedMsgLength = 0;
//                }
//            }

            byte[] signal = "/message".getBytes();

            if (currentState == State.IDLE) {
                currentState = State.MESSAGE_LENGTH;
                receivedMsgLength = 0;
                buf.readerIndex(signal.length);
            }

            if (currentState == State.MESSAGE_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    msgLength = buf.readInt();
                    System.out.println("STATE: File length received - " + msgLength);
                    currentState = State.MESSAGE;
                }
            }
            int index = 0;
            messageByte = new byte[msgLength];
            if (currentState == State.MESSAGE) {
                while (buf.readableBytes() > 0) {
                    messageByte[index++] = buf.readByte();
                    receivedMsgLength++;
                    if (msgLength == receivedMsgLength) {
                        currentState = State.IDLE;
                        break;
                    }
                }
            }
        }

        String message = new String(messageByte, StandardCharsets.UTF_8);
        System.out.println(message);

        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }
}
