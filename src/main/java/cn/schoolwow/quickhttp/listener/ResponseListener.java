package cn.schoolwow.quickhttp.listener;

import cn.schoolwow.quickhttp.request.Request;
import cn.schoolwow.quickhttp.response.Response;

/**
 * http异步调用回调接口
 */
public interface ResponseListener {
    /**
     * http请求成功以后
     *
     * @param request  请求信息
     * @param response 响应信息
     */
    void executeSuccess(Request request, Response response);

    /**
     * http请求成功以后
     *
     * @param request 请求信息
     * @param e       异常信息
     */
    void executeFail(Request request, Exception e);
}
