# 配置QuickHttpClient

使用QuickHttp首先配置QuickHttpClient.

## 获取CookieOption

```java
QuickHttpClient client = QuickHttp.newQuickHttpClient()
                .proxy("127.0.0.1",8888)
                //设置连接超时时间
                .connectTimeout(3000)
                //设置连接超时时间
                .readTimeout(5000)
                //是否自动重定向
                .followRedirects(true)
                //允许重定向的最大次数
                .maxFollowRedirectTimes(20)
                //是否忽略http状态错误
                .ignoreHttpErrors(true);
```

## 全部配置选项

|配置项|含义|默认值|
|---|---|---|
|proxy|设置http代理|无|
|connectTimeout|连接超时时间(毫秒)|0|
|readTimeout|读取超时时间(毫秒)|0|
|followRedirects|是否自动重定向|true|
|maxFollowRedirectTimes|最大重定向次数|20|
|ignoreHttpErrors|是否忽略http错误(4xx和5xx响应码)|false|
|retryTimes|**请求超时时**重试次数|3|
|hostnameVerifier|https链接参数||
|sslSocketFactory|https链接参数||
|threadPoolExecutor(ThreadPoolExecutor threadPoolExecutor)|异步执行时的线程池|
|origin|设置origin,执行http请求时会拼接到url前面|无|
|quickHttpClientListener|事件监听参数|无|

## origin方法使用场景

```java
QuickHttpClient client = QuickHttp.newQuickHttpClient().origin("http://127.0.0.1");
//实际访问http://127.0.0.1/hello
client.connect("/hello").execute();
//如果链接以http开头,则origin属性失效,以下代码访问http://192.168.1.100/hello
client.connect("http://192.168.1.100/hello").execute();
```

## 事件监听

QuickHttp提供了访问前后的事件监听

```java
QuickHttpClient client = QuickHttp.newQuickHttpClient();
client.quickHttpClientListener(new QuickHttpClientListener() {
    @Override
    public void beforeExecute(Request request) {
        RequestMeta requestMeta = request.requestMeta();
        System.out.println("请求执行前");
        System.out.println(requestMeta.url);
        System.out.println(requestMeta.method);
    }

    @Override
    public void executeSuccess(Request request, Response response) {
        System.out.println("请求执行成功");
    }

    @Override
    public void executeFail(Request request, Exception e) {
        System.out.println("请求执行失败");
    }
});
```

## 全局Client

QuickHttp内置全局Client,可直接通过静态方法调用connect,clientConfig等方法.