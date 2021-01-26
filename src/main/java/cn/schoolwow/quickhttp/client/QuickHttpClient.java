package cn.schoolwow.quickhttp.client;

import cn.schoolwow.quickhttp.request.Request;

import java.net.URL;

/**
 * http客户端接口
 */
public interface QuickHttpClient {
    /**
     * 客户端配置
     */
    QuickHttpClientConfig clientConfig();

    /**
     * 访问url
     *
     * @param url 请求地址
     */
    Request connect(String url);

    /**
     * 访问url
     *
     * @param url 请求地址
     */
    Request connect(URL url);
}
