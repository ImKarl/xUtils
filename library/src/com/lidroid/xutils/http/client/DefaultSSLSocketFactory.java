package com.lidroid.xutils.http.client;

import com.lidroid.xutils.util.LogUtils;
import org.apache.http.conn.ssl.SSLSocketFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.Socket;
import java.security.*;

/**
 * SSL协议连接工厂（信任所有证书）
 */
public class DefaultSSLSocketFactory extends SSLSocketFactory {

    private SSLContext sslContext = SSLContext.getInstance("TLS");

    private static KeyStore trustStore;

    static {
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
        } catch (Throwable e) {
            LogUtils.e(e.getMessage(), e);
        }
    }

    private static DefaultSSLSocketFactory instance;

    /**
     * 获取默认的SSL协议连接工厂单例
     * @return SSL协议连接工厂{@link com.lidroid.xutils.http.client.DefaultSSLSocketFactory}
     */
    public static DefaultSSLSocketFactory getSocketFactory() {
        if (instance == null) {
            try {
                instance = new DefaultSSLSocketFactory();
            } catch (Throwable e) {
                LogUtils.e(e.getMessage(), e);
            }
        }
        return instance;
    }

    private DefaultSSLSocketFactory()
            throws UnrecoverableKeyException,
            NoSuchAlgorithmException,
            KeyStoreException,
            KeyManagementException {
        super(trustStore);

        TrustManager trustAllCerts = new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {
            }
        };
        sslContext.init(null, new TrustManager[]{trustAllCerts}, null);

        this.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    }

    /**
     * 创建连接
     * @param socket {@link java.net.Socket}
     * @param host 主机HOST
     * @param port 端口
     * @param autoClose 是否自动关闭连接
     * @return {@link java.net.Socket}
     * @throws IOException IO网络通讯异常
     */
    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    /**
     * 创建连接
     * @return {@link java.net.Socket}
     * @throws IOException IO网络通讯异常
     */
    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }
    
}
