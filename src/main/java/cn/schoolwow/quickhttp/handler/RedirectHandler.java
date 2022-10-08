package cn.schoolwow.quickhttp.handler;

import cn.schoolwow.quickhttp.domain.Client;
import cn.schoolwow.quickhttp.domain.ResponseMeta;
import cn.schoolwow.quickhttp.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**重定向处理器*/
public class RedirectHandler implements Handler{
    private Logger logger = LoggerFactory.getLogger(RedirectHandler.class);

    @Override
    public Handler handle(Client client) throws IOException {
        redirect(client);
        return null;
    }

    /**
     * 处理重定向
     */
    private void redirect(Client client) throws IOException {
        //HttpUrlConnection无法处理从http到https的重定向或者https到http的重定向
        int followRedirectTimes = 0;
        String location = client.responseMeta.httpURLConnection.getHeaderField("Location");
        while (client.requestMeta.followRedirects && null != location) {
            if (followRedirectTimes >= client.requestMeta.maxFollowRedirectTimes) {
                logger.warn("重定向次数过多!限制最大次数:"+client.requestMeta.maxFollowRedirectTimes);
                throw new IOException("重定向次数过多!限制最大次数:" + client.requestMeta.maxFollowRedirectTimes);
            }
            logger.debug("执行重定向!地址:{}",location);
            resetResponseMeta(client.responseMeta);
            redirect(location, client);
            followRedirectTimes++;
            try {
                Handler handler = new DispatcherHandler();
                while(null!=handler){
                    handler = handler.handle(client);
                }
            }catch (Exception e){
                logger.error("重定向时发生异常", e);
            }
            location = client.responseMeta.httpURLConnection.getHeaderField("Location");
        }
    }

    /**
     * 执行重定向操作
     * @param location 重定向地址
     * */
    public void redirect(String location, Client client){
        //处理相对路径形式的重定向
        if (location.startsWith("http")) {
            client.request.url(location);
        } else if (location.startsWith("/")) {
            String url = client.requestMeta.url.getProtocol() + "://"
                    + client.requestMeta.url.getHost() + ":"
                    + (client.requestMeta.url.getPort() == -1 ? client.requestMeta.url.getDefaultPort() : client.requestMeta.url.getPort())
                    + location;
            client.request.url(url);
        } else {
            String u = client.requestMeta.url.toString();
            client.request.url(u.substring(0, u.lastIndexOf("/")) + "/" + location);
        }
        //重定向时方法改为get方法,删除所有主体内容
        client.requestMeta.statusLine = null;
        client.request.method(Request.Method.GET);
        client.requestMeta.statusLine = null;
        client.requestMeta.contentType = null;
        client.requestMeta.userContentType = null;
        client.requestMeta.boundary = null;
        client.requestMeta.parameterMap.clear();
        client.requestMeta.dataMap.clear();
        client.requestMeta.dataFileMap.clear();
        client.requestMeta.requestBody = null;
        client.requestMeta.bodyLog = null;
    }

    /**
     * 重置响应内容
     * */
    public void resetResponseMeta(ResponseMeta responseMeta){
        responseMeta.httpURLConnection = null;
        responseMeta.topHost = null;
        responseMeta.statusCode = 0;
        responseMeta.statusMessage = null;
        responseMeta.statusLine = null;
        responseMeta.charset = null;
        responseMeta.contentType = null;
        responseMeta.headerMap.clear();
        responseMeta.inputStream = null;
        responseMeta.body = null;
        responseMeta.document = null;
        responseMeta.documentParser = null;
    }
}
