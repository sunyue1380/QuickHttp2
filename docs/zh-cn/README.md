# QuickHttp

QuickHttp是一个http客户端框架

* 支持Cookie
* 支持全局http代理
* 链式操作API设计

# 快速入门

## 1 导入QuickHttp
```
<dependency>
  <groupId>cn.schoolwow</groupId>
  <artifactId>QuickHttp</artifactId>
  <version>{最新版本}</version>
</dependency>
```

> [QuickHttp最新版本查询](https://search.maven.org/search?q=a:QuickHttp)

## 2 使用QuickHttp
```java
QuickHttpClient client = QuickHttp.newQuickHttpClient();
Response response = client.connect("https://www.baidu.com")
        //添加请求头部
        .setHeader("header","value")
        //url路径表单参数
        .parameter("a","1")
        //body表单参数
        .data("b","2")
        //上传文件
        .data("file",Paths.get("filePath"))
        .execute();
//返回状态码
System.out.println(response.statusCode());
//返回头部
System.out.println(response.headers());
//返回内容
System.out.println(response.body());
```

# 详细文档

[点此访问](https://quickhttp.schoolwow.cn/)

# 反馈

若有问题请提交Issue或者发送邮件到648823596@qq.com.

# 开源协议
本软件使用[LGPL](http://www.gnu.org/licenses/lgpl-3.0-standalone.html)开源协议!