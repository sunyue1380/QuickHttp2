package cn.schoolwow.quickhttp.document.element;

import cn.schoolwow.quickhttp.document.query.Evaluator;
import cn.schoolwow.quickhttp.document.query.QueryParser;

import java.util.*;

public class AbstractElement implements Element {
    /**
     * html拼接字符串
     */
    private static ThreadLocal<StringBuilder> htmlBuilderThreadLocal = new ThreadLocal<>();
    /***attribute拼接字符串*/
    private static ThreadLocal<StringBuilder> attributeBuilderThreadLocal = new ThreadLocal<>();

    {
        htmlBuilderThreadLocal.set(new StringBuilder());
        attributeBuilderThreadLocal.set(new StringBuilder());
    }

    /**
     * 节点名称
     */
    public String tagName;
    /**
     * 是否是单节点
     */
    public boolean isSingleNode;
    /**
     * 是否是注释节点
     */
    public boolean isComment;
    /**
     * 是否是文本节点
     */
    public boolean isTextNode;
    /**
     * 父节点
     */
    public AbstractElement parent;
    /**
     * 原始属性字符串
     */
    public String originAttributeText;
    /**
     * 原始属性
     */
    public Map<String, String> originAttributes = new HashMap<>();
    /**
     * 属性
     */
    public Map<String, String> attributes = new HashMap<>();
    /**
     * 原始文本内容
     */
    public String ownOriginText;
    /**
     * 转义后文本内容
     */
    public String ownText;
    /**
     * 子元素(不包含文本节点)
     */
    public List<AbstractElement> childList = new ArrayList<>();
    /**
     * 子元素
     */
    public List<AbstractElement> childTextList = new ArrayList<>();

    @Override
    public Elements select(String cssQuery) {
        Elements elements = new Elements();
        Evaluator evaluator = QueryParser.parse(cssQuery);
        //广度遍历
        LinkedList<Element> linkedList = new LinkedList<>();
        linkedList.offer(this);
        while (!linkedList.isEmpty()) {
            Element element = linkedList.poll();
            if (element.tagName() == null) {
                continue;
            }
            //排除掉注释标签
            if (evaluator.matches(element)) {
                elements.add(element);
            }
            linkedList.addAll(element.childElements());
        }
        return elements;
    }

    @Override
    public Element selectFirst(String cssQuery) {
        return select(cssQuery).first();
    }

    @Override
    public Element selectLast(String cssQuery) {
        return select(cssQuery).last();
    }

    @Override
    public Map<String, String> attribute() {
        return attributes;
    }

    @Override
    public String id() {
        return attributes.get("id");
    }

