# CookieOption

QuickHttp支持自动管理Cookie

## 默认CookieOption

```java
//获取默认的Cookie选项
CookieOption cookieOption = QuickHttp.clientConfig().cookieOption();
//获取管理的所有域名Cookie
List<HttpCookie> httpCookieList = cookieOption.getCookieList();
//获取baidu.com域名下的所有Cookie
List<HttpCookie> baiduCookieList = cookieOption.getCookieList("baidu.com");
//添加Cookie
cookieOption.addCookieString("baidu.com","cookie1=value1;cookie2=value2;");
//移除baidu.com域名下的所有Cookie
cookieOption.removeCookie("baidu.com");
//设置Cookie策略
cookieOption.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
//更多CookieOption操作......
```

设置完成Cookie后，QuickHttp发送的http请求会根据请求域名自动添加上``Cookie``头部.