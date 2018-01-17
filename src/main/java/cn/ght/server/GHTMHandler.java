package cn.ght.server;

import cn.ght.protocol.MessageData;
import cn.ght.protocol.MessageModel;
import cn.ght.protocol.MessageType;
import cn.ght.protocol.RegisterData;
import cn.ght.protocol.bean.DeviceInfo;
import cn.ght.protocol.bean.ModuleInfo;
import cn.ght.server.bean.*;
import cn.ght.server.manager.MobileManager;
import cn.ght.server.manager.ModuleManager;
import cn.ght.server.manager.PCManager;
import cn.ght.util.LogUtils;
import cn.ght.util.StringUtils;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class GHTMHandler extends SimpleChannelInboundHandler {


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    protected void messageReceived(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        LogUtils.print("--Message From:" + channelHandlerContext.channel().toString());
        LogUtils.print("    " + o.toString());
        if (o instanceof String) {
            MessageData cmd = JSON.parseObject((String) o, MessageData.class);
            if (cmd != null) {
                switch (cmd.getCmd()) {
                    case MessageType.REGISTER:
                        if (cmd.getData().startsWith(MessageModel.L_BRACES)) {
                            //移动端 PC客户端注册
                            DeviceInfo deviceInfo = JSON.parseObject(cmd.getData(), DeviceInfo.class);
                            switch (deviceInfo.getDeviceType()) {
                                case DeviceInfo.DEVICE_TYPE_PC: {

                                }
                                break;
                                case DeviceInfo.DEVICE_TYPE_MOBILE: {
                                    if (MobileManager.getInstance().contains(deviceInfo.getDeviceName())) {
                                        writeCmd(channelHandlerContext, JSON.toJSONString(new MessageData(MessageType.REGISTER, JSON.toJSONString(new RegisterData(RegisterData.STATE_FAIL, "Already Register:" + deviceInfo.getDeviceName())))));
                                        channelHandlerContext.close();
                                    } else {
                                        writeCmd(channelHandlerContext, JSON.toJSONString(new MessageData(MessageType.REGISTER, JSON.toJSONString(new RegisterData(RegisterData.STATE_SUCCESS, "")))));
                                    }
                                }
                                break;
                            }
                        } else {    //暂时认为不带类型的是模块发送的注册
                            String moduleName = cmd.getData();
                            if (ModuleManager.getInstance().contains(moduleName)) {
                                //已存在相同的名字 虽然模块的IMEI理论上不会重复
                            } else {
                                ModuleManager.getInstance().add(moduleName, channelHandlerContext);
                                writeCmd(channelHandlerContext, MessageModel.REPORT_LBS_CMD);
                            }
                        }
                        break;
                    case MessageType.REPORT_LBS: {
                        ModuleConnection connection = ModuleManager.getInstance().getByContext(channelHandlerContext);
                        if (connection != null) {
                            if (connection.getLocationInfo() == null) {
                                connection.setLocationInfo(new LocationInfo());
                            }
                            connection.getLocationInfo().setReportLBSData(cmd.getData());
                            //根据基站信息查询GPS位置
                            String urlStr = UrlConstant.getUrl(cmd.getData());
                            if (!StringUtils.isEmpty(urlStr)) {
                                URL url = new URL(urlStr);
                                try {
                                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                                    httpURLConnection.setDoOutput(true);
                                    httpURLConnection.setDoInput(true);
                                    httpURLConnection.setConnectTimeout(10000);
                                    httpURLConnection.setReadTimeout(10000);
                                    int responseCode = httpURLConnection.getResponseCode();
                                    if (HttpURLConnection.HTTP_OK == responseCode) {
                                        StringBuffer sb = new StringBuffer();
                                        String readLine;
                                        BufferedReader responseReader;
                                        // 处理响应流，必须与服务器响应流输出的编码一致
                                        responseReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
                                        while ((readLine = responseReader.readLine()) != null) {
                                            sb.append(readLine).append("\n");
                                        }
                                        responseReader.close();
                                        LogUtils.print("----Get Location:" + sb.toString());
                                        GPSDetail detail = JSON.parseObject(sb.toString(), GPSDetail.class);
                                        connection.getLocationInfo().setLongitude(detail.getLongitude());
                                        connection.getLocationInfo().setLatitude(detail.getLatitude());
                                    }
                                } catch (Exception e) {
                                    LogUtils.print("!!!!" + e.getMessage());
                                }
                            } else {
                                LogUtils.print("!!!!LBS info params error:" + cmd.getData());
                            }
                        }
                    }
                    break;
                    case MessageType.POWER_ON_PC: {
                        ModuleConnection connection = ModuleManager.getInstance().getByContext(channelHandlerContext);
                        if (connection != null) {

                        }
                    }
                    break;
                    case MessageType.GET_MODULE_LIST: { //移动端发送过来的
                        List<ModuleInfo> infos = ModuleManager.getInstance().getAllModules();
                        MessageData sendData = new MessageData(MessageType.GET_MODULE_LIST, JSON.toJSONString(infos));
                        writeCmd(channelHandlerContext, JSON.toJSONString(sendData));
                    }
                    break;
                    case MessageType.SET_POWER_ON_PC: { //移动端发送给服务器转发到指定的模块
                        String moduleName = cmd.getData();
                        ModuleConnection connection = ModuleManager.getInstance().getByName(moduleName);
                        if (connection != null) {
                            writeCmd(connection.getConnectContext(), MessageModel.POWER_ON_PC_CMD);
                        }
                    }
                    break;
                    case MessageType.GET_FILE_LIST: {

                    }
                    break;
                    case MessageType.SET_DELETE_FILE: {

                    }
                    break;
                    case MessageType.GET_LOCATION_INFO: {
                        String moduleName = cmd.getData();
                        ModuleConnection connection = ModuleManager.getInstance().getByName(moduleName);
                        if (connection != null) {
                            writeCmd(connection.getConnectContext(), MessageModel.REPORT_LBS_CMD);
                        }
                    }
                    break;
                    case MessageType.SET_POWER_OFF_PC: {

                    }
                    break;
                    case MessageType.FILE_LIST: {

                    }
                    break;
                    case MessageType.DELETE_FILE: {

                    }
                    break;
                    case MessageType.POWER_OFF_PC: {

                    }
                    break;
                }
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        disconnectChannel(ctx);
        super.channelInactive(ctx);
    }

    private void disconnectChannel(ChannelHandlerContext channelHandlerContext) {
        DeviceConnection deviceConnection;
        deviceConnection = ModuleManager.getInstance().getByContext(channelHandlerContext);
        if (deviceConnection != null) {
            LogUtils.print("--Disconnect:" + channelHandlerContext.channel().toString());
            LogUtils.print("    Remove Module:" + deviceConnection.getDeviceName());
            ModuleManager.getInstance().remove((ModuleConnection) deviceConnection);
            return;
        }
        deviceConnection = MobileManager.getInstance().getByContext(channelHandlerContext);
        if (deviceConnection != null) {
            LogUtils.print("--Disconnect:" + channelHandlerContext.channel().toString());
            LogUtils.print("    Remove Mobile:" + deviceConnection.getDeviceName());
            MobileManager.getInstance().remove((MobileConnection) deviceConnection);
            return;
        }
        deviceConnection = PCManager.getInstance().getByContext(channelHandlerContext);
        if (deviceConnection != null) {
            LogUtils.print("--Disconnect:" + channelHandlerContext.channel().toString());
            LogUtils.print("    Remove PC:" + deviceConnection.getDeviceName());
            PCManager.getInstance().remove((PCConnection) deviceConnection);
        }
    }

    private void writeCmd(ChannelHandlerContext channelHandlerContext, String cmd) {
        LogUtils.print("--Message To:" + channelHandlerContext.channel().toString());
        LogUtils.print("    " + cmd);
        channelHandlerContext.writeAndFlush(cmd);
    }
}
