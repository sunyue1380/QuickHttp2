package cn.schoolwow.quickhttp.document.element;

import java.util.Map;

public interface Element {
    /***
     * 通过CSS选择器查找元素
     */
    Elements select(String cssQuery);

    /***
     * 通过CSS选择器查找元素
     */
    Element selectFirst(String cssQuery);

    /***
     * 通过CSS选择器查找元素
     */
    Element selectLast(String cssQuery);

    /**
     * 获取属性
     */
    Map<String, String> attribute();

    /**
     * 获取id
     */
    String id();

    /**
     * 是否存在class
     */
    boolean hasClass(String className);

    /**
     * 是否存在属性
     */
    boolean hasAttr(String attributeKey);

    /**
     * 获取属性
     */
    String attr(String attributeKey);

    /**
     * 设置属性
     */
    void attr(String attributeKey, String attributeValue);

    /**
     * 清除属性
     */
    void clearAttributes();

    /**
     * 获取标签名
     */
    String tagName();

    /**
     * 获取子节点文本
     */
    String text();

    /**
     * 获取文本节点列表
     */
    Elements textElement();

    /**
     * 获取html内容
     */
    String html();

    /**
     * 获取当前节点文本
     */
    String ownText();

    /**
     * 获取outerHTML内容
     */
    String outerHtml();

    /**
     * 获取格式化后的outerHTML内容
     */
    String prettyOuterHtml();

    /**
     * 获取value属性
     */
    String val();

    /**
     * 获取父节点
     */
    Element parent();

    /**
     * 获取所有子节点
     */
    Elements children();

    /**
     * 获取首个子节点
     */
    Element firstChild();

    /**
     * 获取末尾子节点
     */
    Element lastChild();

    /**
     * 获取指定子节点
     */
    Element childElement(int index);

    /**
     * 获取所有子节点
     */
    Elements childElements();

    /**
     * 获取兄弟节点
     */
    Elements siblingElements();

    /**
     * 获取它的前一个节点
     */
    Element previousElementSibling();

    /**
     * 获取它的后一个节点
     */
    Element nextElementSibling();

    /**
     * 获取节点在父节点的子节点中的索引
     */
    int elementSiblingIndex();

    /**
     * 获取该节点所有子节点(包括它自己)
     */
    Elements getAllElements();

    /**
     * 移除该节点
     */
    Element remove();
}
