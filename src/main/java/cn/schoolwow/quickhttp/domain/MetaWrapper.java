package cn.schoolwow.quickhttp.domain;

import cn.schoolwow.quickhttp.request.Request;
import cn.schoolwow.quickhttp.response.Response;

/**
 * 包裹元信息类
 * */
public class MetaWrapper {
    /**请求元信息*/
    public RequestMeta requestMeta;

    /**请求类*/
    public Request request;

    /**响应元信息*/
    public ResponseMeta responseMeta = new ResponseMeta();

    /**响应类*/
    public Response response;

    /**客户端配置信息*/
    public ClientConfig clientConfig;

    public MetaWrapper(RequestMeta requestMeta, Request request, ClientConfig clientConfig) {
        this.requestMeta = requestMeta;
        this.request = request;
        this.clientConfig = clientConfig;
    }
}