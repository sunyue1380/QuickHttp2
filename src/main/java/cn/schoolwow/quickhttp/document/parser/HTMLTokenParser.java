package cn.schoolwow.quickhttp.document.parser;

import cn.schoolwow.quickhttp.document.element.AbstractElement;
import cn.schoolwow.quickhttp.document.element.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class HTMLTokenParser {
    private Logger logger = LoggerFactory.getLogger(HTMLTokenParser.class);
    private List<HTMLToken> htmlTokenList;
    private AbstractElement root = new AbstractElement();

    public static Element parse(List<HTMLToken> htmlTokenList) {
        Element root = new HTMLTokenParser(htmlTokenList).root;
        return root;
    }

    private HTMLTokenParser(List<HTMLToken> htmlTokenList) {
        this.htmlTokenList = htmlTokenList;
        this.root.tagName = "ROOT";
        parse();
        if (this.root.childList.size() == 1) {
            this.root = this.root.childList.get(0);
        }
    }

    /**
     * 词法分析
     */
    private void parse() {
        AbstractElement current = root;
        for (int i = 0; i < htmlTokenList.size(); i++) {
            HTMLToken htmlToken = htmlTokenList.get(i);
            switch (htmlToken.tokenType) {
                case openTag: {
                    AbstractElement newElement = new AbstractElement();
                    newElement.parent = current;
                    newElement.parent.childList.add(newElement);
                    newElement.parent.childTextList.add(newElement);
                    current = newElement;
                }
                break;
                case tagName: {
                    current.tagName = htmlToken.value;
                }
                break;
                case commentTag: {
                    //<!--这里是注释-->
                    current.isComment = true;
                    current.ownOriginText = htmlToken.value;
                    current.ownText = escapeOwnOriginText(current.ownOriginText);
                }
                break;
                case attribute: {
                    AttributeParser.parse(htmlToken.value, current.originAttributes);
                    Set<Map.Entry<String, String>> entrySet = current.originAttributes.entrySet();
                    for (Map.Entry<String, String> entry : entrySet) {
                        current.attributes.put(entry.getKey(), entry.getValue()
                                .replace("\"", "")
                                .replace("'", "")
                        );
                    }
                    current.originAttributeText = htmlToken.value;
                }
                break;
                case openTagClose: {
                }
                break;
                case textContent: {
                    if (null != current) {
                        //<!DOCTYPE HTML> 这里是空白 <head>
                        AbstractElement textElement = new AbstractElement();
                        textElement.isTextNode = true;
                        textElement.ownOriginText = htmlToken.value.trim();
                        textElement.ownText = escapeOwnOriginText(textElement.ownOriginText);
                        textElement.parent = current;
                        current.childTextList.add(textElement);
                    }
                }
                break;
                case closeTag: {
                    if (htmlToken.value.equals(">") || htmlToken.value.equals("/>")) {
                        current.isSingleNode = true;
                        current = current.parent;
                    } else if ("-->".equals(htmlToken.value) || ("</" + current.tagName + ">").equals(htmlToken.value)) {
                        //检查结束标签标签名
                        current = current.parent;
                    } else {
                        //TODO 无法匹配的标签
                    }
                }
                break;
            }
        }
    }

    private String escapeOwnOriginText(String ownOriginText) {
        return ownOriginText.replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&nbsp;", " ");
    }
}
