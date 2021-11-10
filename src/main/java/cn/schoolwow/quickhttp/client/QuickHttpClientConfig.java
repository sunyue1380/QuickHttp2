package cn.schoolwow.quickhttp.client;

import cn.schoolwow.quickhttp.listener.QuickHttpClientListener;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.net.Proxy;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * http客户端配置信息接口
 */
public interface QuickHttpClientConfig {
    /***
     * 设置代理
     * @param proxy 代理对象
     */
    QuickHttpClientConfig proxy(Proxy proxy);

    /***
     * 设置代理
     * @param host 代理地址
     * @param port 代理端口
     */
    QuickHttpClientConfig proxy(String host, int port);

    /**
     * 设置连接超时时间(毫秒)
     *
     * @param connectTimeoutMillis 连接超时时间(毫秒),0表示不限制
     */
    QuickHttpClientConfig connectTimeout(int connectTimeoutMillis);

    /**
     * 设置读取超时时间(毫秒)
     *
     * @param readTimeoutMillis 读取超时时间(毫秒),0表示不限制
     **/
    QuickHttpClientConfig readTimeout(int readTimeoutMillis);

    /**
     * 是否自动重定向
     *
     * @param followRedirects 是否自动重新定定向.默认为true
     */
    QuickHttpClientConfig followRedirects(boolean followRedirects);

    /**
     * 指定最大重定向次数
     *
     * @param maxFollowRedirectTimes 最大重定向次数
     */
    QuickHttpClientConfig maxFollowRedirectTimes(int maxFollowRedirectTimes);

    /**
     * 是否忽略http错误(4xx和5xx响应码)
     *
     * @param ignoreHttpErrors 忽略http错误,默认为false
     */
    QuickHttpClientConfig ignoreHttpErrors(boolean ignoreHttpErrors);

    /**
     * <b>请求超时</b>时重试次数
     *
     * @param retryTimes 重试次数,默认为3次
     */
    QuickHttpClientConfig retryTimes(int retryTimes);

    /**
     * 设置HostnameVerifier
     *
     * @param hostnameVerifier sslSocketFactory
     */
    QuickHttpClientConfig hostnameVerifier(HostnameVerifier hostnameVerifier);

    /**
     * 设置SSLSocketFactory
     *
     * @param sslSocketFactory sslSocketFactory
     */
    QuickHttpClientConfig sslSocketFactory(SSLSocketFactory sslSocketFactory);

    /**
     * 设置异步执行时的线程池
     *
     * @param threadPoolExecutor 异步执行时的线程池
     */
    QuickHttpClientConfig threadPoolExecutor(ThreadPoolExecutor threadPoolExecutor);

    /**
     * 设置origin,执行http请求时会拼接到url前面
     *
     * @param origin origin信息
     */
    QuickHttpClientConfig origin(String origin);

    /**
     * 添加事件监听器
     *
     * @param quickHttpClientListener 事件监听对象
     */
    QuickHttpClientConfig quickHttpClientListener(QuickHttpClientListener quickHttpClientListener);

    /**
     * 指定请求日志文件夹
     *
     * @param logDirectoryPath 日志文件夹路径
     */
    QuickHttpClientConfig logDirectoryPath(String logDirectoryPath);

    /**
     * Cookie管理
     */
    CookieOption cookieOption();
}
