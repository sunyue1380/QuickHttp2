# Document

Document对象表示一个http文档.

> QuickHttp的DOM解析功能尚不完善,对于未严格按照xml格式的html文档可能会获取到错误的结果.

# 查找元素(CSS选择器)

```java
Document doc = response.parse();
Elements divs = doc.select("div");
```

# 元素方法

Element接口定义元素相关方法

|方法|含义|
|---|---|
|select(String cssQuery)|通过CSS选择器查找元素|
|selectFirst(String cssQuery)|通过CSS选择器查找元素|
|selectLast(String cssQuery)|通过CSS选择器查找元素|
|attribute()|获取属性|
|id()|获取id|
|hasClass(String className)|是否存在class|
|hasAttr(String attributeKey)|是否存在属性|
|attr(String attributeKey)|获取属性|
|attr(String attributeKey, String attributeValue)|设置属性|
|clearAttributes()|清除属性|
|tagName()|获取标签名|
|text()|获取子节点文本|
|textElement()|获取文本节点列表|
|html()|获取html内容|
|ownText()|获取当前节点文本|
|outerHtml()|获取outerHTML内容|
|prettyOuterHtml()|获取格式化后的outerHTML内容|
|val()|获取value属性|
|parent()|获取父节点|
|children()|获取所有子节点|
|firstChild()|获取首个子节点|
|lastChild()|获取末尾子节点|
|childElement(int index)|获取指定子节点|
|childElements()|获取所有子节点|
|siblingElements()|获取兄弟节点|
|previousElementSibling()|获取它的前一个节点|
|nextElementSibling()|获取它的后一个节点|
|elementSiblingIndex()|获取节点在父节点的子节点中的索引|
|getAllElements()|获取该节点所有子节点(包括它自己)|
|remove()|移除该节点|