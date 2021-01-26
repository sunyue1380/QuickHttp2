package cn.schoolwow.quickhttp.document;

import cn.schoolwow.quickhttp.document.element.AbstractElement;
import cn.schoolwow.quickhttp.document.parser.AttributeParser;
import cn.schoolwow.quickhttp.document.parser.ElementHandler;
import cn.schoolwow.quickhttp.document.parser.HTMLParser;
import cn.schoolwow.quickhttp.document.parser.HTMLToken;

import java.io.IOException;
import java.util.List;

public class DocumentParser {
    private List<HTMLToken> htmlTokenList;

    public static DocumentParser parse(String html) {
        return new DocumentParser(html);
    }

    private DocumentParser(String html) {
        htmlTokenList = HTMLParser.parse(html);
    }

    /**
     * 语义分析
     */
    public void parse(ElementHandler elementHandler) throws IOException {
        AbstractElement element = null;
        for (HTMLToken htmlToken : htmlTokenList) {
            switch (htmlToken.tokenType) {
                case openTag: {
                    element = new AbstractElement();
                }
                break;
                case tagName: {
                    element.tagName = htmlToken.value.toLowerCase();
                }
                break;
                case commentTag: {
                    element.isComment = true;
                    element.ownOriginText = htmlToken.value;
                }
                break;
                case attribute: {
                    if (!"!DOCTYPE".equals(element.tagName.toUpperCase())) {
                        AttributeParser.parse(htmlToken.value, element.attributes);
                    }
                }
                break;
                case openTagClose: {
                    if (elementHandler.startElement(element)) {
                        return;
                    }
                }
                break;
                case textContent: {
                    if (element != null) {
                        //<!DOCTYPE HTML> 这里是空白 <head>
                        AbstractElement textElement = new AbstractElement();
                        textElement.isTextNode = true;
                        textElement.ownOriginText = htmlToken.value;
                        element.childTextList.add(textElement);
                    }
                }
                break;
                case closeTag: {
                    if (htmlToken.value.equals(">") || htmlToken.value.equals("/>")) {
                        element.isSingleNode = true;
                    }
                }
                break;
            }
        }
    }
}
