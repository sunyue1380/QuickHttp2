package cn.schoolwow.quickhttp.request;

import cn.schoolwow.quickhttp.document.Document;
import cn.schoolwow.quickhttp.document.element.Element;
import cn.schoolwow.quickhttp.document.element.Elements;
import cn.schoolwow.quickhttp.domain.ClientConfig;
import cn.schoolwow.quickhttp.domain.QuickHttpConfig;
import cn.schoolwow.quickhttp.domain.RequestMeta;
import cn.schoolwow.quickhttp.domain.ResponseMeta;
import cn.schoolwow.quickhttp.listener.QuickHttpClientListener;
import cn.schoolwow.quickhttp.response.Response;
import cn.schoolwow.quickhttp.response.ResponseImpl;
import cn.schoolwow.quickhttp.response.SpeedLimitInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.www.MessageHeader;
import sun.net.www.protocol.https.DelegateHttpsURLConnection;
import sun.net.www.protocol.https.HttpsURLConnectionImpl;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * 执行http请求并返回结果
 */
public class RequestExecutor {
    private Logger logger = LoggerFactory.getLogger(RequestExecutor.class);
    private static ThreadLocal<StringBuilder> builderThreadLocal = new ThreadLocal<>();
    private static final char[] mimeBoundaryChars =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int boundaryLength = 32;

    private Request request;
    private RequestMeta requestMeta;
    private ClientConfig clientConfig;
    private Response response;
    private ResponseMeta responseMeta;

    public RequestExecutor(Request request, ClientConfig clientConfig) {
        this.request = request;
        this.requestMeta = request.requestMeta();
        this.clientConfig = clientConfig;
        StringBuilder builder = new StringBuilder();
        builderThreadLocal.set(builder);
    }

