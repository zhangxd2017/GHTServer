package cn.ght.server;

import cn.ght.protocol.MessageData;
import cn.ght.protocol.MessageModel;
import cn.ght.protocol.MessageType;
import cn.ght.protocol.RegisterData;
import cn.ght.protocol.bean.DeviceInfo;
import cn.ght.protocol.bean.ModuleInfo;
import cn.ght.protocol.bean.PCCommon;
import cn.ght.server.bean.*;
import cn.ght.server.manager.MobileManager;
import cn.ght.server.manager.ModuleManager;
import cn.ght.server.manager.PCManager;
import cn.ght.util.LogUtils;
import cn.ght.util.StringUtils;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GHTMHandler extends SimpleChannelInboundHandler {


    private Timer timer = null;
    private Timer pcTimer = null;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    protected void messageReceived(final ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
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
                                    if (PCManager.getInstance().contains(deviceInfo.getDeviceName())) {
                                        writeCmd(channelHandlerContext, JSON.toJSONString(new MessageData(MessageType.REGISTER, JSON.toJSONString(new RegisterData(RegisterData.STATE_FAIL, "Already Register:" + deviceInfo.getDeviceName())))));
                                        channelHandlerContext.close();
                                    } else {
                                        ModuleConnection connection = ModuleManager.getInstance().getByName(deviceInfo.getImei());
                                        if (connection != null) {
                                            LogUtils.print("--Bind PC:" + deviceInfo.getDeviceName() + ",Module:" + deviceInfo.getImei());
                                            PCConnection pcConnection = new PCConnection(deviceInfo.getDeviceName(), channelHandlerContext);
                                            connection.setPcConnection(pcConnection);
                                            PCManager.getInstance().add(pcConnection);
                                            writeCmd(channelHandlerContext, JSON.toJSONString(new MessageData(MessageType.REGISTER, JSON.toJSONString(new RegisterData(RegisterData.STATE_SUCCESS, "")))));
                                            //通知给所有的移动端
                                            MobileManager.getInstance().notityAllMobile("{\"cmd\":\"set_power_on_pc\",\"data\":{\"module\":\"" + connection.getDeviceName() + "\",\"pc\":\"" + deviceInfo.getDeviceName() + "\"}}");
                                        }
                                    }
                                }
                                break;
                                case DeviceInfo.DEVICE_TYPE_MOBILE: {
                                    if (MobileManager.getInstance().contains(deviceInfo.getDeviceName())) {
                                        writeCmd(channelHandlerContext, JSON.toJSONString(new MessageData(MessageType.REGISTER, JSON.toJSONString(new RegisterData(RegisterData.STATE_FAIL, "Already Register:" + deviceInfo.getDeviceName())))));
                                        channelHandlerContext.close();
                                    } else {
                                        MobileManager.getInstance().add(deviceInfo.getDeviceName(), channelHandlerContext);
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
                    case MessageType.HEART_BEAT: {
                        if (pcTimer != null) {
                            System.out.println("stop pcTimer");
                            pcTimer.stop();
                            pcTimer = null;
                        }
                        System.out.println("start pcTimer");
                        pcTimer = new HashedWheelTimer();
                        pcTimer.newTimeout(new TimerTask() {
                            @Override
                            public void run(Timeout timeout) throws Exception {
                                channelHandlerContext.close();
                            }
                        }, 10, TimeUnit.SECONDS);
                    }
                    break;
                    case MessageType.REPORT_STATE: {
                        if (timer != null) {
                            System.out.println("stop timer");
                            timer.stop();
                            timer = null;
                        }
                        System.out.println("start timer");
                        timer = new HashedWheelTimer();
                        timer.newTimeout(new TimerTask() {
                            @Override
                            public void run(Timeout timeout) throws Exception {
                                channelHandlerContext.close();
                            }
                        }, 20, TimeUnit.SECONDS);
                        ModuleConnection connection = ModuleManager.getInstance().getByContext(channelHandlerContext);
                        if (connection != null) {
                            MobileManager.getInstance().notityAllMobile(JSON.toJSONString(new MessageData(MessageType.REPORT_STATE, JSON.toJSONString(new PCCommon(connection.getDeviceName(), cmd.getData())))));
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
                            System.out.println("------URL:" + urlStr);
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
                        PCCommon pc = JSON.parseObject(cmd.getData(), PCCommon.class);
                        PCConnection connection = PCManager.getInstance().getByName(pc.getName());
                        if (connection != null) {
                            writeCmd(connection.getConnectContext(), JSON.toJSONString(new FileListCmd(MessageType.FILE_LIST, pc.getExtraData())));
                        }
                    }
                    break;
                    case MessageType.SET_DELETE_FILE: {
                        PCCommon pc = JSON.parseObject(cmd.getData(), PCCommon.class);
                        PCConnection connection = PCManager.getInstance().getByName(pc.getName());
                        if (connection != null) {
                            writeCmd(connection.getConnectContext(), JSON.toJSONString(new FileListCmd(MessageType.DELETE_FILE, pc.getExtraData())));
                        }
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
                        PCCommon pc = JSON.parseObject(cmd.getData(), PCCommon.class);
                        PCConnection connection = PCManager.getInstance().getByName(pc.getName());
                        if (connection != null) {
                            writeCmd(connection.getConnectContext(), JSON.toJSONString(new FileListCmd(MessageType.POWER_OFF_PC, pc.getExtraData())));
                        }
                    }
                    break;
                    case MessageType.FILE_LIST: {
                        MobileManager.getInstance().notityAllMobile("{\"cmd\":\"get_file_list\",\"data\":" + cmd.getData() + "}");
                    }
                    break;
                    case MessageType.DELETE_FILE: {
                        MobileManager.getInstance().notityAllMobile("{\"cmd\":\"set_delete_file\"}");
                    }
                    break;
                    case MessageType.POWER_OFF_PC: {
                        MobileManager.getInstance().notityAllMobile("{\"cmd\":\"set_power_off_pc\"}");
                    }
                    break;
                    case MessageType.LOG_OUT: {
                        MobileManager.getInstance().notityAllMobile("{\"cmd\":\"set_log_out\",\"data\":" + cmd.getData() + "}");
                    }
                    break;
                    case MessageType.SET_LOG_OUT: {
                        PCCommon pc = JSON.parseObject(cmd.getData(), PCCommon.class);
                        PCConnection connection = PCManager.getInstance().getByName(pc.getName());
                        if (connection != null) {
                            writeCmd(connection.getConnectContext(), JSON.toJSONString(new FileListCmd(MessageType.LOG_OUT, pc.getExtraData())));
                        }
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
            if (timer != null) {
                timer.stop();
                timer = null;
            }
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
