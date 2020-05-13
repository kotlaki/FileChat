package common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MyMessage {

    public enum State {
        IDLE, LENGTH_MESSAGE, MESSAGE
    }

    private State currentState = State.IDLE;
    private String myMessage;
    int msgLength;
    int receivedFileLength;
    private byte[] temp;


    public MyMessage() {
    }

    public MyMessage(String myMessage) {
        this.myMessage = myMessage;
    }

    public void formSendMsg(Channel channel) {
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 66);
        channel.write(buf);

        byte[] myMessageByte = myMessage.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(myMessageByte.length);
        channel.write(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(myMessageByte.length);
        buf.writeBytes(myMessageByte);
         channel.writeAndFlush(buf);
    }

    public String formReceiveMsg(ByteBuf buf) {
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readed = buf.readByte();
                if (readed == (byte) 66) {
                    currentState = State.LENGTH_MESSAGE;
                    receivedFileLength = 0;
                    System.out.println("STATE: It`s Message");
                }
            }

            if (currentState == State.LENGTH_MESSAGE) {
                if (buf.readableBytes() >= 4) {
                    msgLength = buf.readInt();
                    System.out.println("Длина сообщения = " + msgLength);
                    currentState = State.MESSAGE;
                }
            }

            if (currentState == State.MESSAGE) {
                temp = new byte[msgLength];
                int i = 0;
                while (buf.readableBytes() > 0) {
                    byte tempByte = buf.readByte();
                    temp[i++] = tempByte;
                    receivedFileLength++;
                    if (msgLength == receivedFileLength) {
                        currentState = State.IDLE;
                        break;
                    }
                }
            }
        }

        String str;
        str = new String(temp, StandardCharsets.UTF_8);
        Arrays.fill(temp, (byte) 0);

        if (buf.readableBytes() == 0) {
            System.out.println(buf.release());
        }

        return str;
    }

}
