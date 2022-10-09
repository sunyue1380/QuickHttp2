package cn.schoolwow.quickhttp.handler;

import cn.schoolwow.quickhttp.domain.Client;
import cn.schoolwow.quickhttp.listener.QuickHttpClientListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * 请求分发处理
 * */
public class DispatcherHandler implements Handler{
    private static Logger logger = LoggerFactory.getLogger(DispatcherHandler.class);

    /**限制头部*/
    private static final String[] restrictedHeaders = {
            /* Restricted by XMLHttpRequest2 */
            //"Accept-Charset",
            //"Accept-Encoding",
            "Access-Control-Request-Headers",
            "Access-Control-Request-Method",
            "Connection", /* close is allowed */
            "Content-Length",
            //"Cookie",
            //"Cookie2",
            "Content-Transfer-Encoding",
            //"Date",
            "Expect",
            "Host",
            "Keep-Alive",
            "Origin",
            // "Referer",
            // "TE",
            "Trailer",
            "Transfer-Encoding",
            "Upgrade",
            //"User-Agent",
            "Via"
    };

    @Override
    public Handler handle(Client client) throws IOException {
        //请求执行前
        List<QuickHttpClientListener> quickHttpClientListenerList = client.clientConfig.quickHttpClientListenerList;
        for (QuickHttpClientListener quickHttpClientListener : quickHttpClientListenerList) {
            quickHttpClientListener.beforeExecute(client.request);
        }
        //判断是否需要打开限制头部
        checkRestrictedHeaders(client);
        //信息校验
        checkRequestMeta(client);
        //执行请求
        int retryTimes = 1;
        try {
            Handler handler = new RequestHandler();
            IOException e = null;
            while (retryTimes <= client.requestMeta.retryTimes) {
                try {
                    while(null!=handler){
                        handler = handler.handle(client);
                    }
                    break;
                } catch (SocketTimeoutException | ConnectException ex) {
                    e = ex;
                    logger.debug("链接超时,重试{}/{},地址:{}", retryTimes, client.requestMeta.retryTimes, client.requestMeta.url);
                    client.requestMeta.connectTimeoutMillis = client.requestMeta.connectTimeoutMillis*2;
                    client.requestMeta.readTimeoutMillis = client.requestMeta.readTimeoutMillis*2;
                    retryTimes++;
                }
            }
            if(retryTimes>client.requestMeta.retryTimes&&null!=e){
                throw e;
            }
            //请求执行成功
            for (QuickHttpClientListener quickHttpClientListener : quickHttpClientListenerList) {
                quickHttpClientListener.executeSuccess(client.request, client.response);
            }
        } catch (URISyntaxException e) {
            logger.error("uri语法错误", e);
        } catch (IOException e) {
            //请求执行失败
            for (QuickHttpClientListener quickHttpClientListener : quickHttpClientListenerList) {
                quickHttpClientListener.executeFail(client.request, e);
            }
            throw e;
        } finally {
            System.setProperty("sun.net.http.allowRestrictedHeaders", "false");
        }
        return null;
    }

    /**
     * 判断是否要打开限制头部
     * */
    private void checkRestrictedHeaders(Client client){
        for(String restrictedHeader:restrictedHeaders){
            if(null!=client.requestMeta.headerMap.get(restrictedHeader)){
                System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
                break;
            }
        }
    }

    /**
     * 检查请求数据是否有误
     */
    private void checkRequestMeta(Client client) {
        if (null == client.requestMeta.url) {
            logger.debug("url不能为空!");
            throw new IllegalArgumentException("url不能为空!");
        }
        String protocol = client.requestMeta.url.getProtocol();
        if (!protocol.startsWith("http")) {
            logger.debug("当前只支持http和https协议.当前url:{}", client.requestMeta.url);
            throw new IllegalArgumentException("当前只支持http和https协议.当前url:" + client.requestMeta.url);
        }
        if (null == client.requestMeta.proxy) {
            client.requestMeta.proxy = client.clientConfig.proxy;
        }
        if (3000 == client.requestMeta.connectTimeoutMillis) {
            client.requestMeta.connectTimeoutMillis = client.clientConfig.connectTimeoutMillis;
        }
        if (5000 == client.requestMeta.readTimeoutMillis) {
            client.requestMeta.readTimeoutMillis = client.clientConfig.readTimeoutMillis;
        }
        if (client.requestMeta.followRedirects) {
            client.requestMeta.followRedirects = client.clientConfig.followRedirects;
        }
        if (20 == client.requestMeta.maxFollowRedirectTimes) {
            client.requestMeta.maxFollowRedirectTimes = client.clientConfig.maxFollowRedirectTimes;
        }
        if (!client.requestMeta.ignoreHttpErrors) {
            client.requestMeta.ignoreHttpErrors = client.clientConfig.ignoreHttpErrors;
        }
        if (3 == client.requestMeta.retryTimes) {
            client.requestMeta.retryTimes = client.clientConfig.retryTimes;
        }
    }

}
