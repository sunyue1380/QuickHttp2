package cn.schoolwow.quickhttp.handler;

import cn.schoolwow.quickhttp.domain.Client;
import cn.schoolwow.quickhttp.domain.ResponseMeta;
import cn.schoolwow.quickhttp.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RequestHandler implements Handler{
    private static Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private static ThreadLocal<StringBuilder> builderThreadLocal = new ThreadLocal<>();
    private static final char[] mimeBoundaryChars =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int boundaryLength = 32;

    public RequestHandler() {
        StringBuilder builder = builderThreadLocal.get();
        if(null==builder){
            builderThreadLocal.set(new StringBuilder());
        }
    }

    @Override
    public Handler handle(Client client) throws IOException, URISyntaxException {
        handleParameterMap(client);
        handleProxyAndHeader(client);
        handleRequestBody(client);
        return new ResponseHandler();
    }

    /**
     * 处理url表单参数
     * */
    private void handleParameterMap(Client client) throws UnsupportedEncodingException, MalformedURLException {
        if (client.requestMeta.parameterMap.isEmpty()) {
            return;
        }
        //添加路径请求参数
        StringBuilder parameterBuilder = builderThreadLocal.get();
        parameterBuilder.setLength(0);
        Set<Map.Entry<String, String>> entrySet = client.requestMeta.parameterMap.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            String value = entry.getValue();
            if (null != value) {
                value = URLEncoder.encode(value, client.requestMeta.charset);
            }
            parameterBuilder.append(URLEncoder.encode(entry.getKey(), client.requestMeta.charset) + "=" + value + "&");
        }
        parameterBuilder.deleteCharAt(parameterBuilder.length() - 1);
        if (client.requestMeta.url.toString().contains("?")) {
            parameterBuilder.insert(0, "&");
        } else {
            parameterBuilder.insert(0, "?");
        }
        client.requestMeta.url = new URL(client.requestMeta.url.toString() + parameterBuilder.toString());
    }

    /**
     * 处理代理和头部
     * */
    private void handleProxyAndHeader(Client client) throws IOException, URISyntaxException {
        URL u = client.requestMeta.url;
        client.responseMeta = new ResponseMeta();
        client.responseMeta.httpURLConnection = (HttpURLConnection) (
                client.requestMeta.proxy == null ? u.openConnection() : u.openConnection(client.requestMeta.proxy)
        );
        logger.debug("请求行:{} {}", client.requestMeta.method.name(), u);
        if(null!=client.requestMeta.proxy){
            logger.debug("使用代理:{}", client.requestMeta.proxy.address());
        }
        //判断是否https
        if (client.responseMeta.httpURLConnection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) client.responseMeta.httpURLConnection).setSSLSocketFactory(client.clientConfig.sslSocketFactory);
            ((HttpsURLConnection) client.responseMeta.httpURLConnection).setHostnameVerifier(client.clientConfig.hostnameVerifier);
        }
        client.responseMeta.httpURLConnection.setRequestMethod(client.requestMeta.method.name());
        client.responseMeta.httpURLConnection.setConnectTimeout(client.requestMeta.connectTimeoutMillis);
        client.responseMeta.httpURLConnection.setReadTimeout(client.requestMeta.readTimeoutMillis);
        client.responseMeta.httpURLConnection.setInstanceFollowRedirects(false);
        //设置头部
        Set<Map.Entry<String, List<String>>> entrySet = client.requestMeta.headerMap.entrySet();
        for(Map.Entry<String,List<String>> entry:entrySet){
            for(String value:entry.getValue()){
                logger.trace("添加头部 {} : {}", entry.getKey(), value);
                client.responseMeta.httpURLConnection.addRequestProperty(entry.getKey(), value);
            }
        }
        if (null != client.requestMeta.contentType) {
            logger.trace("设置Content-Type: {}", client.requestMeta.contentType);
            client.responseMeta.httpURLConnection.setRequestProperty("Content-Type", client.requestMeta.contentType);
        }
        //设置Cookie
        Map<String,List<String>> cookieHeaderMap = client.clientConfig.cookieManager.get(client.requestMeta.url.toURI(), client.requestMeta.headerMap);
        if(!cookieHeaderMap.containsKey("Cookie")){
            return;
        }
        List<String> cookieList = cookieHeaderMap.get("Cookie");
        if(null==cookieList||cookieList.isEmpty()){
            return;
        }
        StringBuilder builder = builderThreadLocal.get();
        builder.setLength(0);
        for(String cookie:cookieList){
            builder.append(" "+cookie+";");
        }
        logger.trace("设置Cookie头部:{}", builder.toString());
        client.responseMeta.httpURLConnection.setRequestProperty("Cookie", builder.toString());
    }

    /**
     * 写入请求体内容
     * */
    private void handleRequestBody(Client client) throws IOException {
        //执行请求
        client.responseMeta.httpURLConnection.setDoInput(true);
        StringBuilder builder = builderThreadLocal.get();
        builder.setLength(0);
        if (client.requestMeta.method.hasBody() && (!client.requestMeta.dataFileMap.isEmpty() || null != client.requestMeta.requestBody || !client.requestMeta.dataMap.isEmpty())) {
            //优先级 dataFile > requestBody > dataMap
            if (Request.ContentType.MULTIPART_FORMDATA.equals(client.requestMeta.userContentType) || !client.requestMeta.dataFileMap.isEmpty()) {
                if (null == client.requestMeta.boundary) {
                    client.requestMeta.boundary = mimeBoundary();
                }
                client.responseMeta.httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + client.requestMeta.boundary);
                if(!client.requestMeta.dataFileMap.isEmpty()){
                    builder.append("[MultipartFile]" + client.requestMeta.dataFileMap);
                }
                if(!client.requestMeta.dataMap.isEmpty()){
                    builder.append("[Multipart]" + client.requestMeta.dataMap);
                }
            } else if (Request.ContentType.APPLICATION_JSON.equals(client.requestMeta.userContentType) || (client.requestMeta.requestBody != null && client.requestMeta.requestBody.length > 0)) {
                if (null == client.requestMeta.contentType) {
                    client.responseMeta.httpURLConnection.setRequestProperty("Content-Type", (client.requestMeta.userContentType==null?"application/json":client.requestMeta.userContentType.value) + "; charset=" + client.requestMeta.charset);
                }
                builder.append(new String(client.requestMeta.requestBody,client.requestMeta.charset));
            } else if (Request.ContentType.APPLICATION_X_WWW_FORM_URLENCODED.equals(client.requestMeta.userContentType) || !client.requestMeta.dataMap.isEmpty()) {
                client.responseMeta.httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + client.requestMeta.charset);
                if (!client.requestMeta.dataMap.isEmpty()) {
                    StringBuilder formBuilder = new StringBuilder();
                    Set<Map.Entry<String, String>> entrySet = client.requestMeta.dataMap.entrySet();
                    for (Map.Entry<String, String> entry : entrySet) {
                        String value = entry.getValue();
                        if (null != value) {
                            value = URLEncoder.encode(value, client.requestMeta.charset);
                        }
                        formBuilder.append(URLEncoder.encode(entry.getKey(), client.requestMeta.charset) + "=" + value + "&");
                    }
                    formBuilder.deleteCharAt(formBuilder.length() - 1);
                    client.requestMeta.requestBody = formBuilder.toString().getBytes(Charset.forName(client.requestMeta.charset));
                }
                builder.append("[Form]"+client.requestMeta.dataMap.toString());
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final BufferedWriter w = new BufferedWriter(new OutputStreamWriter(baos, client.requestMeta.charset));
            if (Request.ContentType.MULTIPART_FORMDATA.equals(client.requestMeta.userContentType) || !client.requestMeta.dataFileMap.isEmpty()) {
                if (!client.requestMeta.dataMap.isEmpty()) {
                    Set<Map.Entry<String, String>> entrySet = client.requestMeta.dataMap.entrySet();
                    for (Map.Entry<String, String> entry : entrySet) {
                        w.write("--" + client.requestMeta.boundary + "\r\n");
                        w.write("Content-Disposition: form-data; name=\"" + entry.getKey().replace("\"", "%22") + "\"\r\n");
                        w.write("\r\n");
                        w.write(entry.getValue());
                        w.write("\r\n");
                    }
                }
                Set<Map.Entry<String, Collection<Path>>> entrySet = client.requestMeta.dataFileMap.entrySet();
                for (Map.Entry<String, Collection<Path>> entry : entrySet) {
                    Collection<Path> pathCollection = entry.getValue();
                    for(Path file:pathCollection){
                        String name = entry.getKey().replace("\"", "%22");

                        w.write("--" + client.requestMeta.boundary + "\r\n");
                        w.write("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + file.getFileName().toString().replace("\"", "%22") + "\"\r\n");
                        w.write("Content-Type: " + Files.probeContentType(file) + "\r\n");
                        w.write("\r\n");
                        w.flush();
                        baos.write(Files.readAllBytes(file));
                        baos.flush();
                        w.write("\r\n");
                    }
                }
                w.write("--" + client.requestMeta.boundary + "--\r\n");
            } else if (Request.ContentType.APPLICATION_JSON.equals(client.requestMeta.userContentType) || client.requestMeta.requestBody != null && !client.requestMeta.requestBody.equals("")) {
                baos.write(client.requestMeta.requestBody);
            } else if (Request.ContentType.APPLICATION_X_WWW_FORM_URLENCODED.equals(client.requestMeta.userContentType) || !client.requestMeta.dataMap.isEmpty()) {
                if (null != client.requestMeta.requestBody) {
                    baos.write(client.requestMeta.requestBody);
                }
            }
            w.flush();
            w.close();
            //开始正式写入数据
            switch (client.requestMeta.streamingMode){
                case FixedLength:{client.responseMeta.httpURLConnection.setFixedLengthStreamingMode(baos.size());};break;
                case Chunked:{client.responseMeta.httpURLConnection.setChunkedStreamingMode(0);};break;
            }
            client.responseMeta.httpURLConnection.setDoOutput(true);
            baos.writeTo(client.responseMeta.httpURLConnection.getOutputStream());
        }
        client.requestMeta.bodyLog = builder.toString();
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
