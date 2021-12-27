# Document

Document对象表示一个http文档.

> QuickHttp的DOM解析功能尚不完善,对于未严格按照xml格式的html文档可能会获取到错误的结果.

## 获取Document

```java
Document doc = QuickHttp.connect("https://www.baidu.com")
        .execute()
        .parse();
```

## Document操作示例

```java
Element body = doc.select("html body");
String title = body.attr("title");
List<Element> childElement = body.childElements();
String outerHtml = body.outerHtml();
//更多操作......
```