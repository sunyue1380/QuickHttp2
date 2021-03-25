package cn.schoolwow.quickhttp.handler;

import cn.schoolwow.quickhttp.domain.MetaWrapper;
import cn.schoolwow.quickhttp.request.Request;

import java.io.IOException;

/**重定向处理器*/
public class RedirectHandler extends AbstractHandler{

    public RedirectHandler(MetaWrapper metaWrapper) {
        super(metaWrapper);
    }

    @Override
    public Handler handle() throws IOException {
        //HttpUrlConnection无法处理从http到https的重定向或者https到http的重定向
        redirect();
        return null;
    }

    /**
     * 处理重定向
     */
    private void redirect() throws IOException {
        int followRedirectTimes = 0;
        String location = responseMeta.httpURLConnection.getHeaderField("Location");
        while (requestMeta.followRedirects && null != location) {
            if (followRedirectTimes >= requestMeta.maxFollowRedirectTimes) {
                throw new IOException("重定向次数过多!限制最大次数:" + requestMeta.maxFollowRedirectTimes);
            }
            //处理相对路径形式的重定向
            if (location.startsWith("http")) {
                request.url(location);
            } else if (location.startsWith("/")) {
                request.url(requestMeta.url.getProtocol() + "://" + requestMeta.url.getHost() + ":" + (requestMeta.url.getPort() == -1 ? requestMeta.url.getDefaultPort() : requestMeta.url.getPort()) + location);
            } else {
                String u = requestMeta.url.toString();
                request.url(u.substring(0, u.lastIndexOf("/")) + "/" + location);
            }
            //重定向时方法改为get方法,删除所有主体内容
            request.method(Request.Method.GET);
            requestMeta.dataFileMap.clear();
            requestMeta.dataMap.clear();
            requestMeta.requestBody = null;
            followRedirectTimes++;
            try {
                Handler handler = new DispatcherHandler(metaWrapper);
                while(null!=handler){
                    handler = handler.handle();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            location = responseMeta.httpURLConnection.getHeaderField("Location");
        }
    }
}
