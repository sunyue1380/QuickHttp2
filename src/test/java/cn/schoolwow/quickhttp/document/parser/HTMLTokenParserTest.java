package cn.schoolwow.quickhttp.document.parser;

import cn.schoolwow.quickhttp.document.element.Element;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HTMLTokenParserTest {
    private Logger logger = LoggerFactory.getLogger(HTMLTokenParser.class);

    @Test
    public void testBasic() {
        String html = "<html></html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Element root = HTMLTokenParser.parse(htmlTokenList);
        Assert.assertEquals("html", root.tagName());
        Assert.assertEquals(html, root.outerHtml());
    }

    @Test
    public void testAttribute() {
        String html = "<html id=\"quote\" class='singleQuote'></html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Element root = HTMLTokenParser.parse(htmlTokenList);
        Assert.assertEquals("html", root.tagName());
        Assert.assertEquals("quote", root.attr("id"));
        Assert.assertEquals("singleQuote", root.attr("class"));
        Assert.assertEquals(html, root.outerHtml());
    }

    @Test
    public void testComment() {
        String html = "<html><!--this is a comment--></html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Element root = HTMLTokenParser.parse(htmlTokenList);
        Assert.assertEquals("html", root.tagName());
        Element commentElement = root.firstChild();
        Assert.assertEquals("this is a comment", commentElement.ownText());
        Assert.assertEquals(html, root.outerHtml());
    }

    @Test
    public void testSingleNode() {
        String html = "<html><input id='block'><br/></html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Element root = HTMLTokenParser.parse(htmlTokenList);
        Assert.assertEquals("block", root.childElement(1).attr("id"));
        Assert.assertEquals("br", root.childElement(2).tagName());
        Assert.assertEquals("<html><input id='block'/><br/></html>", root.outerHtml());
    }

    @Test
    public void testTextNode() {
        String html = "<html>hello<h1>title</h1></html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Element root = HTMLTokenParser.parse(htmlTokenList);
        Assert.assertEquals("hello", root.ownText());
        Assert.assertEquals("title", root.firstChild().ownText());
        Assert.assertEquals(html, root.outerHtml());
    }

    @Test
    public void testScript() {
        String html = "<html><body><script>replace('</div>');</script></body></html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Element root = HTMLTokenParser.parse(htmlTokenList);
        Assert.assertEquals("replace('</div>');", root.firstChild().firstChild().ownText());
        Assert.assertEquals(html, root.outerHtml());
    }

    @Test
    public void testMissing() {
        String html = "<html><body><p>12313<span>21212</body></html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Element root = HTMLTokenParser.parse(htmlTokenList);
        Assert.assertEquals("<html><body><p>12313<span>21212</span></p></body></html>", root.outerHtml());
    }

    @Test
    public void testMissing2() {
        String html = "<html><body></table></body></html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Element root = HTMLTokenParser.parse(htmlTokenList);
        Assert.assertEquals("<html><body></body></html>", root.outerHtml());
    }

    @Test
    public void testInput() {
        String html = "<input value=\"<iframe src='http://player.youku.com/embed/XNTQwMTgxMTE2' allowfullscreen></iframe>\"/>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Element root = HTMLTokenParser.parse(htmlTokenList);
        Assert.assertEquals(html, root.outerHtml());
    }

    @Test
    public void testMultiTextNode() {
        String html = "<span>已连续签到<em>22</em>天，再坚持签到<em>8</em>天，可获得<em>5</em>张下载券</span>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Element root = HTMLTokenParser.parse(htmlTokenList);
        Assert.assertEquals(html, root.outerHtml());
    }

    @Test
    public void testCommentNode() {
        String html = "<!--[if IE ]><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\"><![endif]-->";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Element root = HTMLTokenParser.parse(htmlTokenList);
        Assert.assertEquals(html, root.outerHtml());
    }

    @Test
    public void testUnclosedNode() {
        String html = "<div><a><font>又拍云</font></a><a>黑ICP备09023260号</a></span></div>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Element root = HTMLTokenParser.parse(htmlTokenList);
        Assert.assertEquals("<div><a><font>又拍云</font></a><a>黑ICP备09023260号</a></div>", root.outerHtml());
    }

    @Test
    public void testUnclosedNode2() {
        String html = "<ul><li><a></a><li><a></a><p></p>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Element root = HTMLTokenParser.parse(htmlTokenList);
        Assert.assertEquals("<div><a><font>又拍云</font></a><a>黑ICP备09023260号</a></div>", root.outerHtml());
    }
}
