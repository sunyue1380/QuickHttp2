# Request

Request接口定义了设置请求参数相关方法

## 获取Request

```java
QuickHttpClient client = QuickHttp.newQuickHttpClient();
Request request = client.connect("https://www.baidu.com")
                //添加请求头部
                .header("header","value")
                //url路径表单参数
                .parameter("a","1")
                //body表单参数
                .data("b","2")
                //上传文件
                .data("file",Paths.get("filePath"))
                //指定body内容
                .requestBody("body");
Response response = request.execute();
```

## 接口方法

|方法|含义|
|---|---|
|url(URL url)|指定请求地址|
|url(String url)|指定请求地址|
|method(String method)|设置请求方法|
|method(Method method)|设置请求方法|
|basicAuth(String username, String password)|设置Basic Auth请求头部|
|charset(String charset)|指定编码格式|
|userAgent(String userAgent)|指定用户代理|
|userAgent(UserAgent userAgent)|指定用户代理|
|referrer(String referrer)|指定referrer|
|contentType(String contentType)|指定Content-Type|
|contentType(ContentType contentType)|指定Content-Type|
|ajax|设置ajax请求头部|
|ranges(long start, long end)|设置分段下载|
|boundary(String boundary)|指定boundary|
|header(String name, String value)|设置头部字段信息|
|headers(Map<String, String> headerMap)|设置头部字段信息|
|parameter(String key, String value)|设置路径请求参数|
|data(String key, String value)|设置表单请求参数|
|data(String key, Path file)|设置表单请求参数(上传文件)|
|data(Map<String, String> dataMap)|设置表单请求参数|
|requestBody(String body)|设置请求体内容|
|requestBody(JSONObject body)|设置请求体内容|
|requestBody(JSONArray body)|设置请求体内容|
|requestBody(Path file)|设置请求体内容|
|proxy(Proxy proxy)|设置代理|
|proxy(String host, int port)|设置代理|
|connectTimeout(int connectTimeoutMillis)|设置连接超时时间(毫秒)|
|readTimeout(int readTimeoutMillis)|设置读取超时时间(毫秒)|
|followRedirects(boolean followRedirects)|是否自动重定向|
|maxFollowRedirectTimes(int maxFollowRedirectTimes)|指定最大重定向次数|
|ignoreHttpErrors(boolean ignoreHttpErrors)|是否忽略http错误(4xx和5xx响应码)|
|retryTimes(int retryTimes)|**请求超时**时重试次数|
|execute|执行请求|
|enqueue(ResponseListener responseListener)|异步执行请求|
|RequestMeta requestMeta()|获取设置的请求信息|

# Content-Type的设置

QuickHttp会根据请求参数自动设置Content-Type头部.判断顺序如下:

|优先级|调用方法|Content-Type|
|---|---|---|
|1|data(String key, Path file)|multipart/form-data|
|2|requestBody(String body)|application/json|
|3|默认|application/x-www-form-urlencoded|

> 若用户手动调用了contentType方法,则以用户调用所指定的Content-Type为准