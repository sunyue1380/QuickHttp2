package cn.schoolwow.quickhttp.listener;

import cn.schoolwow.quickhttp.request.Request;
import cn.schoolwow.quickhttp.response.Response;

import java.io.IOException;

/**
 * http异步调用回调接口
 */
public class SimpleResponseListener implements ResponseListener{
    @Override
    public void executeSuccess(Request request, Response response) throws IOException {

    }

    @Override
    public void executeFail(Request request, Exception e) throws IOException{

    }
}
