# QuickHttp

QuickHttp is a Http Request Client.

* Cookie support
* Support global http proxy
* Chain Operation Design

# QuickStart

## 1 Import QuickHttp
```
<dependency>
  <groupId>cn.schoolwow</groupId>
  <artifactId>QuickHttp</artifactId>
  <version>{LatestVersion}</version>
</dependency>
```

> [Query QuickHttp Lastest Version](https://search.maven.org/search?q=a:QuickHttp)

## 2 use QuickHttp
```java
QuickHttpClient client = QuickHttp.newQuickHttpClient();
Response response = client.connect("https://www.google.com")
        //add header
        .setHeader("header","value")
        //url path variable
        .parameter("a","1")
        //body parameter
        .data("b","2")
        //upload file
        .data("file",Paths.get("filePath"))
        .execute();
//get status code
System.out.println(response.statusCode());
//get headers
System.out.println(response.headers());
//get response body
System.out.println(response.body());
```
# Feedback

If you have any suggestions please Pull Request or mailto 648823596@qq.com.

# LICENSE

[LGPL](http://www.gnu.org/licenses/lgpl-3.0-standalone.html)