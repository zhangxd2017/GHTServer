package cn.ght.server.manager;

import cn.ght.protocol.bean.ModuleInfo;
import cn.ght.server.bean.ModuleConnection;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static ModuleManager instance = null;
    private static final Object locker = new Object();

    private List<ModuleConnection> modules;

    public static ModuleManager getInstance() {
        if (instance == null) {
            synchronized (locker) {
                if (instance == null) {
                    instance = new ModuleManager();
                }
            }
        }
        return instance;
    }

    private ModuleManager() {
        modules = new ArrayList<ModuleConnection>();
    }

    public boolean contains(String deviceName) {
        boolean contains = false;
        for (ModuleConnection connection : modules) {
            if (connection.getDeviceName().equals(deviceName)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    public void add(String deviceName, ChannelHandlerContext context) {
        add(new ModuleConnection(deviceName, context));
    }

    public void add(ModuleConnection moduleConnection) {
        synchronized (locker) {
            modules.add(moduleConnection);
        }
    }

    public ModuleConnection getByName(String deviceName) {
        ModuleConnection moduleConnection = null;
        synchronized (locker) {
            for (ModuleConnection connection : modules) {
                if (connection.getDeviceName().equals(deviceName)) {
                    moduleConnection = connection;
                    break;
                }
            }
        }
        return moduleConnection;
    }

    public ModuleConnection getByContext(ChannelHandlerContext context) {
        ModuleConnection moduleConnection = null;
        synchronized (locker) {
            for (ModuleConnection connection : modules) {
                if (connection.getConnectContext().equals(context)) {
                    moduleConnection = connection;
                    break;
                }
            }
        }
        return moduleConnection;
    }

    public void removeByName(String deviceName) {
        synchronized (locker) {
            ModuleConnection moduleConnection = null;
            for (ModuleConnection connection : modules) {
                if (connection.getDeviceName().equals(deviceName)) {
                    moduleConnection = connection;
                    break;
                }
            }
            if (moduleConnection != null) {
                modules.remove(moduleConnection);
            }
        }
    }

    public void remove(ModuleConnection moduleConnection) {
        synchronized (locker) {
            modules.remove(moduleConnection);
        }
    }

    public List<ModuleInfo> getAllModules() {
        List<ModuleInfo> lists = new ArrayList<>();
        synchronized (locker) {
            for (ModuleConnection module : modules) {
                ModuleInfo info = new ModuleInfo();
                info.setName(module.getDeviceName());
                info.setLongitude(module.getLocationInfo().getLongitude());
                info.setLatitude(module.getLocationInfo().getLatitude());
                lists.add(info);
            }
        }
        return lists;
    }
}
