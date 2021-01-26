package cn.schoolwow.quickhttp.domain;

import cn.schoolwow.quickhttp.client.CookieOption;
import cn.schoolwow.quickhttp.client.CookieOptionImpl;
import cn.schoolwow.quickhttp.listener.QuickHttpClientListener;

import javax.net.ssl.*;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.Proxy;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 客户端配置项
 */
public class ClientConfig {
    /**
     * http代理
     */
    public transient Proxy proxy;

    /**
     * 连接超时(毫秒)
     */
    public int connectTimeoutMillis;

    /**
     * 读取超时(毫秒)
     */
    public int readTimeoutMillis;

    /**
     * 是否自动重定向
     */
    public boolean followRedirects = true;

    /**
     * 最大重定向次数
     */
    public int maxFollowRedirectTimes = 20;

    /**
     * 是否忽略http状态异常
     */
    public boolean ignoreHttpErrors;

    /**
     * 超时重试次数
     */
    public int retryTimes = 3;

    /**
     * hostnameVerifier
     */
    public transient HostnameVerifier hostnameVerifier = (s, sslSession) -> true;
    ;

    /**
     * sslSocketFactory
     */
    public transient SSLSocketFactory sslSocketFactory;

    {
        try {
            SSLContext sslcontext = SSLContext.getInstance("TLSv1.2");
            sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate certificates[], String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] ax509certificate, String s) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }}, new java.security.SecureRandom());
            sslSocketFactory = sslcontext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * origin信息
     */
    public String origin;

    /**
     * 事件监听列表
     */
    public List<QuickHttpClientListener> quickHttpClientListenerList = new ArrayList<>();

    /**
     * Cookie管理
     */
    public transient CookieManager cookieManager = new CookieManager();

    {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    /**
     * Cookie管理
     */
    public transient CookieOption cookieOption = new CookieOptionImpl(cookieManager);

    /**
     * 异步请求线程池配置
     */
    public ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
}
