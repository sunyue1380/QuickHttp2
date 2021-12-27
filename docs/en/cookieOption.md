# CookieOption

QuickHttp can manage cookie automatically.

## Default CookieOption

```java
//get default CookieOption
CookieOption cookieOption = QuickHttp.clientConfig().cookieOption();
//get all cookie
List<HttpCookie> httpCookieList = cookieOption.getCookieList();
//get cookie list of domain google.com
List<HttpCookie> baiduCookieList = cookieOption.getCookieList("google.com");
//add cookie
cookieOption.addCookieString("google.com","cookie1=value1;cookie2=value2;");
//remove cookie of domain google
cookieOption.removeCookie("google.com");
//set cookie policy
cookieOption.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
//more CookieOption operation ......
```

QuickHttp will add ``Cookie`` header before send http request if you has set corresponding cookie.