package cn.ght.server;

import io.netty.util.internal.SystemPropertyUtil;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.*;

public final class GHTSSLContextFactory {


    private static final String PROTOCOL = "TLS";
    private static final SSLContext SERVER_CONTEXT;
    private static final SSLContext CLIENT_CONTEXT;

    private static String SERVER_KEY_STORE = "tls/ght.jks";
    private static String SERVER_TRUST_KEY_STORE = "tls/ght.jks";
    private static String SERVER_KEY_STORE_PASSWORD = "ght123";
    private static String SERVER_TRUST_KEY_STORE_PASSWORD = "ght123";

    private static String CLIENT_KEY_STORE = "tls/client.jks";
    private static String CLIENT_TRUST_KEY_STORE = "tls/client.jks";
    private static String CLIENT_KEY_STORE_PASSWORD = "ght123";
    private static String CLIENT_TRUST_KEY_STORE_PASSWORD = "ght123";

    static {
        String algorithm = SystemPropertyUtil.get("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        SSLContext serverContext;
        SSLContext clientContext;

        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            System.out.println(SERVER_KEY_STORE);
            ks.load(new FileInputStream(SERVER_KEY_STORE), SERVER_KEY_STORE_PASSWORD.toCharArray());
            KeyStore tks = KeyStore.getInstance("JKS");
            tks.load(new FileInputStream(SERVER_TRUST_KEY_STORE), SERVER_TRUST_KEY_STORE_PASSWORD.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            kmf.init(ks, SERVER_KEY_STORE_PASSWORD.toCharArray());
            tmf.init(tks);

            serverContext = SSLContext.getInstance(PROTOCOL);
            serverContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (Exception e) {
            throw new Error("创建服务端TLS上下文失败:", e);
        }

        try {
            KeyStore ks2 = KeyStore.getInstance("JKS");
            ks2.load(new FileInputStream(CLIENT_KEY_STORE), CLIENT_KEY_STORE_PASSWORD.toCharArray());

            KeyStore tks2 = KeyStore.getInstance("JKS");
            tks2.load(new FileInputStream(CLIENT_TRUST_KEY_STORE), CLIENT_TRUST_KEY_STORE_PASSWORD.toCharArray());
            // Set up key manager factory to use our key store
            KeyManagerFactory kmf2 = KeyManagerFactory.getInstance(algorithm);
            TrustManagerFactory tmf2 = TrustManagerFactory.getInstance("SunX509");
            kmf2.init(ks2, CLIENT_KEY_STORE_PASSWORD.toCharArray());
            tmf2.init(tks2);
            clientContext = SSLContext.getInstance(PROTOCOL);
            clientContext.init(kmf2.getKeyManagers(), tmf2.getTrustManagers(), null);
        } catch (Exception e) {
            throw new Error("Failed to initialize the client-side SSLContext", e);
        }

        SERVER_CONTEXT = serverContext;
        CLIENT_CONTEXT = clientContext;
    }

    public static SSLContext getServerContext() {
        return SERVER_CONTEXT;
    }


    public static SSLContext getClientContext() {
        return CLIENT_CONTEXT;
    }

}
