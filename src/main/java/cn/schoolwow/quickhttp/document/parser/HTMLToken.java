package cn.schoolwow.quickhttp.document.parser;

public class HTMLToken {
    public int start;
    public int end;
    public String value;
    public TokenType tokenType;

    public enum TokenType {
        openTag("开始标签"),
        tagName("标签名称"),
        attribute("标签属性"),
        openTagClose("开始标签结束"),
        textContent("标签文本内容"),
        closeTag("结束标签"),
        commentTag("注释标签");

        private String name;

        TokenType(String name) {
            this.name = name;
        }
    }

    @Override
    public String toString() {
        return "(" + start + "," + end + ")" + this.tokenType.name().toUpperCase() + "-" + value;
    }
}
