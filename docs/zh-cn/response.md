# Response

Response接口定义了获取http请求返回结果的方法

如何获取Response对象请参阅[request](request.md)

## Response调用实例代码

```java
//获取状态码
int statusCode = response.statusCode();
//响应行
String statusMessage = response.statusMessage();
//获取响应头部
Map<String, List<String>> headers = response.headers();
response.contentLength();
response.contentType();
response.filename();
//......
//将响应内容解析为JSON对象
JSONObject result = response.bodyAsJSONObject();
//返回内容字符串
String body = response.body();
//限制文件最大下载速度
response.maxDownloadSpeed(1024);
//保存响应内容到文件中
response.bodyAsFile("path/to/file");
//获取响应输入流
InputStream inputStream = response.bodyStream();
//获取响应字节数组
byte[] bytes = response.bodyAsBytes();

//解析成HTML文档
Document doc = response.parse();
//事件解析
DocumentParser documentParser = response.parser();
```