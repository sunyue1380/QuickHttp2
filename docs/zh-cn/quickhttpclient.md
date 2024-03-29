# QuickHttpClient

QuickHttp存在默认的QuickHttpClient实例。

```java
//使用默认的Client发起请求
QuickHttp.connect("https://www.baidu.com");
```

然而QuickHttp支持新建多个独立的``QuickHttpClient``发起请求。

```java
QuickHttpClient client = QuickHttp.newQuickHttpClient();
client.connect("https://www.baidu.com");
```

每个Client是相互独立的。

> 默认Client的CookieOption会影响所有的Client。

## Origin方法

Client设置origin

```java
QuickHttpClient client = QuickHttp.newQuickHttpClient().origin("http://127.0.0.1");
//实际访问http://127.0.0.1/hello
client.connect("/hello").execute();
//如果链接以http开头,则origin属性失效,以下代码访问http://192.168.1.100/hello
client.connect("http://192.168.1.100/hello").execute();
```

## 事件监听

QuickHttp提供了请求访问前后的事件监听

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

## Client配置选项

```java
QuickHttp.clientConfig()
        .proxy("127.0.0.1",8888)
        //设置请求日志文件夹保存路径
        .logDirectoryPath("path/to/log/directory")
        .connectTimeout(3000)
        .readTimeout(5000)
        .followRedirects(false)
        .maxFollowRedirectTimes(50)
        .ignoreHttpErrors(true)
        .retryTimes(10);
```