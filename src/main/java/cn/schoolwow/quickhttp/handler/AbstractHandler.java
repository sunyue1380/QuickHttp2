package cn.schoolwow.quickhttp.handler;

import cn.schoolwow.quickhttp.domain.*;
import cn.schoolwow.quickhttp.request.Request;
import cn.schoolwow.quickhttp.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**处理类抽象类*/
public abstract class AbstractHandler implements Handler{
    private Logger logger = LoggerFactory.getLogger(AbstractHandler.class);

    /**请求元信息*/
    protected RequestMeta requestMeta;
    /**请求类*/
    protected Request request;
    /**响应元数据*/
    protected ResponseMeta responseMeta;
    /**响应信息*/
    protected Response response;
    /**客户端配置信息*/
    protected ClientConfig clientConfig;

    /**元信息包裹类*/
    protected MetaWrapper metaWrapper;

    public AbstractHandler(MetaWrapper metaWrapper) {
        this.requestMeta = metaWrapper.requestMeta;
        this.request = metaWrapper.request;
        this.responseMeta = metaWrapper.responseMeta;
        this.response = metaWrapper.response;
        this.clientConfig = metaWrapper.clientConfig;
        this.metaWrapper = metaWrapper;
    }

    protected void log(LogLevel logLevel, String message, Object... parameters){
        if(null!=metaWrapper.pw){
            StringBuilder builder = new StringBuilder(message);
            for(Object parameter:parameters){
                int startIndex = builder.indexOf("{");
                int endIndex = builder.indexOf("}");
                if(startIndex>0&&endIndex>0){
                    builder.replace(startIndex,endIndex+1,parameter.toString());
                }
            }
            metaWrapper.pw.append((logLevel.name().toString() + " " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")) + " " + builder.toString()+"\n"));
        }
        switch (logLevel){
            case TRACE:logger.trace(message,parameters);break;
            case DEBUG:logger.debug(message,parameters);break;
            case INFO:logger.info(message,parameters);break;
            case WARN:logger.warn(message,parameters);break;
            case ERROR:logger.error(message,parameters);break;
        }
    }
}