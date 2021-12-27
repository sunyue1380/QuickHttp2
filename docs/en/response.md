# Response

Response defined lots of methods to customize response behavior.

Please refer to [Request](request.md) for geting Response instance.

## Response Example

```java
int statusCode = response.statusCode();
String statusMessage = response.statusMessage();
Map<String, List<String>> headers = response.headers();
response.contentLength();
response.contentType();
response.filename();
//......
JSONObject result = response.bodyAsJSONObject();
String body = response.body();
response.maxDownloadSpeed(1024);
response.bodyAsFile("path/to/file");
InputStream inputStream = response.bodyStream();
byte[] bytes = response.bodyAsBytes();

Document doc = response.parse();
DocumentParser documentParser = response.parser();
```