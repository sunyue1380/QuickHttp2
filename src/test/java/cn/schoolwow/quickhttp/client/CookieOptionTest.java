package cn.schoolwow.quickhttp.client;

import cn.schoolwow.quickhttp.QuickHttp;
import org.junit.Assert;
import org.junit.Test;

import java.net.HttpCookie;

public class CookieOptionTest {

    @Test
    public void addCookie() {
        String domain = "localhost";
        CookieOption cookieOption = QuickHttp.clientConfig().cookieOption();
        Assert.assertFalse(cookieOption.hasDomainCookie(domain));
        Assert.assertFalse(cookieOption.hasCookie(domain,"quickHttp"));
        {
            HttpCookie httpCookie = new HttpCookie("addHttpCookie","value");
            httpCookie.setDomain(domain);
            cookieOption.addCookie(httpCookie);
            Assert.assertTrue(cookieOption.hasDomainCookie(domain));
            Assert.assertTrue(cookieOption.hasCookie(domain,"addHttpCookie"));
            Assert.assertTrue(cookieOption.hasCookie(domain,"addHttpCookie","value"));
        }
        {
            cookieOption.addCookie(domain,"addCookie","value");
            Assert.assertTrue(cookieOption.hasCookie(domain,"addCookie"));
            Assert.assertTrue(cookieOption.hasCookie(domain,"addCookie","value"));
        }
        {
            cookieOption.addCookie(domain,"/","addCookiePath","value");
            Assert.assertTrue(cookieOption.hasCookie(domain,"addCookiePath"));
            Assert.assertTrue(cookieOption.hasCookie(domain,"addCookiePath","value"));
        }
        {
            cookieOption.addCookieString(domain,"addCookieString=value;addCookieString2=value");
            Assert.assertTrue(cookieOption.hasCookie(domain,"addCookieString"));
            Assert.assertTrue(cookieOption.hasCookie(domain,"addCookieString","value"));
            Assert.assertTrue(cookieOption.hasCookie(domain,"addCookieString2"));
            Assert.assertTrue(cookieOption.hasCookie(domain,"addCookieString2","value"));
        }
        {
            cookieOption.addCookieString(domain,"/","addCookiePathString=value;addCookiePathString2=value");
            Assert.assertTrue(cookieOption.hasCookie(domain,"addCookiePathString"));
            Assert.assertTrue(cookieOption.hasCookie(domain,"addCookiePathString","value"));
            Assert.assertTrue(cookieOption.hasCookie(domain,"addCookiePathString2"));
            Assert.assertTrue(cookieOption.hasCookie(domain,"addCookiePathString2","value"));
        }
    }

    @Test
    public void remove(){
        String domain = "localhost";
        CookieOption cookieOption = QuickHttp.clientConfig().cookieOption();
        Assert.assertFalse(cookieOption.hasCookie(domain,"quickHttp"));

        {
            cookieOption.addCookieString(domain,"addCookieString=value;addCookieString2=value;");
            Assert.assertTrue(cookieOption.hasCookie(domain,"addCookieString","value"));
            Assert.assertTrue(cookieOption.hasCookie(domain,"addCookieString2","value"));
            cookieOption.removeCookie(domain);
            Assert.assertFalse(cookieOption.hasCookie(domain,"addCookieString","value"));
            Assert.assertFalse(cookieOption.hasCookie(domain,"addCookieString2","value"));
        }
        {
            HttpCookie httpCookie = new HttpCookie("addHttpCookie","value");
            httpCookie.setDomain(domain);
            cookieOption.addCookie(httpCookie);
            Assert.assertTrue(cookieOption.hasCookie(domain,"addHttpCookie","value"));
            cookieOption.removeCookie(httpCookie);
            Assert.assertFalse(cookieOption.hasCookie(domain,"addHttpCookie","value"));
        }
        {
            cookieOption.addCookie(domain,"addCookie","value");
            Assert.assertTrue(cookieOption.hasCookie(domain,"addCookie","value"));
            cookieOption.removeCookie(domain,"addCookie");
            Assert.assertFalse(cookieOption.hasCookie(domain,"addCookie","value"));
        }
        {
            if(cookieOption.clearCookieList()){
                Assert.assertEquals(0,cookieOption.getCookieList().size());
            }
        }
    }
}