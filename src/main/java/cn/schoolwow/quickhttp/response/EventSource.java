package cn.schoolwow.quickhttp.response;

/**
 * 服务端推送技术EventSource对象
 * */
public class EventSource {
    /**事件id*/
    public long id;

    /**事件类型*/
    public String event;

    /**发送数据*/
    public String data;

    /**断开连接后重试等待时间(毫秒)*/
    public int retry = 10000;

    @Override
    public String toString() {
        return "id:"+id+"\nevent:"+event+"\nretry:"+retry+"\ndata:"+data;
    }
}
