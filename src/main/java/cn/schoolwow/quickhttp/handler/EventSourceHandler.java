package cn.schoolwow.quickhttp.handler;

import cn.schoolwow.quickhttp.domain.Client;
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
public class EventSourceHandler implements Handler{
    private static Logger logger = LoggerFactory.getLogger(EventSourceHandler.class);

    @Override
    public Handler handle(Client client) throws IOException {
        handleEventSource(client);
        return new RequestLogHandler();
    }

    /**
     * 处理EventSource类型数据
     * */
    private void handleEventSource(Client client) throws IOException {
        if(null==client.responseMeta.contentType){
            return;
        }
        if(!client.responseMeta.contentType.toLowerCase().contains("text/event-stream")){
            return;
        }
        if(null==client.requestMeta.eventSourceConsumer){
            return;
        }
        logger.trace("处理EventSource, 当前Content-Type:{}", client.responseMeta.contentType);
        InputStreamReader inputStreamReader = new InputStreamReader(client.responseMeta.inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        EventSource eventSource = null;
        String line = bufferedReader.readLine();
        StringBuilder builder = new StringBuilder();
        while(null!=line){
            if(line.isEmpty()){
                client.requestMeta.eventSourceConsumer.accept(eventSource);
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
        client.responseMeta.body = builder.toString().getBytes(StandardCharsets.UTF_8);
    }
}
