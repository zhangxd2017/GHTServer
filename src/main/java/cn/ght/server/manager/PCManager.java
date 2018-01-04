package cn.ght.server.manager;

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
}
