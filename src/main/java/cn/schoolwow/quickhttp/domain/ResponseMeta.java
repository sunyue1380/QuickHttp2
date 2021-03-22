package cn.schoolwow.quickhttp.domain;

import cn.schoolwow.quickhttp.document.Document;
import cn.schoolwow.quickhttp.document.DocumentParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 返回元数据
 */
public class ResponseMeta {
    /**
     * 连接对象
     */
    public HttpURLConnection httpURLConnection;

    /**
     * 顶级域
     */
    public String topHost;

    /**
     * 状态码
     */
    public int statusCode;

    /**
     * 消息
     */
    public String statusMessage;

    /**
     * 状态行
     */
    public String statusLine;

    /**
     * 编码格式
     */
    public String charset;

    /**
     * 响应体类型
     */
    public String contentType;

    /**
     * 头部信息
     */
    public Map<String, List<String>> headerMap = new LinkedHashMap<>();

    /**
     * 输入流
     */
    public InputStream inputStream;

    /**
     * 输入流字节数组
     */
    public byte[] body;

    /**
     * Document对象
     */
    public Document document;

    /**
     * Document对象
     */
    public DocumentParser documentParser;
}
