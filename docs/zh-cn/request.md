# Request

Request接口定义了设置请求参数相关方法

## GET

```java
Response response = QuickHttp.connect("https://www.baidu.com")
        .method(Request.Method.GET)
        .parameter("key","value")
        .execute();
```

## APPLICATION_X_WWW_FORM_URLENCODED

```java
Response response = QuickHttp.connect("https://www.baidu.com")
        .method(Request.Method.POST)
        .data("key","value")
        .execute();
```

## MULTIPART_FORMDATA

```java
Response response = QuickHttp.connect("https://www.baidu.com")
        .method(Request.Method.POST)
        .data("file", Paths.get("path/to/file"))
        .data("key","value")
        .execute();
```

## APPLICATION/JSON

```java
JSONObject requestBody = new JSONObject();
Response response = QuickHttp.connect("https://www.baidu.com")
        .method(Request.Method.POST)
        .requestBody(requestBody)
        .execute();
```

## 头部设置

```java
Response response = QuickHttp.connect("https://www.baidu.com")
        .setHeader("header name","header value")
        .addHeader("header name","header value")
        //添加origin头部
        .origin()
        .ajax()
        //设置用户代理
        .userAgent(Request.UserAgent.ANDROID)
        //设置refer头部
        .referrer("refer url")
        //设置Content-Type
        .contentType("application/json")
        //上传文件时设置分隔符
        .boundary("--xxxxxxx")
        //设置Basic Auth参数
        .basicAuth("username","password")
        //设置分段下载头部
        .ranges(1,100)
        //更多设置Http头部的方法......
        .requestBody(requestBody)
        .execute();
```

## 配置项

```java
Response response = QuickHttp.connect("https://www.baidu.com")
        //设置http代理
        .proxy("127.0.0.1",8888)
        //请求失败时重试次数
        .retryTimes(3)
        //是否自动重定向
        .followRedirects(false)
        //最大重定向次数
        .maxFollowRedirectTimes(20)
        //连接超时时间(毫秒)
        .connectTimeout(3000)
        //读取超时时间(毫秒)
        .readTimeout(5000)
        //是否忽略http错误(4xx,5xx)
        .ignoreHttpErrors(true)
        //设置是否使用chunked模式
        .streamMode(Request.StreamingMode.Chunked)
        //设置请求日志文件保存路径,便于调试
        .logFilePath("path/to/logfile")
        //更多配置项......
        .execute()
```

# Content-Type优先级

默认情况下QuickHttp会根据调用的方法自动设置Content-Type头部.但是若存在冲突，则优先级顺序如下:

|优先级|调用方法|Content-Type|
|---|---|---|
|1|data(String key, Path file)|multipart/form-data|
|2|requestBody(String body)|application/json|
|3|缺省情况|application/x-www-form-urlencoded|

> 若用户手动调用了contentType方法,则以用户调用所指定的Content-Type为准