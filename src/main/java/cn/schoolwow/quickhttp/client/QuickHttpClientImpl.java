package cn.schoolwow.quickhttp.client;

import cn.schoolwow.quickhttp.request.Request;
import cn.schoolwow.quickhttp.request.RequestImpl;

import java.net.URL;

/**
 * http客户端
 */
public class QuickHttpClientImpl implements QuickHttpClient {
    private QuickHttpClientConfigImpl quickHttpClientConfig = new QuickHttpClientConfigImpl();

    @Override
    public QuickHttpClientConfig clientConfig() {
        return quickHttpClientConfig;
    }

    @Override
    public Request connect(String url) {
        return new RequestImpl(quickHttpClientConfig.clientConfig).url(url);
    }

    @Override
    public Request connect(URL url) {
        return new RequestImpl(quickHttpClientConfig.clientConfig).url(url);
    }
}
