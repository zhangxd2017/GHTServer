package cn.ght.server;

import cn.ght.protocol.GHTMessage;
import cn.ght.server.manager.MobileManager;
import cn.ght.server.manager.ModelManager;
import cn.ght.server.model.Model;
import cn.ght.util.LogUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.*;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class GHTHandler extends SimpleChannelInboundHandler<GHTMessage> {

    Timer timer = new HashedWheelTimer();

    private void addTask(TimerTask task) {
        timer.newTimeout(task, 5, TimeUnit.SECONDS);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        //super.channelActive(ctx);
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                new GenericFutureListener<Future<? super Channel>>() {
                    @Override
                    public void operationComplete(Future<? super Channel> future) throws Exception {
                        if (future.isSuccess()) {
                            LogUtils.print("握手成功");
                            //ctx.writeAndFlush(GHTMessage.MessageHeartBeat);
                        } else {
                            LogUtils.print("握手失败");
                        }
                    }
                }
        );
    }


    public String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (Exception e) {
            LogUtils.print("读文件错误" + e.getMessage());
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] getDataFromString(String value) {
        if (value.matches("^\\w\\w( \\w\\w)*$")) {
            String[] datas = value.split(" ");
            byte[] data = new byte[datas.length];
            try {
                for (int i = 0; i < datas.length; i++) {
                    data[i] = Integer.decode("0x" + datas[i]).byteValue();
                }
                return data;
            } catch (Exception e) {
                LogUtils.print("转换数据错误" + e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    protected void messageReceived(final ChannelHandlerContext channelHandlerContext, GHTMessage ghtMessage) throws Exception {
        switch (ghtMessage.getMessageType()) {
            case GHTMessage.MESSAGE_TYPE_HEART_BEAT:
                channelHandlerContext.writeAndFlush(GHTMessage.MessageHeartBeat);
                addTask(new TimerTask() {
                    @Override
                    public void run(Timeout timeout) throws Exception {
                        byte[] sendData = null;
                        String path = "data.txt";
                        if (new File(path).exists()) {
                            String data = readToString(path);
                            sendData = getDataFromString(data);
                        }
                        else {
                            LogUtils.print("data.txt 不存在");
                        }
                        if (sendData != null)
                            channelHandlerContext.writeAndFlush(new GHTMessage(GHTMessage.MESSAGE_TYPE_TRANSPARENT, sendData));
                    }
                });
                break;
            case GHTMessage.MESSAGE_TYPE_REGISTER: {
                byte[] data = (byte[]) ghtMessage.getMessageContent();
                String imei = getIMEI(data);
                String ccid = getCCID(data);
                String location = getLocation(data);
                LogUtils.print("IMEI:" + imei + "-ICCID:" + ccid + "-LAC:" + location);
                ModelManager.getInstance().addModel(new Model(imei, ccid, location, channelHandlerContext));
                channelHandlerContext.writeAndFlush(GHTMessage.MessageRegisterSuccess);
            }
            break;
            case GHTMessage.MESSAGE_TYPE_TRANSPARENT:
                byte[] message = (byte[]) ghtMessage.getMessageContent();
                MobileManager.getInstance().sendMessage(new GHTMessage(GHTMessage.MESSAGE_TYPE_PHONE_TRANSPARENT, message));
                break;
            case GHTMessage.MESSAGE_TYPE_TRANSPARENT_ERROR:
                MobileManager.getInstance().sendMessage(new GHTMessage(GHTMessage.MESSAGE_TYPE_PHONE_TRANSPARENT_ERROR, 1));
                break;
            case GHTMessage.MESSAGE_TYPE_PHONE_LOGIN:
                MobileManager.getInstance().addMobile(channelHandlerContext);
                //回复列表
                channelHandlerContext.writeAndFlush(new GHTMessage(GHTMessage.MESSAGE_TYPE_PHONE_LOGIN_RESPONSE, ModelManager.getInstance().getList()));
                break;
            case GHTMessage.MESSAGE_TYPE_PHONE_TRANSPARENT: {
                byte[] data = (byte[]) ghtMessage.getMessageContent();
                String imei = getIMEI(data);
                byte[] content = getContent(data);
                ModelManager.getInstance().sendMessage(imei, new GHTMessage(GHTMessage.MESSAGE_TYPE_TRANSPARENT, content));
            }
            break;
        }
    }

    private String getIMEI(byte[] data) {
        int length1 = data[0];
        return new String(Arrays.copyOfRange(data, 1, length1 + 1));
    }

    private String getCCID(byte[] data) {
        int length1 = data[0];
        int length2 = data[length1 + 1];
        return new String(Arrays.copyOfRange(data, length1 + 2, length1 + length2 + 2));
    }

    private String getLocation(byte[] data) {
        int length1 = data[0];
        int length2 = data[length1 + 1];
        int length3 = data[length1 + length2 + 2];
        return new String(Arrays.copyOfRange(data, length1 + length2 + 3, length1 + length2 + length3 + 3));
    }

    private byte[] getContent(byte[] data) {
        return null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        LogUtils.print(cause.getMessage());
    }
}
