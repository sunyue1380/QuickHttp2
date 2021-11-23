package cn.schoolwow.quickhttp.domain;

import cn.schoolwow.quickhttp.request.Request;
import cn.schoolwow.quickhttp.response.EventSource;

import java.io.Serializable;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

/**
 * 请求元数据
 */
public class RequestMeta implements Cloneable, Serializable {
    /**
     * 状态行
     */
    public transient String statusLine;

    /**
     * 访问地址
     */
    public transient URL url;

    /**
     * 请求方法
     */
    public transient Request.Method method = Request.Method.GET;

    /**
     * Http代理
     */
    public transient Proxy proxy;

    /**
     * 请求编码
     */
    public String charset = "utf-8";

    /**
     * 系统判断请求类型
     */
    public String contentType;

    /**
     * 用户指定请求类型
     */
    public transient Request.ContentType userContentType;

    /**
     * 用户指定请求类型
     */
    public transient Request.StreamingMode streamingMode = Request.StreamingMode.FixedLength;

    /**
     * 分隔符
     */
    public String boundary;

    /**
     * 头部信息
     */
    public Map<String, List<String>> headerMap = new LinkedHashMap<>();

    /**
     * parameter信息
     */
    public Map<String, String> parameterMap = new LinkedHashMap<>();

    /**
     * 表单信息
     */
    public Map<String, String> dataMap = new LinkedHashMap<>();

    /**
     * 表单信息
     */
    public Map<String, Collection<Path>> dataFileMap = new HashMap<>();

    /**
     * 自定义请求体
     */
    public transient byte[] requestBody = new byte[0];

    /**
     * 连接超时(毫秒)
     */
    public int connectTimeoutMillis = 3000;

    /**
     * 读取超时(毫秒)
     */
    public int readTimeoutMillis = 5000;

    /**
     * 是否自动重定向
     */
    public boolean followRedirects = true;

    /**
     * 最大重定向次数
     */
    public int maxFollowRedirectTimes = 20;

    /**
     * 是否忽略http状态异常
     */
    public boolean ignoreHttpErrors;

    /**
     * 超时重试次数
     */
    public int retryTimes = 3;

    /**
     * EventSource处理函数
     */
    public Consumer<EventSource> eventSourceConsumer;

    /**
     * 请求体内容,用于日志记录
     */
    public transient String bodyLog;

    /**
     * 日志文件路径
     */
    public transient String logFilePath;

    public RequestMeta() {
        headerMap.put("User-Agent", new ArrayList<>(Arrays.asList(Request.UserAgent.CHROME.userAgent)));
        headerMap.put("Accept-Encoding", new ArrayList<>(Arrays.asList("gzip, deflate")));
    }
}