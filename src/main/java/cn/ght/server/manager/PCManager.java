package cn.ght.server.manager;

import cn.ght.server.bean.PCConnection;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

public class PCManager {
    private static PCManager instance = null;
    private static Object locker = new Object();

    private List<PCConnection> pcs;

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
        pcs = new ArrayList<>();
    }

    public boolean contains(String deviceName) {
        boolean contains = false;
        for (PCConnection connection : pcs) {
            if (connection.getDeviceName().equals(deviceName)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    public PCConnection getByName(String name) {
        PCConnection pcConnection = null;
        synchronized (locker) {
            for (PCConnection connection : pcs) {
                if (connection.getDeviceName().equals(name)) {
                    pcConnection = connection;
                    break;
                }
            }
        }
        return pcConnection;
    }

    public PCConnection getByContext(ChannelHandlerContext channelHandlerContext) {
        PCConnection pcConnection = null;
        synchronized (locker) {
            for (PCConnection connection : pcs) {
                if (connection.getConnectContext().equals(channelHandlerContext)) {
                    pcConnection = connection;
                    break;
                }
            }
        }
        return pcConnection;
    }

    public void remove(PCConnection deviceConnection) {
        synchronized (locker) {
            pcs.remove(deviceConnection);
        }
    }

    public void add(String deviceName, ChannelHandlerContext context) {
        add(new PCConnection(deviceName, context));
    }

    public void add(PCConnection pcConnection) {
        synchronized (locker) {
            pcs.add(pcConnection);
        }
    }
}
