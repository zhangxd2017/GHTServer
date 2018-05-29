package cn.ght.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class GHTFramer extends LengthFieldBasedFrameDecoder {

    public GHTFramer(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (in == null) {
            return null;
        }
        if (in.readableBytes() < 3) {
            throw new Exception("可读信息段比头部信息都小，你在逗我？");
        }

        byte head = in.readByte();

        byte lengthHigh = in.readByte();
        byte lengthLow = in.readByte();
        int length = lengthHigh * 256 + lengthLow;
        ByteBuf out = Unpooled.buffer(length + 5);
        out.writeByte(head);
        out.writeByte(lengthHigh);
        out.writeByte(lengthLow);

        for (int i = 0; i < length + 2; i++) {
            out.writeByte(in.readByte());
        }

        return out;
    }
}
