package cn.schoolwow.quickhttp.document.parser;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class AttributeParserTest {
    private Logger logger = LoggerFactory.getLogger(AttributeParser.class);
    Map<String, String> attibuteMap = new HashMap<>();

    @Test
    public void testBasic() {
        String attribute = "id=\"quote\" class='singleQuote'";
        AttributeParser.parse(attribute, attibuteMap);
        logger.info("[属性]{}", attibuteMap);
        Assert.assertEquals(2, attibuteMap.size());
        Assert.assertEquals(true, attibuteMap.containsKey("id"));
        Assert.assertEquals(true, attibuteMap.containsKey("class"));
        Assert.assertEquals("\"quote\"", attibuteMap.get("id"));
        Assert.assertEquals("'singleQuote'", attibuteMap.get("class"));
    }

    @Test
    public void testBasic2() {
        String attribute = " style= \"width:100px; \"";
        AttributeParser.parse(attribute, attibuteMap);
        logger.info("[属性]{}", attibuteMap);
        Assert.assertEquals(1, attibuteMap.size());
        Assert.assertEquals(true, attibuteMap.containsKey("style"));
        Assert.assertEquals("\"width:100px; \"", attibuteMap.get("style"));
    }

    @Test
    public void testBasic3() {
        String attribute = "type=password";
        AttributeParser.parse(attribute, attibuteMap);
        logger.info("[属性]{}", attibuteMap);
        Assert.assertEquals(1, attibuteMap.size());
        Assert.assertEquals(true, attibuteMap.containsKey("type"));
        Assert.assertEquals("password", attibuteMap.get("type"));
    }

    @Test
    public void testOne() {
        String attribute = "disabled name = 'username'";
        AttributeParser.parse(attribute, attibuteMap);
        logger.info("[属性]{}", attibuteMap);
        Assert.assertEquals(2, attibuteMap.size());
        Assert.assertEquals(true, attibuteMap.containsKey("disabled"));
        Assert.assertEquals(true, attibuteMap.containsKey("name"));
        Assert.assertEquals("", attibuteMap.get("disabled"));
        Assert.assertEquals("'username'", attibuteMap.get("name"));
    }

    @Test
    public void testSpace() {
        String attribute = "name = \"user name\"";
        AttributeParser.parse(attribute, attibuteMap);
        logger.info("[属性]{}", attibuteMap);
        Assert.assertEquals(1, attibuteMap.size());
        Assert.assertEquals(true, attibuteMap.containsKey("name"));
        Assert.assertEquals("\"user name\"", attibuteMap.get("name"));
    }

    @Test
    public void testSpace2() {
        String attribute = " ng-click = hello();";
        AttributeParser.parse(attribute, attibuteMap);
        logger.info("[属性]{}", attibuteMap);
        Assert.assertEquals(1, attibuteMap.size());
        Assert.assertEquals(true, attibuteMap.containsKey("ng-click"));
        Assert.assertEquals("hello();", attibuteMap.get("ng-click"));
    }

    @Test
    public void testAll() {
        String attribute = "disabled id=\"quote\" class = \"ha ha\" type=password ng-click = hello();";
        AttributeParser.parse(attribute, attibuteMap);
        logger.info("[属性]{}", attibuteMap);
        Assert.assertEquals(5, attibuteMap.size());
    }

    @Test
    public void testQuoteEnd() {
        String attribute = " curname=\"ygp\"data=\"{'pid': '73868', 'platform': 'pc'}\"";
        AttributeParser.parse(attribute, attibuteMap);
        logger.info("[属性]{}", attibuteMap);
        Assert.assertEquals(2, attibuteMap.size());
    }

    @Test
    public void testQuoteEnd2() {
        String attribute = " http-equiv=\"content-type\" content=\"text/html; charset=GBK\" ";
        AttributeParser.parse(attribute, attibuteMap);
        logger.info("[属性]{}", attibuteMap);
        Assert.assertEquals(2, attibuteMap.size());
    }
}
