package cn.schoolwow.quickhttp.request;

import cn.schoolwow.quickhttp.domain.RequestMeta;
import cn.schoolwow.quickhttp.listener.ResponseListener;
import cn.schoolwow.quickhttp.response.Response;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface Request extends Cloneable {
    enum UserAgent {
        CHROME("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36"), ANDROID("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Mobile Safari/537.36"), MAC("User-Agent, Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36");

        public final String userAgent;

        UserAgent(String userAgent) {
            this.userAgent = userAgent;
        }
    }

    enum ContentType {
        MULTIPART_FORMDATA,
        APPLICATION_JSON,
        APPLICATION_X_WWW_FORM_URLENCODED;
    }

    enum Method {
        GET(false), POST(true), PUT(true), DELETE(false), PATCH(true), HEAD(false), OPTIONS(false), TRACE(false);

        private final boolean hasBody;

        Method(boolean hasBody) {
            this.hasBody = hasBody;
        }

        public final boolean hasBody() {
            return hasBody;
        }
    }

    /***
     * 指定请求地址
     * @param url 请求地址
     */
    Request url(URL url);

    /***
     * 指定请求地址
     * @param url 请求地址
     */
    Request url(String url);

    /***
     * 设置请求方法
     * @param method 请求方法
     */
    Request method(String method);

    /***
     * 设置请求方法
     * @param method 请求方法
     */
    Request method(Method method);

    /**
     * 设置Basic Auth请求头部
     *
     * @param username 用户名
     * @param password 密码
     */
    Request basicAuth(String username, String password);

    /**
     * 指定编码格式
     *
     * @param charset 编码格式
     */
    Request charset(String charset);

    /**
     * 指定用户代理
     *
     * @param userAgent 用户代理
     */
    Request userAgent(String userAgent);

    /**
     * 指定用户代理
     *
     * @param userAgent 用户代理
     */
    Request userAgent(UserAgent userAgent);

    /**
     * 指定referrer
     *
     * @param referrer referrer头部
     */
    Request referrer(String referrer);

    /**
     * 指定Content-Type
     *
     * @param contentType Content-Type头部
     */
    Request contentType(String contentType);

    /**
     * 指定Content-Type
     *
     * @param contentType Content-Type头部
     */
    Request contentType(ContentType contentType);

    /***
     * 设置ajax请求头部
     */
    Request ajax();

    /**
     * 设置分段下载
     *
     * @param start 开始字节
     * @param end   结束字节(0表示获取剩下所有字节)
     */
    Request ranges(long start, long end);

    /**
     * 指定boundary
     *
     * @param boundary boundary
     */
    Request boundary(String boundary);

    /**
     * 设置头部字段信息
     *
     * @param name  头部字段名称
     * @param value 头部字段值
     */
    Request header(String name, String value);

    /**
     * 设置头部字段信息
     *
     * @param headerMap 头部字段信息
     */
    Request headers(Map<String, String> headerMap);

    /**
     * 设置Cookie头部
     *
     * @param name cookie名称
     * @param value cookie值
     */
    Request cookie(String name, String value);

    /**
     * 设置Cookie头部
     *
     * @param cookie cookie字符串
     */
    Request cookie(String cookie);

    /**
     * 设置Cookie头部
     *
     * @param httpCookie cookie信息
     */
    Request cookie(HttpCookie httpCookie);

    /**
     * 设置Cookie头部列表
     *
     * @param httpCookieList cookie列表
     */
    Request cookie(List<HttpCookie> httpCookieList);

    /**
     * 设置路径请求参数
     *
     * @param key   请求参数键
     * @param value 请求参数值
     */
    Request parameter(String key, String value);

    /**
     * 设置表单请求参数
     *
     * @param key   表单请求参数键
     * @param value 表单请求参数值
     */
    Request data(String key, String value);

    /**
     * 设置表单请求参数
     *
     * @param key  表单请求参数键
     * @param file 上传文件
     */
    Request data(String key, Path file);

    /**
     * 设置表单请求参数
     *
     * @param dataMap 表单请求
     */
    Request data(Map<String, String> dataMap);

    /**
     * 设置请求体内容
     *
     * @param body 请求体
     */
    Request requestBody(String body);

    /**
     * 设置请求体内容
     *
     * @param body 请求体
     */
    Request requestBody(JSONObject body);

    /**
     * 设置请求体内容
     *
     * @param body 请求体
     */
    Request requestBody(JSONArray body);

    /**
     * 设置请求体内容
     *
     * @param file 上传文件
     */
    Request requestBody(Path file) throws IOException;

    /***
     * 设置代理
     * @param proxy 代理对象
     */
    Request proxy(Proxy proxy);

    /***
     * 设置代理
     * @param host 代理地址
     * @param port 代理端口
     */
    Request proxy(String host, int port);

    /**
     * 设置连接超时时间(毫秒)
     *
     * @param connectTimeoutMillis 连接超时时间(毫秒),0表示不限制
     */
    Request connectTimeout(int connectTimeoutMillis);

    /**
     * 设置读取超时时间(毫秒)
     *
     * @param readTimeoutMillis 读取超时时间(毫秒),0表示不限制
     **/
    Request readTimeout(int readTimeoutMillis);

    /**
     * 是否自动重定向
     *
     * @param followRedirects 是否自动重新定定向.默认为true
     */
    Request followRedirects(boolean followRedirects);

    /**
     * 指定最大重定向次数
     *
     * @param maxFollowRedirectTimes 最大重定向次数
     */
    Request maxFollowRedirectTimes(int maxFollowRedirectTimes);

    /**
     * 是否忽略http错误(4xx和5xx响应码)
     *
     * @param ignoreHttpErrors 忽略http错误,默认为false
     */
    Request ignoreHttpErrors(boolean ignoreHttpErrors);

    /**
     * <b>请求超时</b>时重试次数
     *
     * @param retryTimes 重试次数,默认为3次
     */
    Request retryTimes(int retryTimes);

    /**
     * 执行请求
     */
    Response execute() throws IOException;

    /**
     * 异步执行请求
     *
     * @param responseListener 请求回调接口
     */
    void enqueue(ResponseListener responseListener);

    /**
     * clone方法
     */
    Request clone();

    RequestMeta requestMeta();
}
