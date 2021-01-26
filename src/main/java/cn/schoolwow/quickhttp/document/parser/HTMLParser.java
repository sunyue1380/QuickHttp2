package cn.schoolwow.quickhttp.document.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class HTMLParser {
    private Logger logger = LoggerFactory.getLogger(HTMLParser.class);
    private static final String[] singleNodeList = {"br", "hr", "img", "input", "param", "meta", "link", "!doctype", "?xml", "col"};
    private char[] chars; //输入参数
    private int pos = 0; //当前位置
    private int sectionStart = 0; //token起始位置
    private int lastTokenPos = -1; //上一个可能的Token结束位置
    private String currentTagName; //上一个解析到的标签名称
    private State state = State.openingTag;//起始状态
    private List<HTMLToken> tokenList = new ArrayList<>(); //Token列表

    public static List<HTMLToken> parse(String html) {
        return new HTMLParser(html).tokenList;
    }

    private HTMLParser(String html) {
        chars = html.toCharArray();
        parseHTML();
    }

    /**
     * 词法分析
     */
    private void parseHTML() {
        while (pos < chars.length) {
            switch (state) {
                case openingTag: {
                    if (isLastMatch("<!--")) {
                        //<!--comment-->
                        state = State.inComment;
                        addToken(HTMLToken.TokenType.openTag);
                    } else if (isLastMatch("<") && !isNextMatch("!--")) {
                        //<body
                        state = State.inTagName;
                        addToken(HTMLToken.TokenType.openTag);
                    }
                }
                break;
                case inTagName: {
                    if (isNextMatch("/>")) {
                        //<br/>
                        currentTagName = addToken(HTMLToken.TokenType.tagName);
                        state = State.closingTag;
                    } else if (chars[pos] == '>') {
                        //判断是否单标签属性
                        currentTagName = addToken(HTMLToken.TokenType.tagName);
                        if (isSingleNode(currentTagName)) {
                            //<input>
                            state = State.closingTag;
                        } else {
                            //<head>
                            state = State.openTagClosing;
                        }
                    } else if (!Character.isLetterOrDigit((int) chars[pos])) {
                        //非英文字符 <head attribute>
                        currentTagName = addToken(HTMLToken.TokenType.tagName);
                        state = State.inAttribute;
                    }
                }
                break;
                case inComment: {
                    if (isNextMatch("-->")) {
                        //<!--这里是注释--></a>
                        addToken(HTMLToken.TokenType.commentTag);
                        state = State.closingTag;
                    }
                }
                break;
                case inAttribute: {
                    if (chars[pos] == '\"') {
                        state = State.inAttributeDoubleQuote;
                    } else if (chars[pos] == '\'') {
                        state = State.inAttributeSingleQuote;
                    } else if (isNextMatch("/>")) {
                        //<input attribute/>
                        addToken(HTMLToken.TokenType.attribute);
                        state = State.closingTag;
                    } else if (isNextMatch(">")) {
                        //<head attribute>
                        addToken(HTMLToken.TokenType.attribute);
                        if (isSingleNode(currentTagName)) {
                            //<input>
                            state = State.closingTag;
                            if (pos == chars.length - 1) {
                                addToken(HTMLToken.TokenType.closeTag);
                            }
                        } else {
                            //<head>
                            state = State.openTagClosing;
                        }
                    }
                }
                break;
                case inAttributeSingleQuote: {
                    if (pos == chars.length - 1 && lastTokenPos > 0) {
                        pos = lastTokenPos;
                        addToken(HTMLToken.TokenType.attribute);
                        if (isSingleNode(currentTagName)) {
                            state = State.closingTag;
                        } else {
                            state = State.openTagClosing;
                        }
                        lastTokenPos = -1;
                    } else if (chars[pos] == '\'') {
                        state = State.inAttribute;
                    } else if (lastTokenPos == -1 && chars[pos] == '>') {
                        lastTokenPos = pos;
                    }
                }
                break;
                case inAttributeDoubleQuote: {
                    if (pos == chars.length - 1 && lastTokenPos > 0) {
                        pos = lastTokenPos;
                        addToken(HTMLToken.TokenType.attribute);
                        if (isSingleNode(currentTagName)) {
                            state = State.closingTag;
                        } else {
                            state = State.openTagClosing;
                        }
                        lastTokenPos = -1;
                    } else if (chars[pos] == '"') {
                        state = State.inAttribute;
                    } else if (lastTokenPos == -1 && chars[pos] == '>') {
                        lastTokenPos = pos;
                    }
                }
                break;
                case openTagClosing: {
                    if (isNextMatch("</")) {
                        state = State.closingTag;
                    } else if (chars[pos] == '<') {
                        state = State.openingTag;
                    } else if ("style".equals(currentTagName) || "script".equals(currentTagName)) {
                        state = State.inStyleOrScript;
                    } else {
                        state = State.inTextContent;
                    }
                    addToken(HTMLToken.TokenType.openTagClose);
                }
                break;
                case inStyleOrScript: {
                    if (isNextMatch("</script>") || isNextMatch("</style>")) {
                        addToken(HTMLToken.TokenType.textContent);
                        state = State.closingTag;
                    }
                }
                break;
                case inTextContent: {
                    if (isNextMatch("</")) {
                        addToken(HTMLToken.TokenType.textContent);
                        state = State.closingTag;
                    } else if (chars[pos] == '<') {
                        addToken(HTMLToken.TokenType.textContent);
                        state = State.openingTag;
                    }
                }
                break;
                case closingTag: {
                    if (pos == chars.length - 1) {
                        //</html>$
                        addToken(HTMLToken.TokenType.closeTag);
                        break;
                    } else if (isLastMatch(">") && isNextMatch("</")) {
                        //</body></html>
                        addToken(HTMLToken.TokenType.closeTag);
                        state = State.closingTag;
                    } else if (isLastMatch(">") && chars[pos] == '<') {
                        //</body><script>
                        addToken(HTMLToken.TokenType.closeTag);
                        state = State.openingTag;
                    } else if (isLastMatch(">") && chars[pos] != '<') {
                        //</body>aaa
                        addToken(HTMLToken.TokenType.closeTag);
                        state = State.inTextContent;
                    }
                }
                break;
            }
            pos++;
        }
        logger.trace("[Token列表]{}", tokenList.toString());
    }

    /**
     * 添加Token信息
     */
    private String addToken(HTMLToken.TokenType tokenType) {
        HTMLToken token = new HTMLToken();
        token.start = sectionStart;
        token.end = pos;
        token.tokenType = tokenType;
        if (pos == sectionStart) {
            token.value = chars[pos] + "";
        } else {
            int count = token.end - token.start;
            if (pos == chars.length - 1) {
                count++;
            }
            token.value = new String(chars, token.start, count);
        }
        sectionStart = pos;
        tokenList.add(token);
        return token.value;
    }

    /**
     * 上一个字符串是否等于key
     */
    private boolean isLastMatch(String key) {
        if (pos - key.length() < 0) {
            return false;
        }
        int index = 0;
        while (index < key.length() && chars[pos - key.length() + index] == key.charAt(index)) {
            index++;
        }
        return index == key.length();
    }

    /**
     * 下一个字符串是否等于key
     */
    private boolean isNextMatch(String key) {
        if (pos + key.length() > chars.length) {
            return false;
        }
        int index = 0;
        while (index < key.length() && chars[index + pos] == key.charAt(index)) {
            index++;
        }
        return index == key.length();
    }

    /**
     * 当前节点是否是单节点
     */
    private static boolean isSingleNode(String tagName) {
        for (String singleNode : singleNodeList) {
            if (tagName.equalsIgnoreCase(singleNode)) {
                return true;
            }
        }
        return false;
    }

    private enum State {
        /**
         * 在开始标签中
         */
        openingTag,
        /**
         * 在标签名中
         */
        inTagName,
        /**
         * 在标签属性中
         */
        inAttribute,
        /**
         * 在属性的单引号里
         */
        inAttributeSingleQuote,
        /**
         * 在属性的双引号里
         */
        inAttributeDoubleQuote,
        /**
         * 在开始标签结束标签中
         */
        openTagClosing,
        /**
         * 在style标签或者script标签中
         */
        inStyleOrScript,
        /**
         * 在节点文本节点内容中
         */
        inTextContent,
        /**
         * 在关闭标签中
         */
        closingTag,
        /**
         * 在注释中
         */
        inComment;
    }
}
