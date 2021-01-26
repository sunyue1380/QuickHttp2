package cn.schoolwow.quickhttp.document.parser;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class AttributeParser {
    private static Logger logger = LoggerFactory.getLogger(AttributeParser.class);
    /**
     * 输入参数
     */
    private char[] chars;
    /**
     * 当前位置
     */
    private int pos = 0;
    /**
     * token起始位置
     */
    private int sectionStart = 0;
    /**
     * 当前key
     */
    private String currentKey;
    /**
     * 属性表
     */
    private Map<String, String> attributes;

    public static void parse(String attribute, Map<String, String> attributes) {
        new AttributeParser(attribute, attributes);
    }

    private AttributeParser(String attribute, Map<String, String> attributes) {
        this.attributes = attributes;
        chars = attribute.toCharArray();
        pos = 0;
        sectionStart = 0;
        currentKey = null;
        parseAttribute();
    }

    /**
     * 词法分析
     */
    private void parseAttribute() {
        State state = null;
        //判断初始状态
        if (chars[pos] == ' ') {
            state = State.inSpace;
        } else {
            state = State.inKey;
        }
        pos++;
        while (pos < chars.length) {
            switch (state) {
                case inSpace: {
                    if (isKeyValueStart()) {
                        if (isLastEqual()) {
                            state = State.inValue;
                            sectionStart = pos;
                        } else {
                            if (currentKey != null) {
                                addAttribute(AttributeType.key);
                            }
                            state = State.inKey;
                            sectionStart = pos;
                        }
                    } else if (chars[pos] == '=') {
                        state = State.inEqual;
                    } else if (chars[pos] == '\'') {
                        state = State.inSingleQuoteStart;
                        sectionStart = pos;
                    } else if (chars[pos] == '"') {
                        state = State.inDoubleQuoteStart;
                        sectionStart = pos;
                    } else if (pos == chars.length - 1) {
                        addAttribute(AttributeType.key);
                    }
                }
                break;
                case inKey: {
                    if (pos == chars.length - 1) {
                        currentKey = new String(chars, sectionStart, pos - sectionStart);
                        addAttribute(AttributeType.key);
                    } else if (chars[pos] == ' ') {
                        currentKey = new String(chars, sectionStart, pos - sectionStart);
                        state = State.inSpace;
                    } else if (chars[pos] == '=') {
                        currentKey = new String(chars, sectionStart, pos - sectionStart);
                        state = State.inEqual;
                    }
                }
                break;
                case inEqual: {
                    if (chars[pos] == ' ') {
                        state = State.inSpace;
                    } else if (isKeyValueStart()) {
                        state = State.inValue;
                        sectionStart = pos;
                    } else if (chars[pos] == '\'') {
                        state = State.inSingleQuoteStart;
                        sectionStart = pos;
                    } else if (chars[pos] == '"') {
                        state = State.inDoubleQuoteStart;
                        sectionStart = pos;
                    }
                }
                break;
                case inValue: {
                    if (chars[pos] == ' ' || pos == chars.length - 1) {
                        state = State.inSpace;
                        addAttribute(AttributeType.keyValue);
                    }
                }
                break;
                case inSingleQuoteStart: {
                    if (pos == chars.length - 1) {
                        addAttribute(AttributeType.quoteKeyValue);
                    } else if (chars[pos] == '\'') {
                        state = State.inSingleQuoteEnd;
                    }
                }
                break;
                case inSingleQuoteEnd: {
                    if (pos == chars.length - 1) {
                        addAttribute(AttributeType.quoteKeyValue);
                    } else if (chars[pos] == ' ') {
                        state = State.inSpace;
                        addAttribute(AttributeType.quoteKeyValue);
                    }
                }
                break;
                case inDoubleQuoteStart: {
                    if (pos == chars.length - 1) {
                        addAttribute(AttributeType.quoteKeyValue);
                    } else if (chars[pos] == '"') {
                        state = State.inDoubleQuoteEnd;
                    }
                }
                break;
                case inDoubleQuoteEnd: {
                    if (chars[pos - 1] == '\"') {
                        addAttribute(AttributeType.quoteKeyValue);
                    }
                    if (chars[pos] == ' ') {
                        state = State.inSpace;
                    } else if (isKeyValueStart()) {
                        //curname="ygp"data="{'pid': '73868', 'platform': 'pc'}"
                        state = State.inKey;
                    }
                }
                break;
            }
            pos++;
        }
        logger.trace("[属性列表]{}", JSON.toJSONString(attributes));
    }

    private void addAttribute(AttributeType attributeType) {
        int count = pos - sectionStart;
        if (pos == chars.length - 1 && chars[pos] != ' ') {
            count++;
        }
        String value = new String(chars, sectionStart, count);
        if (value.charAt(value.length() - 1) == '=') {
            value = value.substring(0, value.length() - 1);
        }
        //有些不规则的属性语法,忽略掉
        if (currentKey == null) {
            return;
        }
        switch (attributeType) {
            case key: {
                attributes.put(value.trim(), "");
            }
            break;
            case keyValue:
            case quoteKeyValue: {
                attributes.put(currentKey.trim(), value);
            }
            break;
        }
        currentKey = null;
        sectionStart = pos;
    }

    private boolean isLastEqual() {
        if (pos == 0) {
            return false;
        }
        int last = pos - 1;
        while (last > 0 && chars[last] == ' ') {
            last--;
        }
        return chars[last] == '=';
    }

    private boolean isQuoteStartEnd() {
        return chars[pos] == '"' || chars[pos] == '\'';
    }

    private boolean isKeyValueStart() {
        return chars[pos] == '_' || Character.isLetterOrDigit(chars[pos]);
    }

    private enum AttributeType {
        key, keyValue, quoteKeyValue;
    }

    private enum State {
        /**
         * 在属性名中
         */
        inKey,
        /**
         * 在属性值中
         */
        inValue,
        /**
         * 单引号开始
         */
        inSingleQuoteStart,
        /**
         * 双引号开始
         */
        inDoubleQuoteStart,
        /**
         * 单引号结束
         */
        inSingleQuoteEnd,
        /**
         * 双引号结束
         */
        inDoubleQuoteEnd,
        /**
         * 在空格中
         */
        inSpace,
        /**
         * 等于符号
         */
        inEqual;
    }
}
