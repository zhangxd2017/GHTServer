package cn.ght.server;

import cn.ght.protocol.GHTMessage;
import cn.ght.util.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.Arrays;
import java.util.List;

public class GHTDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        String value = "接收数据:";
        if (!(byteBuf instanceof EmptyByteBuf)) {
            if (byteBuf.readableBytes() < 1) {
                LogUtils.print("获取不到数据头0x55");
                return;
            }
            byte controlByte = byteBuf.readByte();
            if (controlByte == 0x55) {
                value += String.format(" %02x", controlByte);
                if (byteBuf.readableBytes() < 1) {
                    LogUtils.print("获取不到数据长度高位");
                    return;
                }
                byte lengthHigh = byteBuf.readByte();
                value += String.format(" %02x", lengthHigh);
                if (byteBuf.readableBytes() < 1) {
                    LogUtils.print("获取不到数据长度低位");
                    return;
                }
                byte lengthLow = byteBuf.readByte();
                value += String.format(" %02x", lengthLow);
                int length = lengthHigh * 256 + lengthLow;
                LogUtils.print("计算数据长度为:" + length);
                byte[] content = new byte[length];
                if (byteBuf.readableBytes() < length) {
                    LogUtils.print("获取不到完整的数据");
                    return;
                }
                byteBuf.readBytes(content);
                for (int i = 0; i < content.length; i++) {
                    value += String.format(" %02x", content[i]);
                }
                if (byteBuf.readableBytes() < 1) {
                    LogUtils.print("获取不到数据校验高位");
                    return;
                }
                byte subHigh = byteBuf.readByte();
                value += String.format(" %02x", subHigh);
                if (byteBuf.readableBytes() < 1) {
                    LogUtils.print("获取不到数据校验低位");
                    return;
                }
                byte subLow = byteBuf.readByte();
                value += String.format(" %02x", subLow);
                LogUtils.print("校验结果为:" + ((subHigh < 0 ? 256 + subHigh : subHigh) * 256 + (subLow < 0 ? 256 + subLow : subLow)));
                //校验
                int sum = 0;
                for (int i = 0; i < length; i++) {
                    sum += content[i];
                }
                LogUtils.print("校验计算结果为:" + sum);
                if ((sum - (subHigh < 0 ? 256 + subHigh : subHigh) * 256 - (subLow < 0 ? 256 + subLow : subLow)) == 0) {
                    GHTMessage message = new GHTMessage(content[0], Arrays.copyOfRange(content, 1, length));
                    list.add(message);
                } else {
                    value += "!!! 数据校验错误";
                }

            } else {
                value += "!!! 错误";
            }
        } else {
            value += "!!! 为空";
        }
        LogUtils.print(value);
    }
}
