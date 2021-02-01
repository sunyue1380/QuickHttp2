package cn.schoolwow.quickhttp.client;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Cookie管理
 */
public class CookieOptionImpl implements CookieOption {
    private CookieManager cookieManager;

    public CookieOptionImpl(CookieManager cookieManager) {
        this.cookieManager = cookieManager;
    }

    @Override
    public boolean hasCookie(String domain, String name) {
        List<HttpCookie> httpCookieListStore = cookieManager.getCookieStore().getCookies();
        for (HttpCookie httpCookie : httpCookieListStore) {
            if (httpCookie.getDomain().contains(domain) && httpCookie.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getCookieString(String domain) {
        List<HttpCookie> httpCookieList = getCookieList(domain);
        StringBuilder builder = new StringBuilder();
        for(HttpCookie httpCookie:httpCookieList){
            builder.append(httpCookie.getName()+"="+httpCookie.getValue()+";");
        }
        return builder.toString();
    }

    @Override
    public HttpCookie getCookie(String domain, String name) {
        List<HttpCookie> httpCookieListStore = cookieManager.getCookieStore().getCookies();
        for (HttpCookie httpCookie : httpCookieListStore) {
            if (httpCookie.getDomain().contains(domain) && httpCookie.getName().equals(name)) {
                return httpCookie;
            }
        }
        return null;
    }

    @Override
    public List<HttpCookie> getCookieList(String domain) {
        List<HttpCookie> httpCookieList = new ArrayList<>();
        List<HttpCookie> httpCookieListStore = cookieManager.getCookieStore().getCookies();
        for (HttpCookie httpCookie : httpCookieListStore) {
            if (httpCookie.getDomain().contains(domain)) {
                httpCookieList.add(httpCookie);
            }
        }
        return httpCookieList;
    }

    @Override
    public List<HttpCookie> getCookieList() {
        return cookieManager.getCookieStore().getCookies();
    }

    @Override
    public void addCookieString(String cookie, String domain) {
        if (null == cookie || cookie.isEmpty()) {
            return;
        }
        StringTokenizer st = new StringTokenizer(cookie, ";");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int startIndex = token.indexOf("=");
            String name = token.substring(0, startIndex).trim();
            String value = token.substring(startIndex + 1).trim();
            addCookie(name, value, domain);
        }
    }

    @Override
    public void addCookie(String domain, String name, String value) {
        HttpCookie httpCookie = new HttpCookie(name, value);
        httpCookie.setMaxAge(3600000);
        httpCookie.setDomain(domain);
        httpCookie.setPath("/");
        httpCookie.setVersion(0);
        httpCookie.setDiscard(false);
        addCookie(httpCookie);
    }

    @Override
    public void addCookie(HttpCookie httpCookie) {
        if (!httpCookie.getDomain().startsWith(".")) {
            httpCookie.setDomain("." + httpCookie.getDomain());
        }
        if (httpCookie.getMaxAge() <= 0) {
            httpCookie.setMaxAge(3600);
        }
        try {
            cookieManager.getCookieStore().add(new URI(httpCookie.getDomain()), httpCookie);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addCookie(List<HttpCookie> httpCookieList) {
        for (HttpCookie httpCookie : httpCookieList) {
            addCookie(httpCookie);
        }
    }

    @Override
    public void removeCookie(String domain) {
        cookieManager.getCookieStore().getCookies().removeIf(httpCookie -> httpCookie.getDomain().contains(domain));
    }

    @Override
    public void removeCookie(String domain, String name) {
        cookieManager.getCookieStore().getCookies().removeIf(httpCookie -> httpCookie.getDomain().contains(domain) && httpCookie.getName().equals(name));
    }

    @Override
    public void removeCookie(HttpCookie httpCookie) {
        cookieManager.getCookieStore().getCookies().removeIf(httpCookie1 -> httpCookie1 == httpCookie);
    }

    @Override
    public boolean clearCookieList() {
        return cookieManager.getCookieStore().removeAll();
    }

    @Override
    public void setCookiePolicy(CookiePolicy cookiePolicy) {
        cookieManager.setCookiePolicy(cookiePolicy);
    }

    @Override
    public CookieManager cookieManager() {
        return cookieManager;
    }
}
