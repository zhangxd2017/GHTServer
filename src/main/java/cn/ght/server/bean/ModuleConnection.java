package cn.ght.server.bean;

import io.netty.channel.ChannelHandlerContext;

public class ModuleConnection extends DeviceConnection {

    private LocationInfo locationInfo;

    private PCConnection pcConnection;

    public ModuleConnection() {
    }

    public ModuleConnection(String deviceName, ChannelHandlerContext context) {
        super(deviceName, context);
    }

    public LocationInfo getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(LocationInfo locationInfo) {
        this.locationInfo = locationInfo;
    }

    public PCConnection getPcConnection() {
        return pcConnection;
    }

    public void setPcConnection(PCConnection pcConnection) {
        this.pcConnection = pcConnection;
    }
}
