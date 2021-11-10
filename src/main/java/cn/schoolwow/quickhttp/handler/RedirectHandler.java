package cn.schoolwow.quickhttp.handler;

import cn.schoolwow.quickhttp.domain.LogLevel;
import cn.schoolwow.quickhttp.domain.MetaWrapper;
import cn.schoolwow.quickhttp.request.RequestImpl;

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
                log(LogLevel.WARN,"重定向次数过多!限制最大次数:"+requestMeta.maxFollowRedirectTimes);
                throw new IOException("重定向次数过多!限制最大次数:" + requestMeta.maxFollowRedirectTimes);
            }
            log(LogLevel.DEBUG,"执行重定向!地址:{}",location);
            RequestImpl requestImpl = (RequestImpl) request;
            requestImpl.redirect(location);
            responseMeta.reset();
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
