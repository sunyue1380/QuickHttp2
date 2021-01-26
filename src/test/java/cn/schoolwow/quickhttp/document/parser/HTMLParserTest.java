package cn.schoolwow.quickhttp.document.parser;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class HTMLParserTest {

    @Test
    public void testBasic() {
        String html = "<html></html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Assert.assertEquals(4, htmlTokenList.size());
    }

    @Test
    public void testAttribute() {
        String html = "<html id=\"quote\" class='singleQuote'></html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Assert.assertEquals(5, htmlTokenList.size());
    }

    @Test
    public void testComment() {
        String html = "<html><!--this is a comment--></html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Assert.assertEquals(7, htmlTokenList.size());
    }

    @Test
    public void testSingleNode() {
        String html = "<html><input id='block'><br/></html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Assert.assertEquals(11, htmlTokenList.size());
    }

    @Test
    public void testTextNode() {
        String html = "<html>hello<h1>title</h1></html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Assert.assertEquals(10, htmlTokenList.size());
    }

    @Test
    public void testScript() {
        String html = "<html><script>replace('</div>');</script></html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Assert.assertEquals(9, htmlTokenList.size());
    }

    @Test
    public void testScript2() {
        String html = "<script></script>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Assert.assertEquals(4, htmlTokenList.size());
    }

    @Test
    public void testBodyScript() {
        String html = "<html><body><script>replace('</div>');</script></body></html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Assert.assertEquals(13, htmlTokenList.size());
    }

    @Test
    public void testXML() {
        String html = "<?xml version=\"1.0\"?><softCompany></softCompany>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Assert.assertEquals(8, htmlTokenList.size());
    }

    @Test
    public void testComposit() {
        String html = "<meta http-equiv=\"content-type\" content=\"text/html; charset=GBK\" />";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Assert.assertEquals(4, htmlTokenList.size());
    }

    @Test
    public void testMissing() {
        String html = "<html><body><p>12313<span>21212</body></html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Assert.assertEquals(16, htmlTokenList.size());
    }

    @Test
    public void testMissing2() {
        String html = "<html><body><input></body></html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Assert.assertEquals(11, htmlTokenList.size());
    }

    @Test
    public void testInput() {
        String html = "<input value=\"<iframe src='http://player.youku.com/embed/XNTQwMTgxMTE2' allowfullscreen></iframe>\"/>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Assert.assertEquals(4, htmlTokenList.size());
    }

    @Test
    public void testDoctype() {
        String html = "<!DOCTYPE html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Assert.assertEquals(4, htmlTokenList.size());
    }

    @Test
    public void testCRLFAttribute() {
        String html = "<html\r\nkey=value></html>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Assert.assertEquals(5, htmlTokenList.size());
    }

    @Test
    public void testQuote() {
        String html = "<a key=value \"></a>";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Assert.assertEquals(5, htmlTokenList.size());
    }

    @Test
    public void testCommentNode() {
        String html = "<!--[if IE ]><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\"><![endif]-->";
        List<HTMLToken> htmlTokenList = HTMLParser.parse(html);
        Assert.assertEquals(3, htmlTokenList.size());
    }
}
