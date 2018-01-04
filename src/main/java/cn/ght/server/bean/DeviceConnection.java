package cn.ght.server.bean;

import io.netty.channel.ChannelHandlerContext;

public class DeviceConnection {

    private String deviceName;

    private ChannelHandlerContext connectContext;

    public DeviceConnection() {
    }

    public DeviceConnection(String deviceName, ChannelHandlerContext connectContext) {
        this.deviceName = deviceName;
        this.connectContext = connectContext;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public ChannelHandlerContext getConnectContext() {
        return connectContext;
    }

    public void setConnectContext(ChannelHandlerContext connectContext) {
        this.connectContext = connectContext;
    }
}
