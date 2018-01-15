package cn.ght.server.manager;

import cn.ght.server.bean.MobileConnection;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

public class MobileManager {

    private static MobileManager instance = null;
    private static final Object locker = new Object();

    private List<MobileConnection> mobiles;

    public static MobileManager getInstance() {
        if (instance == null) {
            synchronized (locker) {
                if (instance == null) {
                    instance = new MobileManager();
                }
            }
        }
        return instance;
    }

    private MobileManager() {
        mobiles = new ArrayList<MobileConnection>();
    }

    public boolean contains(String deviceName) {
        boolean contains = false;
        for (MobileConnection connection : mobiles) {
            if (connection.getDeviceName().equals(deviceName)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    public void add(MobileConnection mobileConnection) {
        synchronized (locker) {
            mobiles.add(mobileConnection);
        }
    }

    public void add(String deviceName, ChannelHandlerContext channelHandlerContext) {
        add(new MobileConnection(deviceName, channelHandlerContext));
    }

    public MobileConnection getByContext(ChannelHandlerContext channelHandlerContext) {
        MobileConnection mobileConnection = null;
        synchronized (locker) {
            for (MobileConnection connection : mobiles) {
                if (connection.getConnectContext().equals(channelHandlerContext)) {
                    mobileConnection = connection;
                    break;
                }
            }
        }
        return mobileConnection;
    }


    public void remove(MobileConnection mobileConnection) {
        synchronized (locker) {
            mobiles.remove(mobileConnection);
        }
    }
}
