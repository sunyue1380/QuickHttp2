package cn.schoolwow.quickhttp.document.parser;

import cn.schoolwow.quickhttp.document.Document;
import cn.schoolwow.quickhttp.document.element.Element;
import org.junit.Assert;
import org.junit.Test;

public class ElementTest {

    @Test
    public void testRemove() {
        String html = "<div><p>p1</p><p>p2</p></div>";
        Element element = Document.parse(html).root();
        Assert.assertEquals(html, element.outerHtml());
        Assert.assertEquals("<p>p1</p><p>p2</p>", element.html());
        Element p = element.selectFirst("p");
        p.remove();
        Assert.assertEquals("<div><p>p2</p></div>", element.outerHtml());
        Assert.assertEquals("<p>p2</p>", element.html());
    }
}
