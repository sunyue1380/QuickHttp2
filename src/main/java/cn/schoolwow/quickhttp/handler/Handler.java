package cn.schoolwow.quickhttp.handler;

import cn.schoolwow.quickhttp.domain.Client;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * 处理器接口定义
 */
public interface Handler {
    /**
     * 执行http请求
     * @param client 请求响应信息
     *
     * @return 下一个处理器, 为null则直接返回
     */
    Handler handle(Client client) throws IOException, URISyntaxException;
}
