package cn.schoolwow.quickhttp.listener;

import cn.schoolwow.quickhttp.request.Request;
import cn.schoolwow.quickhttp.response.Response;

import java.io.IOException;

/***
 * 默认QuickHttp监听器实现类
 */
public class SimpleQuickHttpClientListener implements QuickHttpClientListener{
    @Override
    public void beforeExecute(Request request) throws IOException {

    }

    @Override
    public void executeSuccess(Request request, Response response) throws IOException{

    }

    @Override
    public void executeFail(Request request, Exception e) throws IOException{

    }
}
