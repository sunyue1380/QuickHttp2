package cn.schoolwow.quickhttp.client;

import cn.schoolwow.quickhttp.domain.ClientConfig;
import cn.schoolwow.quickhttp.listener.QuickHttpClientListener;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.ThreadPoolExecutor;

public class QuickHttpClientConfigImpl implements QuickHttpClientConfig {
    public ClientConfig clientConfig = new ClientConfig();

    @Override
    public QuickHttpClientConfig proxy(Proxy proxy) {
        clientConfig.proxy = proxy;
        return this;
    }

    @Override
    public QuickHttpClientConfig proxy(String host, int port) {
        clientConfig.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        return this;
    }

    @Override
    public QuickHttpClientConfig connectTimeout(int connectTimeoutMillis) {
        clientConfig.connectTimeoutMillis = connectTimeoutMillis;
        return this;
    }

    @Override
    public QuickHttpClientConfig readTimeout(int readTimeoutMillis) {
        clientConfig.readTimeoutMillis = readTimeoutMillis;
        return this;
    }

    @Override
    public QuickHttpClientConfig followRedirects(boolean followRedirects) {
        clientConfig.followRedirects = followRedirects;
        return this;
    }

    @Override
    public QuickHttpClientConfig maxFollowRedirectTimes(int maxFollowRedirectTimes) {
        clientConfig.maxFollowRedirectTimes = maxFollowRedirectTimes;
        return this;
    }

    @Override
    public QuickHttpClientConfig ignoreHttpErrors(boolean ignoreHttpErrors) {
        clientConfig.ignoreHttpErrors = ignoreHttpErrors;
        return this;
    }

    @Override
    public QuickHttpClientConfig retryTimes(int retryTimes) {
        clientConfig.retryTimes = retryTimes;
        return this;
    }

    @Override
    public QuickHttpClientConfig hostnameVerifier(HostnameVerifier hostnameVerifier) {
        clientConfig.hostnameVerifier = hostnameVerifier;
        return this;
    }

    @Override
    public QuickHttpClientConfig sslSocketFactory(SSLSocketFactory sslSocketFactory) {
        clientConfig.sslSocketFactory = sslSocketFactory;
        return this;
    }

    @Override
    public QuickHttpClientConfig threadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
        clientConfig.threadPoolExecutor = threadPoolExecutor;
        return this;
    }

    @Override
    public QuickHttpClientConfig origin(String origin) {
        clientConfig.origin = origin;
        return this;
    }

    @Override
    public QuickHttpClientConfig quickHttpClientListener(QuickHttpClientListener QuickHttpClientConfigListener) {
        clientConfig.quickHttpClientListenerList.add(QuickHttpClientConfigListener);
        return this;
    }

    @Override
    public QuickHttpClientConfig logDirectoryPath(String logDirectoryPath) {
        clientConfig.logDirectoryPath = logDirectoryPath;
        return this;
    }

    @Override
    public CookieOption cookieOption() {
        return clientConfig.cookieOption;
    }
}
