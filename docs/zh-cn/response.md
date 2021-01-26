# Response

Response接口定义了获取http请求返回结果的方法

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
//获取状态码
System.out.println(response.statusCode());
//获取头部
System.out.println(response.headers());
//获取内容
System.out.println(response.body());
```

## 接口方法

|方法|含义|
|---|---|
|url()|获取返回地址|
|statusCode()|获取状态码|
|statusMessage()|获取状态说明|
|charset|获取编码格式|
|contentType()|获取返回格式类型|
|contentLength()|获取大小|
|filename()|获取文件名|
|acceptRanges()|是否支持分段下载|
|hasHeader(String name)|是否有该Header|
|hasHeader(String name, String value)|是否存在该Header|
|header(String name)|获取头部信息|
|headers()|获取所有Header信息|
|maxDownloadSpeed(int maxDownloadSpeed)|设置最大下载速率(kb/s)|
|body()|返回字符串|
|bodyAsJSONObject()|返回JSON对象|
|bodyAsJSONArray()|返回JSON数组|
|jsonpAsJSONObject()|解析jsonp返回JSON对象|
|jsonpAsJSONArray()|解析jsonp返回JSON数组|
|bodyAsBytes()|返回字节数组|
|bodyAsFile(Path file)|将输入流写入到指定文件.若文件已存在,则会追加到文件尾部|
|bodyStream()|获取输入流|
|parse()|解析成DOM数并返回Document对象|
|parser()|使用事件监听机制获取处理DOM树|
|disconnect()|断开连接|
|responseMeta()|获取返回元数据|