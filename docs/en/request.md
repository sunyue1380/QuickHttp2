# Request

Request defined lots of methods to customize request behavior.

## GET

```java
Response response = QuickHttp.connect("https://www.google.com")
        .method(Request.Method.GET)
        .parameter("key","value")
        .execute();
```

## APPLICATION_X_WWW_FORM_URLENCODED

```java
Response response = QuickHttp.connect("https://www.google.com")
        .method(Request.Method.POST)
        .data("key","value")
        .execute();
```

## MULTIPART_FORMDATA

```java
Response response = QuickHttp.connect("https://www.google.com")
        .method(Request.Method.POST)
        .data("file", Paths.get("path/to/file"))
        .data("key","value")
        .execute();
```

## APPLICATION/JSON

```java
JSONObject requestBody = new JSONObject();
Response response = QuickHttp.connect("https://www.google.com")
        .method(Request.Method.POST)
        .requestBody(requestBody)
        .execute();
```

## Header

```java
Response response = QuickHttp.connect("https://www.google.com")
        .setHeader("header name","header value")
        .addHeader("header name","header value")
        //add origin header
        .origin()
        .ajax()
        //set user Agent
        .userAgent(Request.UserAgent.ANDROID)
        //add refer header
        .referrer("refer url")
        //set Content-Type
        .contentType("application/json")
        //set boundary
        .boundary("--xxxxxxx")
        //set Basic Auth
        .basicAuth("username","password")
        //http 206 part download
        .ranges(1,100)
        //more http operations......
        .requestBody(requestBody)
        .execute();
```

## Configuration

```java
Response response = QuickHttp.connect("https://www.google.com")
        //proxy
        .proxy("127.0.0.1",8888)
        //retry times when fail
        .retryTimes(3)
        .followRedirects(false)
        .maxFollowRedirectTimes(20)
        .connectTimeout(3000)
        .readTimeout(5000)
        .ignoreHttpErrors(true)
        .streamMode(Request.StreamingMode.Chunked)
        //path to save request log
        .logFilePath("path/to/logfile")
        //more operations......
        .execute()
```

# Content-Type Priority

|Priority|Method|Content-Type|
|---|---|---|
|1|data(String key, Path file)|multipart/form-data|
|2|requestBody(String body)|application/json|
|3|Default|application/x-www-form-urlencoded|

> The forgoing rules has no effect if user invokes ``contentType`` method manually.