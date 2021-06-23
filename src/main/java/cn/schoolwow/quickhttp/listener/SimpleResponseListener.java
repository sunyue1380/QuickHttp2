package cn.schoolwow.quickhttp.listener;

import cn.schoolwow.quickhttp.request.Request;
import cn.schoolwow.quickhttp.response.Response;

/**
 * http异步调用回调接口
 */
public class SimpleResponseListener implements ResponseListener{
    @Override
    public void executeSuccess(Request request, Response response) {

    }

    @Override
    public void executeFail(Request request, Exception e) {

    }
}
