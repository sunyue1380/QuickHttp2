package cn.schoolwow.quickhttp.handler;

import cn.schoolwow.quickhttp.domain.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 打印请求日志
 * */
public class RequestLogHandler implements Handler{
    private static Logger logger = LoggerFactory.getLogger(RequestLogHandler.class);

    @Override
    public Handler handle(Client client) throws IOException {
        if(client.requestMeta.ignoreHttpErrors&&client.responseMeta.statusCode>=400){
            logger.warn("http报文\n{}", getRequestAndResponseLog(client));
            throw new IOException("http状态码异常!状态码:"+client.responseMeta.statusCode+",地址:"+client.requestMeta.url);
        }
        logger.debug("http报文\n{}", getRequestAndResponseLog(client));
        return new RedirectHandler();
    }

    /**
     * 获取请求和响应日志
     */
    private String getRequestAndResponseLog(Client client) throws IOException {
        StringBuilder contentBuilder = new StringBuilder("\n====================================================================\n");
        contentBuilder.append(client.requestMeta.statusLine + "\n");
        Set<Map.Entry<String, List<String>>> requestHeaderSet = client.requestMeta.headerMap.entrySet();
        for (Map.Entry<String, List<String>> entry : requestHeaderSet) {
            for(String value : entry.getValue()){
                contentBuilder.append(entry.getKey() + ": " + value + "\n");
            }
        }
        contentBuilder.append("\n" + client.requestMeta.bodyLog + "\n\n");

        contentBuilder.append(client.responseMeta.statusLine + "\n");
        Set<Map.Entry<String, List<String>>> responseHeaderSet = client.responseMeta.headerMap.entrySet();
        for (Map.Entry<String, List<String>> entry : responseHeaderSet) {
            for(String value : entry.getValue()){
                contentBuilder.append(entry.getKey() + ": " + value + "\n");
            }
        }
        if(null==client.responseMeta.contentType){
            contentBuilder.append("\n[" + client.response.contentLength() + "]");
        }else if(client.responseMeta.contentType.contains("application/json")
                || client.responseMeta.contentType.contains("text/")
                || client.responseMeta.contentType.contains("charset")
        ){
            InputStream inputStream = client.response.bodyStream();
            if(null==inputStream||client.response.contentLength()<0){
                contentBuilder.append("\n[响应内容无法获取]");
            }else if(client.response.contentLength()==0){
                contentBuilder.append("\n[响应内容为空]");
            }else{
                int length = (int) Math.min(1024, client.response.contentLength());
                inputStream.mark(length);
                byte[] bytes = new byte[length];
                length = inputStream.read(bytes,0,bytes.length);
                if(length>0){
                    contentBuilder.append("\n" + new String(bytes,0,length, Charset.forName(client.responseMeta.charset)) + "......");
                }else{
                    contentBuilder.append("\n[响应内容为空]");
                }
                inputStream.reset();
            }
        }
        contentBuilder.append("\n====================================================================\n");
        return contentBuilder.toString();
    }
}