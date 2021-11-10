package cn.schoolwow.quickhttp.handler;

import cn.schoolwow.quickhttp.domain.LogLevel;
import cn.schoolwow.quickhttp.domain.MetaWrapper;
import cn.schoolwow.quickhttp.response.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * EventSource类型数据处理
 * */
public class EventSourceHandler extends AbstractHandler{
    private static Logger logger = LoggerFactory.getLogger(EventSourceHandler.class);

    public EventSourceHandler(MetaWrapper metaWrapper) {
        super(metaWrapper);
    }

    @Override
    public Handler handle() throws IOException {
        handleEventSource();
        return new RequestLogHandler(metaWrapper);
    }

    /**
     * 处理EventSource类型数据
     * */
    private void handleEventSource() throws IOException {
        if(null==responseMeta.contentType){
            return;
        }
        if(!responseMeta.contentType.toLowerCase().contains("text/event-stream")){
            return;
        }
        if(null==requestMeta.eventSourceConsumer){
            return;
        }
        log(LogLevel.TRACE,"[处理EventSource]Content-Type:{}",responseMeta.contentType);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(responseMeta.inputStream, StandardCharsets.UTF_8));
        EventSource eventSource = null;
        String line = bufferedReader.readLine();
        StringBuilder builder = new StringBuilder();
        while(null!=line){
            if(line.equals("")){
                requestMeta.eventSourceConsumer.accept(eventSource);
            }else{
                if(null==eventSource){
                    eventSource = new EventSource();
                }
                if(line.startsWith("id:")){
                    eventSource.id = Integer.parseInt(line.substring(line.indexOf(":")+1));
                }
                if(line.startsWith("event:")){
                    eventSource.event = line.substring(line.indexOf(":")+1);
                }
                if(line.startsWith("retry:")){
                    eventSource.retry = Integer.parseInt(line.substring(line.indexOf(":")+1));
                }
                if(line.startsWith("data:")){
                    eventSource.data = line.substring(line.indexOf(":")+1);
                }
            }
            builder.append(line+"\r\n");
            line = bufferedReader.readLine();
        }
        responseMeta.body = builder.toString().getBytes(StandardCharsets.UTF_8);
    }
}
