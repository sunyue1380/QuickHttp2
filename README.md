# QuickHttp

QuickHttp是一个http客户端框架

* 支持Cookie的自动管理
* 支持设置全局http代理
* 支持异步请求
* 声明式API设计和链式操作设计

# 快速入门

## 1 导入QuickHttp
```
<dependency>
  <groupId>cn.schoolwow</groupId>
  <artifactId>QuickHttp</artifactId>
  <version>{最新版本}</version>
</dependency>
```

[最新版本查询](https://search.maven.org/artifact/cn.schoolwow/QuickHttp)

## 2 使用QuickHttp
```java
QuickHttpClient client = QuickHttp.newQuickHttpClient();
Response response = client.connect("https://www.baidu.com")
                //添加请求头部
                .header("header","value")
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

* 提交Issue
* 邮箱: 648823596@qq.com
* QQ群: 958754367(quick系列交流,群初建,人较少)

# 开源协议
本软件使用 [GPL](http://www.gnu.org/licenses/gpl-3.0.html) 开源协议!
非商业使用无限制,若需商业使用,请通过邮箱,QQ群,微信号联系本人获取授权.