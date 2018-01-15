package cn.ght.protocol.bean;

public class DeviceInfo {

    public static final int DEVICE_TYPE_MODULE = 0;

    public static final int DEVICE_TYPE_PC = 1;

    public static final int DEVICE_TYPE_MOBILE = 2;


    private String deviceName;

    private int deviceType;

    public DeviceInfo() {
    }

    public DeviceInfo(String deviceName, int deviceType) {
        this.deviceName = deviceName;
        this.deviceType = deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }
}
