package cn.schoolwow.quickhttp.handler;

import java.io.IOException;

/**
 * 处理器接口定义
 */
public interface Handler {
    /**
     * 执行http请求
     *
     * @return 下一个处理器, 为null则直接返回
     */
    Handler handle() throws IOException;
}
