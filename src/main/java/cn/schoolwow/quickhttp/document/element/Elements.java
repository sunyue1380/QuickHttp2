package cn.schoolwow.quickhttp.document.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Elements extends ArrayList<Element> {
    public Elements() {
    }

    public Elements(int initialCapacity) {
        super(initialCapacity);
    }

    public Elements(Collection<Element> elements) {
        super(elements);
    }

    public Elements(List<Element> elements) {
        super(elements);
    }

    public Elements(Element... elements) {
        super(Arrays.asList(elements));
    }

    /**
     * 返回集合的第一个元素
     */
    public Element first() {
        if (this.isEmpty()) {
            return null;
        } else {
            return this.get(0);
        }
    }

    /**
     * 返回集合的最后一个元素
     */
    public Element last() {
        if (this.isEmpty()) {
            return null;
        } else {
            return this.get(this.size() - 1);
        }
    }

    /**
     * 返回集合第一个标签的标签名
     */
    public String tagName() {
        return this.get(0).tagName();
    }

    /**
     * 获取文本元素
     */
    public String text() {
        StringBuilder sb = new StringBuilder();
        for (Element element : this) {
            sb.append(element.text() + " ");
        }
        return sb.toString();
    }

    /**
     * 是否存在属性
     */
    public boolean hasAttr(String attributeKey) {
        for (Element element : this) {
            if (element.hasAttr(attributeKey)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取属性
     */
    public String attr(String attributeKey) {
        for (Element element : this) {
            if (element.hasAttr(attributeKey)) {
                return element.attr(attributeKey);
            }
        }
        return null;
    }

    /**
     * 获取集合内的所有该元素属性
     */
    public List<String> eachAttr(String attributeKey) {
        List<String> attrs = new ArrayList<>(size());
        for (Element element : this) {
            if (element.hasAttr(attributeKey))
                attrs.add(element.attr(attributeKey));
        }
        return attrs;
    }

    /**
     * 获取集合内的所有元素的文本
     */
    public List<String> eachText() {
        ArrayList<String> texts = new ArrayList<>(size());
        for (Element element : this) {
            texts.add(element.text());
        }
        return texts;
    }

    public Elements remove() {
        Elements elements = new Elements(this.size());
        for (int i = 0; i < this.size(); i++) {
            Element e = this.get(i);
            e.remove();
            elements.add(e);
        }
        return elements;
    }
}
