package cn.ght.server;

import cn.ght.protocol.MessageData;
import cn.ght.server.bean.DeviceConnection;
import cn.ght.server.bean.LocationInfo;
import cn.ght.server.bean.ModuleConnection;
import cn.ght.server.manager.ModuleManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.SocketAddress;

public class GHTServerHandler extends SimpleChannelInboundHandler<MessageData.Message> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println("--Connection From:" + ctx.channel().toString());
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        System.out.println("--Registered From:" + ctx.channel().toString());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        System.out.println("--Unregistered To:" + ctx.channel().toString());
    }

    protected void messageReceived(ChannelHandlerContext channelHandlerContext, MessageData.Message message) {
        System.out.println("--Message From:" + channelHandlerContext.channel().toString());
        System.out.println("    " + message.toString());
        switch (message.getMsgType()) {
            case Login_Request:
                if (message.hasLoginRequest()) {
                    switch (message.getLoginRequest().getDeviceType()) {
                        case DeviceModule:
                            if (ModuleManager.getInstance().contains(message.getLoginRequest().getDeviceName())) {
                                sendLoginResponse(channelHandlerContext, false, "已存在相同名字的模块！");
                                channelHandlerContext.disconnect();
                                channelHandlerContext.close();
                            } else {
                                sendLoginResponse(channelHandlerContext, true, "");
                                ModuleManager.getInstance().add(message.getLoginRequest().getDeviceName(), channelHandlerContext);
                                sendReportGPSQuery(channelHandlerContext);
                            }
                            break;
                        case DevicePC:
                            break;
                        case DeviceAndroid:
                        case DeviceIOS:
                            break;
                    }
                }
                break;
            case Power_On_PC:
                //发布通知
                break;
            case Report_GPS:
                if (message.hasReportGps()) {
                    ModuleConnection moduleConnection = ModuleManager.getInstance().getByName(message.getReportGps().getDeviceName());
                    moduleConnection.setLocationInfo(new LocationInfo(message.getReportGps().getLongitude(), message.getReportGps().getLatitude()));
                }
                break;
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        disconnectDevice(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        System.out.println(cause.toString());
    }

    private void sendMessage(ChannelHandlerContext channelHandlerContext, Object msg) {
        System.out.println("--Message To:" + channelHandlerContext.channel().toString());
        System.out.println("    " + msg.toString());
        channelHandlerContext.writeAndFlush(msg);
    }

    /**
     * 回复客户端登录状态
     *
     * @param channelHandlerContext 连接上下文
     * @param success               是否登录成功
     * @param reason                失败原因
     */
    private void sendLoginResponse(ChannelHandlerContext channelHandlerContext, boolean success, String reason) {
        MessageData.Message.Builder builder = MessageData.Message.newBuilder();
        builder.setMsgType(MessageData.MSG.Login_Response)
                .setLoginResponse(MessageData.LoginResponse.newBuilder()
                        .setResult(success ? 0 : 1)
                        .setReason(reason).build());
        sendMessage(channelHandlerContext, builder.build());
    }

    /**
     * 向模块发送上报GPS指令
     *
     * @param channelHandlerContext 连接上下文
     */
    private void sendReportGPSQuery(ChannelHandlerContext channelHandlerContext) {
        MessageData.Message.Builder builder = MessageData.Message.newBuilder();
        builder.setMsgType(MessageData.MSG.Report_GPS_Query);
        builder.setReportGpsCmd(MessageData.ReportGPSQuery.newBuilder().build());
        sendMessage(channelHandlerContext, builder.build());
    }

    /**
     * 向模块发送开机指令
     *
     * @param channelHandlerContext 连接上下文
     */
    private void sendPowerOnQuery(ChannelHandlerContext channelHandlerContext) {
        MessageData.Message.Builder builder = MessageData.Message.newBuilder();
        builder.setMsgType(MessageData.MSG.Power_On_PC_Query);
        builder.setPowerOnPcCmd(MessageData.PowerOnPCQuery.newBuilder().build());
        sendMessage(channelHandlerContext, builder.build());
    }

    /**
     * 关闭当前连接并删除索引
     *
     * @param channelHandlerContext 连接上下文
     */
    private void disconnectDevice(ChannelHandlerContext channelHandlerContext) {
        DeviceConnection deviceConnection;
        deviceConnection = ModuleManager.getInstance().getByContext(channelHandlerContext);
        if (deviceConnection != null) {
            System.out.println("--Disconnect:" + channelHandlerContext.channel().toString());
            System.out.println("    Remove Module:" + deviceConnection.getDeviceName());
            ModuleManager.getInstance().remove((ModuleConnection) deviceConnection);
            return;
        }
    }
}
