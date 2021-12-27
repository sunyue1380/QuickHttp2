# Document

Document is http document.

> There are some problems in DOM parse of QuickHttp if html document doesn't follow strict syntax rules. So use this method carefully.

## Get Document

```java
Document doc = QuickHttp.connect("https://www.google.com")
        .execute()
        .parse();
```

## Document Example

```java
Element body = doc.select("html body");
String title = body.attr("title");
List<Element> childElement = body.childElements();
String outerHtml = body.outerHtml();
//more operations......
```