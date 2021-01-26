package cn.schoolwow.quickhttp.listener;

import cn.schoolwow.quickhttp.request.Request;
import cn.schoolwow.quickhttp.response.Response;

/**
 * http客户端事件监听
 */
public interface QuickHttpClientListener {
    /**
     * 在http请求发送之前
     *
     * @param request http请求信息
     * @return 是否继续请求
     */
    void beforeExecute(Request request);

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
