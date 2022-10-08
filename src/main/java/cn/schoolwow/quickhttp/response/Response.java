package cn.schoolwow.quickhttp.response;

import cn.schoolwow.quickhttp.document.Document;
import cn.schoolwow.quickhttp.document.DocumentParser;
import cn.schoolwow.quickhttp.domain.ResponseMeta;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface Response extends AutoCloseable{
    /**
     * 获取返回地址
     */
    String url();

    /**
     * 获取状态码
     */
    int statusCode();

    /**
     * 获取状态说明
     */
    String statusMessage();

    /**
     * 获取编码格式
     */
    String charset();

    /**
     * 获取返回格式类型
     */
    String contentType();

    /**
     * 获取大小
     */
    long contentLength();

    /**
     * 获取文件名
     */
    String filename();

    /**
     * 是否支持分段下载
     */
    boolean acceptRanges();

    /**
     * 获取contentEncoding头部信息,若不存在则返回null
     */
    String contentEncoding();

    /**
     * 是否有该Header
     * @param name 头部名称
     */
    boolean hasHeader(String name);

    /**
     * 是否存在该Header
     * @param name 头部名称
     * @param value 头部值
     */
    boolean hasHeader(String name, String value);

    /**
     * 获取头部信息
     * @param name 头部名称
     */
    List<String> header(String name);

    /**
     * 获取所有Header信息
     */
    Map<String, List<String>> headers();

    /**
     * 是否存在指定Cookie
     * @param name cookie名称
     * */
    boolean hasCookie(String name);

    /**
     * 是否存在指定Cookie
     * @param name cookie名称
     * @param value cookie值
     * */
    boolean hasCookie(String name, String value);

    /**
     * 设置最大下载速率(kb/s)
     */
    Response maxDownloadSpeed(int maxDownloadSpeed);

    /**
     * 返回字符串
     */
    String body() throws IOException;

    /**
     * 返回JSON对象
     */
    JSONObject bodyAsJSONObject() throws IOException;

    /**
     * 返回JSON数组
     */
    JSONArray bodyAsJSONArray() throws IOException;

    /**
     * 解析jsonp返回JSON对象
     */
    JSONObject jsonpAsJSONObject() throws IOException;

    /**
     * 解析jsonp返回JSON数组
     */
    JSONArray jsonpAsJSONArray() throws IOException;

    /**
     * 返回字节数组
     */
    byte[] bodyAsBytes() throws IOException;

    /**
     * 将输入流写入到指定文件
     * <p>若文件已存在,则会追加到文件尾部</p>
     */
    void bodyAsFile(String filePath) throws IOException;

    /**
     * 将输入流写入到指定文件
     * <p>若文件已存在,则会追加到文件尾部</p>
     */
    void bodyAsFile(Path file) throws IOException;

    /**
     * 获取输入流
     */
    InputStream bodyStream();

    /**
     * 解析成DOM数并返回Document对象
     */
    Document parse() throws IOException;

    /**
     * 使用事件监听机制获取处理DOM树
     */
    DocumentParser parser() throws IOException;

    /**
     * 关闭连接
     */
    void close();

    /**
     * 断开连接
     */
    void disconnect();

    /**
     * 获取返回元数据
     */
    ResponseMeta responseMeta();
}
