package cn.schoolwow.quickhttp.request;

import cn.schoolwow.quickhttp.domain.ClientConfig;
import cn.schoolwow.quickhttp.domain.MetaWrapper;
import cn.schoolwow.quickhttp.domain.RequestMeta;
import cn.schoolwow.quickhttp.handler.DispatcherHandler;
import cn.schoolwow.quickhttp.listener.ResponseListener;
import cn.schoolwow.quickhttp.response.EventSource;
import cn.schoolwow.quickhttp.response.Response;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

public class RequestImpl implements Request {
    private ClientConfig clientConfig;
    private RequestMeta requestMeta = new RequestMeta();

    public RequestImpl(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    @Override
    public Request url(URL url) {
        requestMeta.url = url;
        return this;
    }

    @Override
    public Request url(String url) {
        try {
            if (null != clientConfig.origin && !url.startsWith("http")) {
                requestMeta.url = new URL(clientConfig.origin + url);
            } else {
                requestMeta.url = new URL(url);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public Request method(String method) {
        for (Method methodEnum : Method.values()) {
            if (methodEnum.name().equalsIgnoreCase(method)) {
                requestMeta.method = methodEnum;
                return this;
            }
        }
        throw new IllegalArgumentException("不支持的请求方法!" + method);
    }

    @Override
    public Request method(Method method) {
        requestMeta.method = method;
        return this;
    }

    @Override
    public Request basicAuth(String username, String password) {
        String encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(Charset.forName(requestMeta.charset)));
        requestMeta.headerMap.put("Authorization", new ArrayList<>(Arrays.asList("Basic " + encoded)));
        return this;
    }

    @Override
    public Request charset(String charset) {
        requestMeta.charset = charset;
        return this;
    }

    @Override
    public Request userAgent(String userAgent) {
        requestMeta.headerMap.put("User-Agent", new ArrayList<>(Arrays.asList(userAgent)));
        return this;
    }

    @Override
    public Request userAgent(UserAgent userAgent) {
        requestMeta.headerMap.put("User-Agent", new ArrayList<>(Arrays.asList(userAgent.userAgent)));
        return this;
    }

    @Override
    public Request referrer(String referrer) {
        requestMeta.headerMap.put("Referer", new ArrayList<>(Arrays.asList(referrer)));
        return this;
    }

    @Override
    public Request contentType(String contentType) {
        requestMeta.contentType = contentType;
        return this;
    }

    @Override
    public Request contentType(ContentType contentType) {
        requestMeta.userContentType = contentType;
        return this;
    }

    @Override
    public Request ajax() {
        URL url = requestMeta.url;
        return setHeader("X-Requested-With", "XMLHttpRequest")
                .setHeader("Origin", url.getProtocol() + "://" + url.getHost());
    }

    @Override
    public Request ranges(long start, long end) {
        return setHeader("Range", "bytes=" + start + "-" + (end > 0 ? end : ""));
    }

    @Override
    public Request boundary(String boundary) {
        requestMeta.boundary = boundary;
        return this;
    }

    @Override
    public Request acceptEncoding(boolean acceptEncoding) {
        if(!acceptEncoding){
            requestMeta.headerMap.remove("Accept-Encoding");
        }
        return this;
    }

    @Override
    public Request addHeader(String name, String value) {
        if(!requestMeta.headerMap.containsKey(name)){
            requestMeta.headerMap.put(name,new ArrayList<>());
        }
        requestMeta.headerMap.get(name).add(value);
        return this;
    }

    @Override
    public Request setHeader(String name, String value) {
        requestMeta.headerMap.put(name,new ArrayList<>(Arrays.asList(value)));
        return this;
    }

    @Override
    public Request headers(Map<String, List<String>> headerMap) {
        requestMeta.headerMap.putAll(headerMap);
        return this;
    }

    @Override
    public Request cookie(String name, String value) {
        HttpCookie httpCookie = new HttpCookie(name,value);
        httpCookie.setMaxAge(3600000);
        httpCookie.setPath("/");
        cookie(httpCookie);
        clientConfig.cookieOption.addCookie(httpCookie);
        return this;
    }

    @Override
    public Request cookie(String cookie) {
        clientConfig.cookieOption.addCookieString(requestMeta.url.getHost(),cookie);
        return this;
    }

    @Override
    public Request cookie(HttpCookie httpCookie) {
        if(null==httpCookie.getDomain()||httpCookie.getDomain().isEmpty()){
            httpCookie.setDomain(requestMeta.url.getHost());
        }
        httpCookie.setVersion(0);
        httpCookie.setDiscard(false);
        clientConfig.cookieOption.addCookie(httpCookie);
        return this;
    }

    @Override
    public Request cookie(List<HttpCookie> httpCookieList) {
        for(HttpCookie httpCookie:httpCookieList){
            cookie(httpCookie);
        }
        return null;
    }

    @Override
    public Request parameter(String key, String value) {
        requestMeta.parameterMap.put(key, value);
        return this;
    }

    @Override
    public Request parameter(Map<String,String> parameterMap) {
        requestMeta.parameterMap.putAll(parameterMap);
        return this;
    }

    @Override
    public Request data(String key, String value) {
        requestMeta.dataMap.put(key, value);
        return this;
    }

    @Override
    public Request data(String key, Path... files) {
        if(!requestMeta.dataFileMap.containsKey(key)){
            requestMeta.dataFileMap.put(key,new ArrayList());
        }
        requestMeta.dataFileMap.get(key).addAll(Arrays.asList(files));
        return this;
    }

    @Override
    public Request data(Map<String, String> dataMap) {
        requestMeta.dataMap.putAll(dataMap);
        return this;
    }

    @Override
    public Request requestBody(String body) {
        requestMeta.requestBody = body.getBytes(Charset.forName(requestMeta.charset));
        return this;
    }

    @Override
    public Request requestBody(JSONObject body) {
        requestMeta.requestBody = body.toJSONString().getBytes(Charset.forName(requestMeta.charset));
        return this;
    }

    @Override
    public Request requestBody(JSONArray body) {
        requestMeta.requestBody = body.toJSONString().getBytes(Charset.forName(requestMeta.charset));
        return this;
    }

    @Override
    public Request requestBody(Path file) throws IOException {
        requestMeta.requestBody = Files.readAllBytes(file);
        if(null==requestMeta.contentType){
            requestMeta.contentType = Files.probeContentType(file);
        }
        return this;
    }

    @Override
    public Request proxy(Proxy proxy) {
        requestMeta.proxy = proxy;
        return this;
    }

    @Override
    public Request proxy(String host, int port) {
        requestMeta.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        return this;
    }

    @Override
    public Request connectTimeout(int connectTimeoutMillis) {
        requestMeta.connectTimeoutMillis = connectTimeoutMillis;
        return this;
    }

    @Override
    public Request readTimeout(int readTimeoutMillis) {
        requestMeta.readTimeoutMillis = readTimeoutMillis;
        return this;
    }

    @Override
    public Request followRedirects(boolean followRedirects) {
        requestMeta.followRedirects = followRedirects;
        return this;
    }

    @Override
    public Request maxFollowRedirectTimes(int maxFollowRedirectTimes) {
        requestMeta.maxFollowRedirectTimes = maxFollowRedirectTimes;
        return this;
    }

    @Override
    public Request ignoreHttpErrors(boolean ignoreHttpErrors) {
        requestMeta.ignoreHttpErrors = ignoreHttpErrors;
        return this;
    }

    @Override
    public Request retryTimes(int retryTimes) {
        requestMeta.retryTimes = retryTimes;
        return this;
    }

    @Override
    public Request onEventSource(Consumer<EventSource> eventSourceConsumer) {
        requestMeta.eventSourceConsumer = eventSourceConsumer;
        return this;
    }

    @Override
    public Request logFilePath(String logFilePath) {
        requestMeta.logFilePath = logFilePath;
        return this;
    }

    @Override
    public Response execute() {
        MetaWrapper metaWrapper = new MetaWrapper(requestMeta,this,clientConfig);
        try {
            new DispatcherHandler(metaWrapper).handle();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            if(null!=metaWrapper.pw){
                metaWrapper.pw.flush();
                metaWrapper.pw.close();
            }
        }
        return metaWrapper.response;
    }

    @Override
    public void enqueue(ResponseListener responseListener) {
        if (null == clientConfig.threadPoolExecutor) {
            synchronized (Request.class) {
                if (null == clientConfig.threadPoolExecutor) {
                    clientConfig.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
                }
            }
        }
        clientConfig.threadPoolExecutor.execute(() -> {
            Response response = execute();
            try {
                responseListener.executeSuccess(this, response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public Request clone() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this.requestMeta);
            oos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            RequestMeta requestMeta = (RequestMeta) ois.readObject();
            requestMeta.url = this.requestMeta.url;
            requestMeta.method = this.requestMeta.method;
            requestMeta.proxy = this.requestMeta.proxy;
            requestMeta.userContentType = this.requestMeta.userContentType;
            requestMeta.requestBody = this.requestMeta.requestBody;
            RequestImpl request = new RequestImpl(clientConfig);
            request.requestMeta = requestMeta;
            return request;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public RequestMeta requestMeta() {
        return requestMeta;
    }

    /**
     * 执行重定向操作
     * @param location 重定向地址
     * */
    public void redirect(String location){
        //处理相对路径形式的重定向
        if (location.startsWith("http")) {
            url(location);
        } else if (location.startsWith("/")) {
            url(requestMeta.url.getProtocol() + "://" + requestMeta.url.getHost() + ":" + (requestMeta.url.getPort() == -1 ? requestMeta.url.getDefaultPort() : requestMeta.url.getPort()) + location);
        } else {
            String u = requestMeta.url.toString();
            url(u.substring(0, u.lastIndexOf("/")) + "/" + location);
        }
        //重定向时方法改为get方法,删除所有主体内容
        requestMeta.statusLine = null;
        method(Request.Method.GET);
        requestMeta.statusLine = null;
        requestMeta.contentType = null;
        requestMeta.userContentType = null;
        requestMeta.boundary = null;
        requestMeta.parameterMap.clear();
        requestMeta.dataMap.clear();
        requestMeta.dataFileMap.clear();
        requestMeta.requestBody = null;
        requestMeta.bodyLog = null;
    }
}