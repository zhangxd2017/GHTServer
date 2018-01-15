package cn.ght.server.manager;

import cn.ght.server.bean.DeviceConnection;
import cn.ght.server.bean.PCConnection;
import io.netty.channel.ChannelHandlerContext;

public class PCManager {
    private static PCManager instance = null;
    private static Object locker = new Object();

    public static PCManager getInstance() {
        if (instance == null) {
            synchronized (locker) {
                if (instance == null) {
                    instance = new PCManager();
                }
            }
        }
        return instance;
    }

    private PCManager() {
    }

    public PCConnection getByContext(ChannelHandlerContext channelHandlerContext) {
        return null;
    }

    public void remove(PCConnection deviceConnection) {
    }
}
