package cn.ght.server;

import cn.ght.protocol.GHTMessage;
import cn.ght.util.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class GHTEncoder extends MessageToByteEncoder<GHTMessage> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, GHTMessage ghtMessage, ByteBuf byteBuf) throws Exception {
        byte[] data = null;
        switch (ghtMessage.getMessageType()) {
            case GHTMessage.MESSAGE_TYPE_HEART_BEAT:
                data = new byte[]{0x55, 0x00, 0x02, 0x00, 0x02, 0x00, 0x02};
                break;
            case GHTMessage.MESSAGE_TYPE_REGISTER_RESULT:
                int value = (int) ghtMessage.getMessageContent();
                if (value == 0) {
                    data = new byte[]{0x55, 0x00, 0x02, 0x02, 0x00, 0x00, 0x02};
                } else {
                    data = new byte[7];
                    data[0] = 0x55;
                    data[1] = 0x00;
                    data[2] = 0x02;
                    data[3] = 0x02;
                    data[4] = (byte) value;
                    data[5] = 0x00;
                    data[6] = (byte) (value + 2);
                }
                break;
            case GHTMessage.MESSAGE_TYPE_TRANSPARENT:
                data = getSendData((byte[]) ghtMessage.getMessageContent());
                break;
        }
        if (data != null) {
            byteBuf.writeBytes(data);
            String value = "发送数据:";
            for (int i = 0; i < data.length; i++) {
                value+= String.format(" %02x", data[i]);
            }
            LogUtils.print(value);
            channelHandlerContext.flush();
        }
    }

    private byte[] getSendData(byte[] src) {
        int length = src.length + 5;
        byte[] data = new byte[length];
        data[0] = 0x55;
        int sum = 0;
        for (int i = 0; i < src.length; i++) {
            sum += src[i];
            data[i + 3] = src[i];
        }
        data[1] = (byte) (src.length / 256);
        data[2] = (byte) (src.length % 256);
        data[src.length + 3] = (byte) (sum / 256);
        data[src.length + 4] = (byte) (sum % 256);
        return data;
    }
}
