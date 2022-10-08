package cn.schoolwow.quickhttp.handler;

import cn.schoolwow.quickhttp.domain.ClientConfig;
import cn.schoolwow.quickhttp.domain.MetaWrapper;
import cn.schoolwow.quickhttp.domain.RequestMeta;
import cn.schoolwow.quickhttp.domain.ResponseMeta;
import cn.schoolwow.quickhttp.request.Request;
import cn.schoolwow.quickhttp.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

}