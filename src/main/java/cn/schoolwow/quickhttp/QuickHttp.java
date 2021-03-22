package cn.schoolwow.quickhttp;

import cn.schoolwow.quickhttp.client.QuickHttpClient;
import cn.schoolwow.quickhttp.client.QuickHttpClientConfig;
import cn.schoolwow.quickhttp.client.QuickHttpClientImpl;
import cn.schoolwow.quickhttp.request.Request;

import java.net.CookieManager;
import java.net.URL;

public class QuickHttp {
    /**
     * 默认QuickHttp客户端
     */
    private static QuickHttpClient defaultQuickHttpClient = new QuickHttpClientImpl();

    static {
        CookieManager.setDefault(defaultQuickHttpClient.clientConfig().cookieOption().cookieManager());
    }

    /**
     * 客户端配置
     */
    public static QuickHttpClientConfig clientConfig() {
        return defaultQuickHttpClient.clientConfig();
    }

    /**
     * 访问url
     *
     * @param url 请求地址
     */
    public static Request connect(String url) {
        return defaultQuickHttpClient.connect(url);
    }

    /**
     * 访问url
     *
     * @param url 请求地址
     */
    public static Request connect(URL url) {
        return defaultQuickHttpClient.connect(url);
    }

    /**
     * 获取默认的http客户端
     */
    public static QuickHttpClient defaultQuickHttpClient() {
        return defaultQuickHttpClient;
    }

    /**
     * 创建http客户端
     */
    public static QuickHttpClient newQuickHttpClient() {
        return new QuickHttpClientImpl();
    }
}
