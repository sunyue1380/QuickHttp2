package cn.schoolwow.quickhttp.domain;

import cn.schoolwow.quickhttp.request.Request;
import cn.schoolwow.quickhttp.response.Response;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 包裹元信息类
 * */
public class MetaWrapper {
    /**请求元信息*/
    public RequestMeta requestMeta;

    /**请求类*/
    public Request request;

    /**响应元信息*/
    public ResponseMeta responseMeta = new ResponseMeta();

    /**响应类*/
    public Response response;

    /**客户端配置信息*/
    public ClientConfig clientConfig;

    /**记录日志*/
    public PrintWriter pw;

    public MetaWrapper(RequestMeta requestMeta, Request request, ClientConfig clientConfig) {
        this.requestMeta = requestMeta;
        this.request = request;
        this.clientConfig = clientConfig;
        if(null==requestMeta.logFilePath&&null!=clientConfig.logDirectoryPath){
            requestMeta.logFilePath = clientConfig.logDirectoryPath + "/" +requestMeta.url.getHost() + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"))+".txt";
        }
        if(null!=requestMeta.logFilePath){
            File logFile = new File(requestMeta.logFilePath);
            logFile.getParentFile().mkdirs();
            try {
                pw = new PrintWriter(new FileWriter(logFile,true));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}