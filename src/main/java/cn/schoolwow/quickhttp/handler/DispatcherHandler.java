package cn.schoolwow.quickhttp.handler;

import cn.schoolwow.quickhttp.domain.LogLevel;
import cn.schoolwow.quickhttp.domain.MetaWrapper;
import cn.schoolwow.quickhttp.domain.QuickHttpConfig;
import cn.schoolwow.quickhttp.listener.QuickHttpClientListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * 请求分发处理
 * */
public class DispatcherHandler extends AbstractHandler{
    private static Logger logger = LoggerFactory.getLogger(DispatcherHandler.class);

    public DispatcherHandler(MetaWrapper metaWrapper) {
        super(metaWrapper);
    }

    @Override
    public Handler handle() throws IOException {
        //信息校验
        checkRequestMeta();
        //请求执行前
        List<QuickHttpClientListener> quickHttpClientListenerList = clientConfig.quickHttpClientListenerList;
        for (QuickHttpClientListener quickHttpClientListener : quickHttpClientListenerList) {
            quickHttpClientListener.beforeExecute(metaWrapper.request);
        }
        //执行请求
        int retryTimes = 1;
        try {
            while (retryTimes <= clientConfig.retryTimes) {
                try {
                    Handler handler = new RequestHandler(metaWrapper);
                    while(null!=handler){
                        handler = handler.handle();
                    }
                    break;
                } catch (SocketTimeoutException | ConnectException e) {
                    log(LogLevel.WARN,"[链接超时]重试{}/{},原因:{},地址:{}", retryTimes, clientConfig.retryTimes, e.getMessage(), requestMeta.url);
                    requestMeta.connectTimeoutMillis = requestMeta.connectTimeoutMillis*2;
                    requestMeta.readTimeoutMillis = requestMeta.readTimeoutMillis*2;
                    retryTimes++;
                }
            }
            //请求执行成功
            for (QuickHttpClientListener quickHttpClientListener : quickHttpClientListenerList) {
                quickHttpClientListener.executeSuccess(metaWrapper.request, metaWrapper.response);
            }
        } catch (Exception e) {
            //请求执行失败
            for (QuickHttpClientListener quickHttpClientListener : quickHttpClientListenerList) {
                quickHttpClientListener.executeFail(metaWrapper.request, e);
            }
            throw e;
        }finally {
            System.setProperty("sun.net.http.allowRestrictedHeaders", "false");
        }
        return null;
    }

    /**
     * 检查请求数据是否有误
     */
    private void checkRequestMeta() {
        log(LogLevel.DEBUG,"检查请求元数据!");
        if (null == requestMeta.url) {
            log(LogLevel.DEBUG,"url不能为空!");
            throw new IllegalArgumentException("url不能为空!");
        }
        String protocol = requestMeta.url.getProtocol();
        if (!protocol.startsWith("http")) {
            log(LogLevel.DEBUG,"当前只支持http和https协议.当前url:" + requestMeta.url);
            throw new IllegalArgumentException("当前只支持http和https协议.当前url:" + requestMeta.url);
        }
        if (null == requestMeta.proxy) {
            requestMeta.proxy = clientConfig.proxy;
        }
        if (null == requestMeta.proxy) {
            requestMeta.proxy = QuickHttpConfig.proxy;
        }

        if (3000 == requestMeta.connectTimeoutMillis) {
            requestMeta.connectTimeoutMillis = clientConfig.connectTimeoutMillis;
        }
        if (5000 == requestMeta.readTimeoutMillis) {
            requestMeta.readTimeoutMillis = clientConfig.readTimeoutMillis;
        }
        if (requestMeta.followRedirects) {
            requestMeta.followRedirects = clientConfig.followRedirects;
        }
        if (20 == requestMeta.maxFollowRedirectTimes) {
            requestMeta.maxFollowRedirectTimes = clientConfig.maxFollowRedirectTimes;
        }
        if (!requestMeta.ignoreHttpErrors) {
            requestMeta.ignoreHttpErrors = clientConfig.ignoreHttpErrors;
        }
        if (3 == requestMeta.retryTimes) {
            requestMeta.retryTimes = clientConfig.retryTimes;
        }
    }
}
