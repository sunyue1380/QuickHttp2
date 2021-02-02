# CookieOption

CookieOption定义了对于Cookie的相关操作.每个QuickHttpClient有各自独立的Cookie管理策略,存储.

## 获取CookieOption

```java
CookieOption cookieOption = QuickHttp.newQuickHttpClient().clientConfig().cookieOption();
```

## 接口方法

|方法|含义|
|---|---|
|hasCookie(String domain, String name)|cookie是否存在|
|getCookie(String domain, String name)|获取cookie|
|getCookieList(String domain)|获取指定域名下的Cookie列表|
|getCookieList()|获取所有Cookie|
|addCookieString(String domain, String cookie)|添加Cookie,**从v2.0.1开始将domain参数作为第一个参数**|
|addCookie(String domain, String name, String value)|添加Cookie,**从v2.0.1开始将domain参数作为第一个参数**|
|addCookie(HttpCookie httpCookie)|添加Cookie|
|addCookie(List<HttpCookie> httpCookieList)|添加Cookie列表|
|removeCookie(String domain)|删除指定域名下所有Cookie|
|removeCookie(String domain, String name)|删除指定域名下的指定Cookie|
|removeCookie(HttpCookie httpCookie)|删除指定Cookie|
|clearCookieList()|清空Cookie列表|
|setCookiePolicy(CookiePolicy cookiePolicy)|设置Cookie策略|
|cookieManager|获取CookieManage对象|