    /**
     * 执行请求
     */
    public Response execute() {
        //信息校验
        checkRequestMeta();
        //请求执行前
        List<QuickHttpClientListener> quickHttpClientListenerList = clientConfig.quickHttpClientListenerList;
        for (QuickHttpClientListener quickHttpClientListener : quickHttpClientListenerList) {
            quickHttpClientListener.beforeExecute(request);
        }
        //执行请求
        try {
            int retryTimes = 1;
            while (retryTimes <= clientConfig.retryTimes) {
                try {
                    HttpURLConnection httpURLConnection = createHttpUrlConnection();
                    response = getResponse(httpURLConnection);
                    break;
                } catch (SocketTimeoutException | ConnectException e) {
                    logger.warn("[链接超时]重试{}/{},原因:{},地址:{}", retryTimes, clientConfig.retryTimes, e.getMessage(), requestMeta.url);
                    requestMeta.connectTimeoutMillis = requestMeta.connectTimeoutMillis*2;
                    requestMeta.readTimeoutMillis = requestMeta.readTimeoutMillis*2;
                    retryTimes++;
                }
            }
            if(null!=response){
                handleRedirect(request, response);
                //请求执行成功
                for (QuickHttpClientListener quickHttpClientListener : quickHttpClientListenerList) {
                    quickHttpClientListener.executeSuccess(request, response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            //请求执行失败
            for (QuickHttpClientListener quickHttpClientListener : quickHttpClientListenerList) {
                quickHttpClientListener.executeFail(request, e);
            }
        }
        return response;
    }

    /**
     * 处理重定向
     */
    private Response handleRedirect(Request request, Response response) throws IOException {
        int followRedirectTimes = 0;
        //HttpUrlConnection无法处理从http到https的重定向或者https到http的重定向
        String location = responseMeta.httpURLConnection.getHeaderField("Location");
        while (requestMeta.followRedirects && null != location) {
            if (followRedirectTimes >= requestMeta.maxFollowRedirectTimes) {
                throw new IOException("重定向次数过多!限制最大次数:" + requestMeta.maxFollowRedirectTimes);
            }
            //处理相对路径形式的重定向
            if (location.startsWith("http")) {
                request.url(location);
            } else if (location.startsWith("/")) {
                request.url(requestMeta.url.getProtocol() + "://" + requestMeta.url.getHost() + ":" + (requestMeta.url.getPort() == -1 ? requestMeta.url.getDefaultPort() : requestMeta.url.getPort()) + location);
            } else {
                String u = requestMeta.url.toString();
                request.url(u.substring(0, u.lastIndexOf("/")) + "/" + location);
            }
            //重定向时方法改为get方法,删除所有主体内容
            request.method(Request.Method.GET);
            requestMeta.dataFileMap.clear();
            requestMeta.dataMap.clear();
            requestMeta.requestBody = null;
            followRedirectTimes++;
            response = execute();
            location = responseMeta.httpURLConnection.getHeaderField("Location");
        }
        return response;
    }

    /**
     * 获取响应对象
     */
    private Response getResponse(HttpURLConnection httpURLConnection) throws IOException {
        responseMeta = new ResponseMeta();
        responseMeta.httpURLConnection = httpURLConnection;
        try {
            responseMeta.statusCode = httpURLConnection.getResponseCode();
            responseMeta.statusMessage = httpURLConnection.getResponseMessage();
        } catch (FileNotFoundException e) {
            responseMeta.statusCode = 404;
            responseMeta.statusMessage = "Not Found";
        }
        if (null == responseMeta.statusMessage) {
            responseMeta.statusMessage = "";
        }
        //获取顶级域
        responseMeta.topHost = httpURLConnection.getURL().getHost();
        String substring = responseMeta.topHost.substring(0,responseMeta.topHost.lastIndexOf("."));
        if(substring.contains(".")){
            responseMeta.topHost = responseMeta.topHost.substring(substring.lastIndexOf(".")+1);
        }
        //提取请求头信息
        MessageHeader requestsMessageHeader = null;
        MessageHeader responsesMessageHeader = null;
        try {
            Field requestsMessageHeaderField = sun.net.www.protocol.http.HttpURLConnection.class.getDeclaredField("requests");
            requestsMessageHeaderField.setAccessible(true);
            Field responsesMessageHeaderField = sun.net.www.protocol.http.HttpURLConnection.class.getDeclaredField("responses");
            responsesMessageHeaderField.setAccessible(true);
            if(httpURLConnection instanceof HttpsURLConnection){
                Field delegateField = HttpsURLConnectionImpl.class.getDeclaredField("delegate");
                delegateField.setAccessible(true);
                DelegateHttpsURLConnection delegateHttpsURLConnection = (DelegateHttpsURLConnection) delegateField.get(httpURLConnection);
                requestsMessageHeader = (MessageHeader) requestsMessageHeaderField.get(delegateHttpsURLConnection);
                responsesMessageHeader = (MessageHeader) responsesMessageHeaderField.get(delegateHttpsURLConnection);
            }else {
                requestsMessageHeader = (MessageHeader) requestsMessageHeaderField.get(httpURLConnection);
                responsesMessageHeader = (MessageHeader) responsesMessageHeaderField.get(httpURLConnection);
            }
            //添加请求头部
            {
                Map<String,List<String>> headerMap = requestsMessageHeader.getHeaders();
                Set<Map.Entry<String,List<String>>> entrySet = headerMap.entrySet();
                requestMeta.headerMap.clear();
                for(Map.Entry<String,List<String>> entry:entrySet){
                    if(null==entry.getValue().get(0)){
                        requestMeta.statusLine = entry.getKey();
                        continue;
                    }
                    requestMeta.headerMap.put(entry.getKey(),entry.getValue());
                }
            }
            //添加响应头部
            {
                Map<String,List<String>> headerMap = responsesMessageHeader.getHeaders();
                Set<Map.Entry<String,List<String>>> entrySet = headerMap.entrySet();
                for(Map.Entry<String,List<String>> entry:entrySet){
                    if(null==entry.getKey()){
                        responseMeta.statusLine = entry.getValue().get(0);
                        continue;
                    }
                    List<String> values = entry.getValue();
                    List<String> newValues = new ArrayList<>(values.size());
                    for(int i=0;i<values.size();i++){
                        if(null==values.get(i)){
                            continue;
                        }
                        newValues.add(new String(values.get(i).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
                    }
                    responseMeta.headerMap.put(entry.getKey(),newValues);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //提取body信息
        {
            try {
                String contentEncoding = httpURLConnection.getContentEncoding();
                InputStream inputStream = httpURLConnection.getErrorStream() != null ? httpURLConnection.getErrorStream() : httpURLConnection.getInputStream();
                if (contentEncoding != null && !contentEncoding.isEmpty()) {
                    if (contentEncoding.equals("gzip")) {
                        inputStream = new GZIPInputStream(inputStream);
                    } else if (contentEncoding.equals("deflate")) {
                        inputStream = new InflaterInputStream(inputStream, new Inflater(true));
                    }
                }
                responseMeta.inputStream = inputStream;
                responseMeta.inputStream = new BufferedInputStream(inputStream);
                responseMeta.inputStream = new SpeedLimitInputStream(responseMeta.inputStream);
            } catch (FileNotFoundException e) {
                logger.warn("[读取输入流失败]");
            }
        }
        getCharset();
        try {
            URI uri = httpURLConnection.getURL().toURI();
            clientConfig.cookieManager.put(uri, httpURLConnection.getHeaderFields());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Response response = new ResponseImpl(requestMeta, responseMeta,clientConfig);
        if (!requestMeta.ignoreHttpErrors) {
            if (responseMeta.statusCode < 200 || responseMeta.statusCode >= 400) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[请求与响应]\n{}", getRequestAndResponseLog(requestMeta, response));
                }
                throw new IOException("http状态异常!状态码:" + responseMeta.statusCode + ",地址:" + requestMeta.url);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[请求与响应]\n{}", getRequestAndResponseLog(requestMeta, response));
        }
        return response;
    }

    /**
     * 提取编码信息
     */
    private void getCharset() throws IOException {
        getCharsetFromContentType(responseMeta.httpURLConnection.getContentType());
        if (responseMeta.charset == null) {
            byte[] bytes = new byte[1024 * 5];
            responseMeta.inputStream.mark(bytes.length);
            responseMeta.inputStream.read(bytes, 0, bytes.length);
            boolean readFully = (responseMeta.inputStream.read() == -1);
            responseMeta.inputStream.reset();
            ByteBuffer firstBytes = ByteBuffer.wrap(bytes);
            getCharsetFromBOM(firstBytes);
            if (responseMeta.charset == null) {
                getCharsetFromMeta(firstBytes, readFully);
            }
        }
        if (responseMeta.charset == null) {
            responseMeta.charset = "utf-8";
        }
    }

    /**
     * 从meta标签里面获取编码信息
     */
    private void getCharsetFromMeta(ByteBuffer byteBuffer, boolean readFully) {
        String docData = StandardCharsets.UTF_8.decode(byteBuffer).toString();
        //判断是否是HTML或者XML文档
        if (!docData.startsWith("<?xml") && !docData.startsWith("<!DOCTYPE")) {
            return;
        }
        Document doc = Document.parse(docData);
        if (doc.root() == null) {
            //不是HTML文档
            return;
        }
        Elements metaElements = doc.select("meta[http-equiv=content-type], meta[charset]");
        for (Element meta : metaElements) {
            if (meta.hasAttr("http-equiv")) {
                getCharsetFromContentType(meta.attr("content"));
            }
            if (responseMeta.charset == null && meta.hasAttr("charset")) {
                responseMeta.charset = meta.attr("charset");
            }
            break;
        }

        if (responseMeta.charset == null) {
            Element root = doc.root();
            if (doc.root().tagName().equals("?xml") && root.hasAttr("encoding")) {
                responseMeta.charset = root.attr("encoding");
            }
        }
        if (readFully) {
            responseMeta.document = doc;
        }
    }

    /**
     * 从BOM里面获取编码信息
     */
    private void getCharsetFromBOM(ByteBuffer byteBuffer) throws IOException {
        final Buffer buffer = byteBuffer;
        buffer.mark();
        byte[] bom = new byte[4];
        if (byteBuffer.remaining() >= bom.length) {
            byteBuffer.get(bom);
            buffer.rewind();
        }
        if (bom[0] == 0x00 && bom[1] == 0x00 && bom[2] == (byte) 0xFE && bom[3] == (byte) 0xFF ||
                bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE && bom[2] == 0x00 && bom[3] == 0x00) {
            responseMeta.charset = "utf-32";
        } else if (bom[0] == (byte) 0xFE && bom[1] == (byte) 0xFF ||
                bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE) {
            responseMeta.charset = "utf-16";
        } else if (bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
            responseMeta.charset = "utf-8";
        }
        if (responseMeta.charset != null) {
            responseMeta.inputStream.skip(1);
        }
    }

    /**
     * 从Content-Type头部获取编码信息
     */
    private void getCharsetFromContentType(String contentType) {
        String prefix = "charset=";
        if (contentType != null && contentType.contains(prefix)) {
            int startIndex = contentType.indexOf(prefix);
            if (startIndex >= 0) {
                int endIndex = contentType.lastIndexOf(";");
                if (endIndex > startIndex) {
                    responseMeta.charset = contentType.substring(startIndex + prefix.length(), endIndex).trim();
                } else if (endIndex < startIndex) {
                    responseMeta.charset = contentType.substring(startIndex + prefix.length()).trim();
                }
            }
        }
    }

    /**
     * 创建连接对象
     */
    private HttpURLConnection createHttpUrlConnection() throws IOException {
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
        logger.info("[请求行]{} {},代理:{}", requestMeta.method.name(), u, requestMeta.proxy == null ? "无" : requestMeta.proxy.address());
        //判断是否https
        if (httpURLConnection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) httpURLConnection).setSSLSocketFactory(clientConfig.sslSocketFactory);
            ((HttpsURLConnection) httpURLConnection).setHostnameVerifier(clientConfig.hostnameVerifier);
        }
        httpURLConnection.setRequestMethod(requestMeta.method.name());
        httpURLConnection.setConnectTimeout(requestMeta.connectTimeoutMillis);
        httpURLConnection.setReadTimeout(requestMeta.readTimeoutMillis);
        httpURLConnection.setInstanceFollowRedirects(false);
        try {
            Map<String,List<String>> cookieHeaderMap = clientConfig.cookieManager.get(requestMeta.url.toURI(), requestMeta.headerMap);
            logger.trace("[设置Cookie头部]{}",cookieHeaderMap);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //设置头部
        {
            Set<Map.Entry<String, List<String>>> entrySet = requestMeta.headerMap.entrySet();
            for(Map.Entry<String,List<String>> entry:entrySet){
                for(String value:entry.getValue()){
                    httpURLConnection.addRequestProperty(entry.getKey(), value);
                }
            }
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
                if(Request.ContentType.APPLICATION_JSON.equals(requestMeta.userContentType)){
                    builder.append(new String(requestMeta.requestBody,requestMeta.charset));
                }
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
            if (null != requestMeta.contentType && requestMeta.contentType.isEmpty()) {
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
                Set<Map.Entry<String, Path>> entrySet = requestMeta.dataFileMap.entrySet();
                for (Map.Entry<String, Path> entry : entrySet) {
                    Path file = entry.getValue();
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
     * 检查请求数据是否有误
     */
    private void checkRequestMeta() {
        if (null == requestMeta.url) {
            throw new IllegalArgumentException("url不能为空!");
        }
        String protocol = requestMeta.url.getProtocol();
        if (!protocol.startsWith("http")) {
            throw new IllegalArgumentException("当前只支持http和https协议.当前url:" + requestMeta.url);
        }
        if (null == requestMeta.proxy) {
            requestMeta.proxy = clientConfig.proxy;
        }
        if (null == requestMeta.proxy) {
            requestMeta.proxy = QuickHttpConfig.proxy;
        }

        if (3000 == requestMeta.connectTimeoutMillis) {
            requestMeta.connectTimeoutMillis = clientConfig.connectTimeoutMillis;
        }
        if (5000 == requestMeta.readTimeoutMillis) {
            requestMeta.readTimeoutMillis = clientConfig.readTimeoutMillis;
        }
        if (requestMeta.followRedirects) {
            requestMeta.followRedirects = clientConfig.followRedirects;
        }
        if (20 == requestMeta.maxFollowRedirectTimes) {
            requestMeta.maxFollowRedirectTimes = clientConfig.maxFollowRedirectTimes;
        }
        if (!requestMeta.ignoreHttpErrors) {
            requestMeta.ignoreHttpErrors = clientConfig.ignoreHttpErrors;
        }
        if (3 == requestMeta.retryTimes) {
            requestMeta.retryTimes = clientConfig.retryTimes;
        }
    }

    /**
     * 获取请求和响应日志
     *
     * @param requestMeta 请求元数据
     * @param response 响应元数据
     */
    private String getRequestAndResponseLog(RequestMeta requestMeta, Response response) throws IOException {
        StringBuilder contentBuilder = new StringBuilder(requestMeta.statusLine + "\n");
        Set<Map.Entry<String, List<String>>> requestHeaderSet = requestMeta.headerMap.entrySet();
        for (Map.Entry<String, List<String>> entry : requestHeaderSet) {
            for(String value : entry.getValue()){
                contentBuilder.append(entry.getKey() + ": " + value + "\n");
            }
        }
        contentBuilder.append("\n" + requestMeta.bodyLog + "\n\n");

        contentBuilder.append(responseMeta.statusLine + "\n");
        Set<Map.Entry<String, List<String>>> responseHeaderSet = responseMeta.headerMap.entrySet();
        for (Map.Entry<String, List<String>> entry : responseHeaderSet) {
            for(String value : entry.getValue()){
                contentBuilder.append(entry.getKey() + ": " + value + "\n");
            }
        }
        if(null!=requestMeta.contentType){
            if (requestMeta.contentType.contains("application/json")
                    || requestMeta.contentType.contains("text/")
                    || requestMeta.contentType.contains("charset")
            ) {
                contentBuilder.append("\n" + response.body());
            }
        } else {
            contentBuilder.append("\n[" + response.contentLength() + "]");
        }
        contentBuilder.append("\n====================================================================");
        return contentBuilder.toString();
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
