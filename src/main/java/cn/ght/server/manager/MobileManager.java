package cn.ght.server.manager;

public class MobileManager {

    private static MobileManager instance = null;
    private static Object locker = new Object();

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
    }
}
