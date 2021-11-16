package cn.schoolwow.quickhttp.handler;

import cn.schoolwow.quickhttp.domain.LogLevel;
import cn.schoolwow.quickhttp.domain.MetaWrapper;
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
public class RequestLogHandler extends AbstractHandler{
    private static Logger logger = LoggerFactory.getLogger(RequestLogHandler.class);

    public RequestLogHandler(MetaWrapper metaWrapper) {
        super(metaWrapper);
    }

    @Override
    public Handler handle() throws IOException {
        if (!requestMeta.ignoreHttpErrors) {
            if (responseMeta.statusCode < 200 || responseMeta.statusCode >= 400) {
                logger.warn("[请求与响应]{}\n{}",requestMeta.statusLine, getRequestAndResponseLog());
                throw new IOException("http状态异常!状态码:" + responseMeta.statusCode + ",地址:" + requestMeta.url);
            }
        }
        log(LogLevel.DEBUG,"[请求与响应]{}\n{}",requestMeta.statusLine, getRequestAndResponseLog());
        return new RedirectHandler(metaWrapper);
    }

    /**
     * 获取请求和响应日志
     */
    private String getRequestAndResponseLog() throws IOException {
        StringBuilder contentBuilder = new StringBuilder("\n====================================================================\n");
        contentBuilder.append(requestMeta.statusLine + "\n");
        Set<Map.Entry<String, List<String>>> requestHeaderSet = requestMeta.headerMap.entrySet();
        for (Map.Entry<String, List<String>> entry : requestHeaderSet) {
            for(String value : entry.getValue()){
                contentBuilder.append(entry.getKey() + ": " + value + "\n");
            }
        }
        contentBuilder.append("\n" + requestMeta.bodyLog + "\n\n");

        contentBuilder.append(responseMeta.statusLine + "\n");
        Set<Map.Entry<String, List<String>>> responseHeaderSet = responseMeta.headerMap.entrySet();
        for (Map.Entry<String, List<String>> entry : responseHeaderSet) {
            for(String value : entry.getValue()){
                contentBuilder.append(entry.getKey() + ": " + value + "\n");
            }
        }
        if(null!=responseMeta.contentType){
            if (responseMeta.contentType.contains("application/json")
                    || responseMeta.contentType.contains("text/")
                    || responseMeta.contentType.contains("charset")
            ) {
                InputStream inputStream = response.bodyStream();
                if(null!=inputStream){
                    inputStream.mark(1024);
                    byte[] bytes = new byte[Math.min(1024,inputStream.available())];
                    inputStream.read(bytes);
                    inputStream.reset();
                    contentBuilder.append("\n" + new String(bytes, Charset.forName(responseMeta.charset)) + "......");
                }else{
                    contentBuilder.append("\n[响应内容无法获取]");
                }
            }
        } else {
            contentBuilder.append("\n[" + response.contentLength() + "]");
        }
        contentBuilder.append("\n====================================================================\n");
        return contentBuilder.toString();
    }
}