# QuickHttpClient

There is a default QuickHttpClient instance.

```java
//use default client instance
QuickHttp.connect("https://www.google.com");
```

However you can create your own ``QuickHttpClient``.

Every QuickHttpClient is separated from others.

```java
QuickHttpClient client = QuickHttp.newQuickHttpClient();
client.connect("https://www.google.com");
```

> Default Client CookieOption is shared by all clients.

## Origin

```java
QuickHttpClient client = QuickHttp.newQuickHttpClient().origin("http://127.0.0.1");
//actual url:http://127.0.0.1/hello
client.connect("/hello").execute();
//actual url:http://192.168.1.100/hello  if url starts with http,then origin method has no effect.
client.connect("http://192.168.1.100/hello").execute();
```

## Event Listener

QuickHttp provides listener.

```java
QuickHttpClient client = QuickHttp.newQuickHttpClient();
client.quickHttpClientListener(new QuickHttpClientListener() {
    @Override
    public void beforeExecute(Request request) {
        RequestMeta requestMeta = request.requestMeta();
        System.out.println("before send request");
        System.out.println(requestMeta.url);
        System.out.println(requestMeta.method);
    }

    @Override
    public void executeSuccess(Request request, Response response) {
        System.out.println("request execute successful");
    }

    @Override
    public void executeFail(Request request, Exception e) {
        System.out.println("request throw exception");
    }
});
```

## Client Option

```java
QuickHttp.clientConfig()
        .proxy("127.0.0.1",8888)
        //directory which save request log
        .logDirectoryPath("path/to/log/directory")
        .connectTimeout(3000)
        .readTimeout(5000)
        .followRedirects(false)
        .maxFollowRedirectTimes(50)
        .ignoreHttpErrors(true)
        .retryTimes(10);
```