package cn.schoolwow.quickhttp.domain;

import cn.schoolwow.quickhttp.request.Request;

import java.io.Serializable;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 请求元数据
 */
public class RequestMeta implements Cloneable, Serializable {
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
     * 分隔符
     */
    public String boundary;

    /**
     * 头部信息
     */
    public Map<String, String> headers = new LinkedHashMap<>();

    /**
     * parameter信息
     */
    public Map<String, String> parameters = new LinkedHashMap<>();

    /**
     * 表单信息
     */
    public Map<String, String> dataMap = new LinkedHashMap<>();

    /**
     * 表单信息
     */
    public Map<String, Path> dataFileMap = new IdentityHashMap<>();

    /**
     * 自定义请求体
     */
    public transient byte[] requestBody = new byte[0];

    /**
     * 连接超时(毫秒)
     */
    public int connectTimeoutMillis;

    /**
     * 读取超时(毫秒)
     */
    public int readTimeoutMillis;

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

    public RequestMeta() {
        headers.put("User-Agent", Request.UserAgent.CHROME.userAgent);
        headers.put("Accept-Encoding", "gzip, deflate");
    }
}
