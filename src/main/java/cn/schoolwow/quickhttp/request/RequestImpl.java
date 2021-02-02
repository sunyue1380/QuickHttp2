package cn.schoolwow.quickhttp.request;

import cn.schoolwow.quickhttp.domain.ClientConfig;
import cn.schoolwow.quickhttp.domain.RequestMeta;
import cn.schoolwow.quickhttp.listener.ResponseListener;
import cn.schoolwow.quickhttp.response.Response;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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
        String encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        requestMeta.headers.put("Authorization", "Basic " + encoded);
        return this;
    }

    @Override
    public Request charset(String charset) {
        requestMeta.charset = charset;
        return this;
    }

    @Override
    public Request userAgent(String userAgent) {
        requestMeta.headers.put("User-Agent", userAgent);
        return this;
    }

    @Override
    public Request userAgent(UserAgent userAgent) {
        requestMeta.headers.put("User-Agent", userAgent.userAgent);
        return this;
    }

    @Override
    public Request referrer(String referrer) {
        requestMeta.headers.put("Referer", referrer);
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
        return header("X-Requested-With", "XMLHttpRequest")
                .header("Origin", url.getProtocol() + "://" + url.getHost());
    }

    @Override
    public Request ranges(long start, long end) {
        return header("Range", "bytes=" + start + "-" + (end > 0 ? end : ""));
    }

    @Override
    public Request boundary(String boundary) {
        requestMeta.boundary = boundary;
        return this;
    }

    @Override
    public Request header(String name, String value) {
        requestMeta.headers.put(name, value);
        return this;
    }

    @Override
    public Request headers(Map<String, String> headerMap) {
        requestMeta.headers.putAll(headerMap);
        return this;
    }

    @Override
    public Request cookie(String name, String value) {
        HttpCookie httpCookie = new HttpCookie(name,value);
        httpCookie.setMaxAge(3600000);
        httpCookie.setDomain("."+requestMeta.url.getHost());
        httpCookie.setPath("/");
        httpCookie.setVersion(0);
        httpCookie.setDiscard(false);
        clientConfig.cookieOption.addCookie(httpCookie);
        return this;
    }

    @Override
    public Request cookie(String cookie) {
        clientConfig.cookieOption.addCookieString("."+requestMeta.url.getHost(),cookie);
        return this;
    }

    @Override
    public Request cookie(HttpCookie httpCookie) {
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
        requestMeta.parameters.put(key, value);
        return this;
    }

    @Override
    public Request data(String key, String value) {
        requestMeta.dataMap.put(key, value);
        return this;
    }

    @Override
    public Request data(String key, Path file) {
        requestMeta.dataFileMap.put(new String(key), file);
        return this;
    }

    @Override
    public Request data(Map<String, String> dataMap) {
        requestMeta.dataMap.putAll(dataMap);
        return this;
    }

    @Override
    public Request requestBody(String body) {
        requestMeta.requestBody = body.getBytes();
        return this;
    }

    @Override
    public Request requestBody(JSONObject body) {
        requestMeta.requestBody = body.toJSONString().getBytes();
        return this;
    }

    @Override
    public Request requestBody(JSONArray body) {
        requestMeta.requestBody = body.toJSONString().getBytes();
        return this;
    }

    @Override
    public Request requestBody(Path file) throws IOException {
        requestMeta.requestBody = Files.readAllBytes(file);
        requestMeta.contentType = Files.probeContentType(file);
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
    public Response execute() throws IOException {
        RequestExecutor requestExecutor = new RequestExecutor(this, clientConfig);
        return requestExecutor.execute();
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
            try {
                Response response = execute();
                responseListener.executeSuccess(this, response);
            } catch (IOException e) {
                e.printStackTrace();
                responseListener.executeFail(this, e);
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
}