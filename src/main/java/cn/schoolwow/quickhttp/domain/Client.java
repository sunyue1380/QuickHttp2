package cn.schoolwow.quickhttp.domain;

import cn.schoolwow.quickhttp.request.Request;
import cn.schoolwow.quickhttp.response.Response;

public class Client {
    /**请求元信息*/
    public RequestMeta requestMeta;

    /**请求类*/
    public Request request;

    /**响应元数据*/
    public ResponseMeta responseMeta;

    /**响应信息*/
    public Response response;

    /**客户端配置信息*/
    public ClientConfig clientConfig;
}