    @Override
    public boolean hasClass(String className) {
        String elementClassName = attributes.get("class");
        if (elementClassName == null || elementClassName.isEmpty()) {
            return false;
        }
        String[] classNames = new String[]{className};
        if (className.contains(".")) {
            classNames = className.split("\\.");
        }
        for (String _className : classNames) {
            if (!elementClassName.contains(_className)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasAttr(String attributeKey) {
        return attributes.containsKey(attributeKey);
    }

    @Override
    public String attr(String attributeKey) {
        String value = attributes.get(attributeKey);
        if (null != value) {
            value = value.replace("\"", "").replace("'", "");
        }
        return value;
    }

    @Override
    public void attr(String attributeKey, String attributeValue) {
        this.originAttributes.put(attributeKey, attributeValue);
        this.attributes.put(attributeKey, attributeValue);
    }

    @Override
    public void clearAttributes() {
        this.originAttributes.clear();
        this.attributes.clear();
    }

    public String tagName() {
        return tagName;
    }

    public String text() {
        Elements elements = textElement();
        StringBuilder builder = htmlBuilderThreadLocal.get();
        builder.setLength(0);
        for (Element element : elements) {
            builder.append(element.ownText());
        }
        return builder.toString();
    }

    @Override
    public Elements textElement() {
        Stack<AbstractElement> stack = new Stack<>();
        stack.push(this);
        Elements elements = new Elements();
        while (!stack.isEmpty()) {
            AbstractElement abstractElement = stack.pop();
            if (abstractElement.isTextNode) {
                elements.add(abstractElement);
            }
            for (int i = abstractElement.childTextList.size() - 1; i >= 0; i--) {
                stack.push(abstractElement.childTextList.get(i));
            }
        }
        return elements;
    }

    @Override
    public String html() {
        StringBuilder builder = htmlBuilderThreadLocal.get();
        builder.setLength(0);
        for (AbstractElement child : childTextList) {
            getHtmlRecursive(child, builder);
        }
        return builder.toString();
    }

    @Override
    public String ownText() {
        if (isSingleNode || isComment || isTextNode) {
            return ownText;
        }
        StringBuilder builder = htmlBuilderThreadLocal.get();
        builder.setLength(0);
        for (AbstractElement abstractElement : childTextList) {
            if (abstractElement.isTextNode) {
                builder.append(abstractElement.ownOriginText);
            }
        }
        return builder.toString();
    }

    @Override
    public String outerHtml() {
        StringBuilder builder = htmlBuilderThreadLocal.get();
        builder.setLength(0);
        if (null != this.tagName && this.tagName.equals("ROOT")) {
            for (AbstractElement child : childTextList) {
                getHtmlRecursive(child, builder);
            }
        } else {
            getHtmlRecursive(this, builder);
        }
        return builder.toString();
    }

    @Override
    public String prettyOuterHtml() {
        StringBuilder builder = htmlBuilderThreadLocal.get();
        builder.setLength(0);
        if (null != this.tagName && this.tagName.equals("ROOT")) {
            for (AbstractElement child : childTextList) {
                getHtmlRecursivePretty(child, builder);
            }
        } else {
            getHtmlRecursivePretty(this, builder);
        }
        return builder.toString();
    }

    @Override
    public String val() {
        if ("textarea".equals(tagName)) {
            return text();
        } else if (hasAttr("value")) {
            return attr("value");
        } else {
            return null;
        }
    }

    @Override
    public Element parent() {
        return parent;
    }

    @Override
    public Elements children() {
        Elements elements = new Elements();
        elements.addAll(this.childList);
        return elements;
    }

    @Override
    public Element firstChild() {
        if (childList.isEmpty()) {
            return null;
        }
        return childList.get(0);
    }

    @Override
    public Element lastChild() {
        if (childList.isEmpty()) {
            return null;
        }
        return childList.get(childList.size() - 1);
    }

    @Override
    public Element childElement(int index) {
        if (index < 1 || index > childList.size()) {
            return null;
        }
        return childList.get(index - 1);
    }

    @Override
    public Elements childElements() {
        Elements elements = new Elements(childList.size());
        elements.addAll(childList);
        return elements;
    }

    @Override
    public Elements siblingElements() {
        Elements elements = new Elements();
        for (Element element : parent.childList) {
            if (element != this) {
                elements.add(element);
            }
        }
        return elements;
    }

    @Override
    public Element previousElementSibling() {
        int pos = elementSiblingIndex();
        if (pos - 1 >= 0) {
            return parent.childList.get(pos - 1);
        } else {
            return null;
        }
    }

    @Override
    public Element nextElementSibling() {
        int pos = elementSiblingIndex();
        if (pos + 1 < parent.childList.size()) {
            return parent.childList.get(pos + 1);
        } else {
            return null;
        }
    }

    @Override
    public int elementSiblingIndex() {
        if (null == parent) {
            return 0;
        }
        for (int i = 0; i < parent.childList.size(); i++) {
            if (parent.childList.get(i) == this) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Elements getAllElements() {
        Stack<AbstractElement> stack = new Stack<>();
        if (this.tagName.equals("ROOT")) {
            for (AbstractElement child : childList) {
                stack.push(child);
            }
        } else {
            stack.push(this);
        }
        Elements elements = new Elements();
        while (!stack.isEmpty()) {
            AbstractElement abstractElement = stack.pop();
            elements.add(abstractElement);
            for (int i = abstractElement.childList.size() - 1; i >= 0; i--) {
                stack.push(abstractElement.childList.get(i));
            }
        }
        return elements;
    }

    @Override
    public Element remove() {
        this.parent.childList.remove(this);
        this.parent.childTextList.remove(this);
        return this;
    }

    @Override
    public String toString() {
        String attribute = getAttribute(this);
        if (isComment) {
            return "<!--" + ownText + "-->";
        } else if (isTextNode) {
            return ownOriginText;
        } else if (isSingleNode) {
            return "<" + tagName + " " + attribute + "/>";
        } else {
            return "<" + tagName + " " + attribute + "></" + tagName + ">";
        }
    }

    private void getHtmlRecursivePretty(AbstractElement abstractElement, StringBuilder builder) {
        //计算\t个数
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StringBuilder tabBuilder = attributeBuilderThreadLocal.get();
        tabBuilder.setLength(0);
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if (stackTraceElement.getMethodName().equals("getHtmlRecursive")) {
                tabBuilder.append("\t");
            }
        }
        String tab = tabBuilder.toString();
        String attribute = getAttribute(abstractElement);
        if (abstractElement.isComment) {
            builder.append(tab + "<!--" + abstractElement.ownOriginText + "-->\n");
        } else if (abstractElement.isTextNode) {
            builder.append(abstractElement.ownOriginText);
        } else if (abstractElement.isSingleNode) {
            //放入单标签(input等)
            builder.append(tab + "<" + abstractElement.tagName + attribute);
            if (!abstractElement.tagName.startsWith("!") && !abstractElement.tagName.startsWith("?")) {
                //排除特殊标签
                builder.append("/");
            }
            builder.append(">\n");
        } else {
            builder.append(tab + "<" + abstractElement.tagName + attribute + ">\n");
            for (AbstractElement child : abstractElement.childTextList) {
                getHtmlRecursive(child, builder);
            }
            builder.append("\n" + tab + "</" + abstractElement.tagName + ">\n");
        }
    }

    private void getHtmlRecursive(AbstractElement abstractElement, StringBuilder builder) {
        String attribute = getAttribute(abstractElement);
        if (abstractElement.isComment) {
            builder.append("<!--" + abstractElement.ownOriginText + "-->");
        } else if (abstractElement.isTextNode) {
            builder.append(abstractElement.ownOriginText);
        } else if (abstractElement.isSingleNode) {
            //放入单标签(input等)
            builder.append("<" + abstractElement.tagName + attribute);
            if (!abstractElement.tagName.startsWith("!") && !abstractElement.tagName.startsWith("?")) {
                //排除特殊标签
                builder.append("/");
            }
            builder.append(">");
        } else {
            builder.append("<" + abstractElement.tagName + attribute + ">");
            for (AbstractElement child : abstractElement.childTextList) {
                getHtmlRecursive(child, builder);
            }
            builder.append("</" + abstractElement.tagName + ">");
        }
    }

    private String getAttribute(AbstractElement abstractElement) {
        StringBuilder builder = attributeBuilderThreadLocal.get();
        builder.setLength(0);

        Set<Map.Entry<String, String>> entrySet = abstractElement.originAttributes.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            builder.append(" " + entry.getKey());
            if (!entry.getValue().isEmpty()) {
                builder.append("=" + entry.getValue());
            }
        }
        return builder.toString();
    }
}
