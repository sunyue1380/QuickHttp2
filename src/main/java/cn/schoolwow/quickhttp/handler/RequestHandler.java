package cn.schoolwow.quickhttp.handler;

import cn.schoolwow.quickhttp.domain.ClientConfig;
import cn.schoolwow.quickhttp.domain.LogLevel;
import cn.schoolwow.quickhttp.domain.MetaWrapper;
import cn.schoolwow.quickhttp.domain.RequestMeta;
import cn.schoolwow.quickhttp.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RequestHandler extends AbstractHandler{
    private static Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private static ThreadLocal<StringBuilder> builderThreadLocal = new ThreadLocal<>();
    private static final char[] mimeBoundaryChars =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int boundaryLength = 32;

    public RequestHandler(MetaWrapper metaWrapper) {
        super(metaWrapper);
        StringBuilder builder = builderThreadLocal.get();
        if(null==builder){
            builderThreadLocal.set(new StringBuilder());
        }
    }

    @Override
    public Handler handle() throws IOException {
        HttpURLConnection httpURLConnection = createHttpUrlConnection(metaWrapper);
        metaWrapper.responseMeta.httpURLConnection = httpURLConnection;
        return new ResponseHandler(metaWrapper);
    }

    /**
     * 创建HttpUrlConnection对象
     */
    private HttpURLConnection createHttpUrlConnection(MetaWrapper metaWrapper) throws IOException {
        RequestMeta requestMeta = metaWrapper.requestMeta;
        ClientConfig clientConfig = metaWrapper.clientConfig;

        //添加路径请求参数
        if (!requestMeta.parameterMap.isEmpty()) {
            StringBuilder parameterBuilder = builderThreadLocal.get();
            parameterBuilder.setLength(0);
            Set<Map.Entry<String, String>> entrySet = requestMeta.parameterMap.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                String value = entry.getValue();
                if (null != value) {
                    value = URLEncoder.encode(value, requestMeta.charset);
                }
                parameterBuilder.append(URLEncoder.encode(entry.getKey(), requestMeta.charset) + "=" + value + "&");
            }
            parameterBuilder.deleteCharAt(parameterBuilder.length() - 1);
            if (requestMeta.url.toString().contains("?")) {
                parameterBuilder.insert(0, "&");
            } else {
                parameterBuilder.insert(0, "?");
            }
            requestMeta.url = new URL(requestMeta.url.toString() + parameterBuilder.toString());
        }
        URL u = requestMeta.url;
        final HttpURLConnection httpURLConnection = (HttpURLConnection) (
                requestMeta.proxy == null ? u.openConnection() : u.openConnection(requestMeta.proxy)
        );
        log(LogLevel.DEBUG,"[请求行]{} {},代理:{}", requestMeta.method.name(), u, requestMeta.proxy == null ? "无" : requestMeta.proxy.address());
        //判断是否https
        if (httpURLConnection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) httpURLConnection).setSSLSocketFactory(clientConfig.sslSocketFactory);
            ((HttpsURLConnection) httpURLConnection).setHostnameVerifier(clientConfig.hostnameVerifier);
        }
        httpURLConnection.setRequestMethod(requestMeta.method.name());
        httpURLConnection.setConnectTimeout(requestMeta.connectTimeoutMillis);
        httpURLConnection.setReadTimeout(requestMeta.readTimeoutMillis);
        httpURLConnection.setInstanceFollowRedirects(false);
        //设置头部
        {
            Set<Map.Entry<String, List<String>>> entrySet = requestMeta.headerMap.entrySet();
            for(Map.Entry<String,List<String>> entry:entrySet){
                for(String value:entry.getValue()){
                    httpURLConnection.addRequestProperty(entry.getKey(), value);
                }
            }
        }
        //设置Cookie
        try {
            Map<String,List<String>> cookieHeaderMap = clientConfig.cookieManager.get(requestMeta.url.toURI(), requestMeta.headerMap);
            if(cookieHeaderMap.containsKey("Cookie")){
                List<String> cookieList = cookieHeaderMap.get("Cookie");
                if(cookieList.size()>0){
                    StringBuilder builder = builderThreadLocal.get();
                    builder.setLength(0);
                    for(String cookie:cookieList){
                        builder.append(" "+cookie+";");
                    }
                    httpURLConnection.setRequestProperty("Cookie",builder.toString());
                    log(LogLevel.TRACE,"[设置Cookie头部]{}",builder.toString());
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //执行请求
        httpURLConnection.setDoInput(true);
        StringBuilder builder = builderThreadLocal.get();
        builder.setLength(0);
        if (requestMeta.method.hasBody() && (!requestMeta.dataFileMap.isEmpty() || null != requestMeta.requestBody || !requestMeta.dataMap.isEmpty())) {
            //优先级 dataFile > requestBody > dataMap
            if (Request.ContentType.MULTIPART_FORMDATA.equals(requestMeta.userContentType) || !requestMeta.dataFileMap.isEmpty()) {
                if (null == requestMeta.boundary) {
                    requestMeta.boundary = mimeBoundary();
                }
                httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + requestMeta.boundary);
                httpURLConnection.setChunkedStreamingMode(0);
                if(!requestMeta.dataFileMap.isEmpty()){
                    builder.append("[MultipartFile]" + requestMeta.dataFileMap);
                }
                if(!requestMeta.dataMap.isEmpty()){
                    builder.append("[Multipart]" + requestMeta.dataMap);
                }
            } else if (Request.ContentType.APPLICATION_JSON.equals(requestMeta.userContentType) || (requestMeta.requestBody != null && requestMeta.requestBody.length > 0)) {
                httpURLConnection.setRequestProperty("Content-Type", (requestMeta.userContentType==null?"application/json":requestMeta.userContentType) + "; charset=" + requestMeta.charset + ";");
                httpURLConnection.setFixedLengthStreamingMode(requestMeta.requestBody.length);
                builder.append(new String(requestMeta.requestBody,requestMeta.charset));
            } else if (Request.ContentType.APPLICATION_X_WWW_FORM_URLENCODED.equals(requestMeta.userContentType) || !requestMeta.dataMap.isEmpty()) {
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + requestMeta.charset);
                if (!requestMeta.dataMap.isEmpty()) {
                    StringBuilder formBuilder = new StringBuilder();
                    Set<Map.Entry<String, String>> entrySet = requestMeta.dataMap.entrySet();
                    for (Map.Entry<String, String> entry : entrySet) {
                        String value = entry.getValue();
                        if (null != value) {
                            value = URLEncoder.encode(value, requestMeta.charset);
                        }
                        formBuilder.append(URLEncoder.encode(entry.getKey(), requestMeta.charset) + "=" + value + "&");
                    }
                    formBuilder.deleteCharAt(formBuilder.length() - 1);
                    requestMeta.requestBody = formBuilder.toString().getBytes(Charset.forName(requestMeta.charset));
                }
                httpURLConnection.setFixedLengthStreamingMode(requestMeta.requestBody.length);
                builder.append("[Form]"+requestMeta.dataMap.toString());
            }
            if (null != requestMeta.contentType && !requestMeta.contentType.isEmpty()) {
                httpURLConnection.setRequestProperty("Content-Type", requestMeta.contentType);
            }
            //开始正式写入数据
            httpURLConnection.setDoOutput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            final BufferedWriter w = new BufferedWriter(new OutputStreamWriter(outputStream, requestMeta.charset));
            if (Request.ContentType.MULTIPART_FORMDATA.equals(requestMeta.userContentType) || !requestMeta.dataFileMap.isEmpty()) {
                if (!requestMeta.dataMap.isEmpty()) {
                    Set<Map.Entry<String, String>> entrySet = requestMeta.dataMap.entrySet();
                    for (Map.Entry<String, String> entry : entrySet) {
                        w.write("--" + requestMeta.boundary + "\r\n");
                        w.write("Content-Disposition: form-data; name=\"" + entry.getKey().replace("\"", "%22") + "\"\r\n");
                        w.write("\r\n");
                        w.write(entry.getValue());
                        w.write("\r\n");
                    }
                }
                Set<Map.Entry<String, Collection<Path>>> entrySet = requestMeta.dataFileMap.entrySet();
                for (Map.Entry<String, Collection<Path>> entry : entrySet) {
                    Collection<Path> pathCollection = entry.getValue();
                    for(Path file:pathCollection){
                        String name = entry.getKey().replace("\"", "%22");

                        w.write("--" + requestMeta.boundary + "\r\n");
                        w.write("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + file.getFileName().toString().replace("\"", "%22") + "\"\r\n");
                        w.write("Content-Type: " + Files.probeContentType(file) + "\r\n");
                        w.write("\r\n");
                        w.flush();
                        outputStream.write(Files.readAllBytes(file));
                        outputStream.flush();
                        w.write("\r\n");
                    }
                }
                w.write("--" + requestMeta.boundary + "--\r\n");
            } else if (Request.ContentType.APPLICATION_JSON.equals(requestMeta.userContentType) || requestMeta.requestBody != null && !requestMeta.requestBody.equals("")) {
                outputStream.write(requestMeta.requestBody);
            } else if (Request.ContentType.APPLICATION_X_WWW_FORM_URLENCODED.equals(requestMeta.userContentType) || !requestMeta.dataMap.isEmpty()) {
                if (null != requestMeta.requestBody) {
                    outputStream.write(requestMeta.requestBody);
                }
            }
            w.flush();
            w.close();
        }
        requestMeta.bodyLog = builder.toString();
        return httpURLConnection;
    }

    /**
     * 创建随机Boundary字符串作为分隔符
     */
    private static String mimeBoundary() {
        final StringBuilder mime = new StringBuilder(boundaryLength);
        final Random rand = new Random();
        for (int i = 0; i < boundaryLength; i++) {
            mime.append(mimeBoundaryChars[rand.nextInt(mimeBoundaryChars.length)]);
        }
        return mime.toString();
    }
}
