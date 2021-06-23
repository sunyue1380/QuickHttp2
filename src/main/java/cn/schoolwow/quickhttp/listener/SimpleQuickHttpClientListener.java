package cn.schoolwow.quickhttp.listener;

import cn.schoolwow.quickhttp.request.Request;
import cn.schoolwow.quickhttp.response.Response;

/***
 * 默认QuickHttp监听器实现类
 */
public class SimpleQuickHttpClientListener implements QuickHttpClientListener{
    @Override
    public void beforeExecute(Request request) {

    }

    @Override
    public void executeSuccess(Request request, Response response) {

    }

    @Override
    public void executeFail(Request request, Exception e) {

    }
}
