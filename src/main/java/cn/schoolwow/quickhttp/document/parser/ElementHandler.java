package cn.schoolwow.quickhttp.document.parser;

import cn.schoolwow.quickhttp.document.element.Element;

import java.io.IOException;

public interface ElementHandler {
    /**
     * 标签开始时
     *
     * @param element 标签
     * @return 是否已经处理
     */
    boolean startElement(Element element) throws IOException;
}
