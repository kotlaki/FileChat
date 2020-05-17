package common;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MyCommandReceive {
    private enum State {
        IDLE, MESSAGE_LENGTH, MESSAGE
    }

    private static State currentState = State.IDLE;
    private static byte[] messageByte;
    private static int msgLength;
    private static int receivedMsgLength;


    public static void receiveCommand(ByteBuf buf) throws IOException {
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte reade = buf.readByte();
                if (reade == (byte) 77) {
                    currentState = State.MESSAGE_LENGTH;
                    receivedMsgLength = 0;
                }
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
