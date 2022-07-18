package cn.schoolwow.quickhttp.client;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
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
    public boolean hasDomainCookie(String domain) {
        List<HttpCookie> httpCookieListStore = cookieManager.getCookieStore().getCookies();
        for (HttpCookie httpCookie : httpCookieListStore) {
            if (httpCookie.getDomain().contains(domain)) {
                return true;
            }
        }
        return false;
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
    public boolean hasCookie(String domain, String name, String value) {
        List<HttpCookie> httpCookieListStore = cookieManager.getCookieStore().getCookies();
        for (HttpCookie httpCookie : httpCookieListStore) {
            if (httpCookie.getDomain().contains(domain)
                    && httpCookie.getName().equals(name)
                    && httpCookie.getValue().equals(value)
            ) {
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
    public void addCookieString(String domain, String cookie) {
        addCookieString(domain,"/",cookie);
    }

    @Override
    public void addCookieString(String domain, String path, String cookie) {
        if (null == cookie || cookie.isEmpty()) {
            return;
        }
        StringTokenizer st = new StringTokenizer(cookie, ";");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int startIndex = token.indexOf("=");
            String name = token.substring(0, startIndex).trim();
            String value = token.substring(startIndex + 1).trim();
            addCookie(domain, path, name, value);
        }
    }

    @Override
    public void addCookie(String domain, String name, String value) {
        addCookie(domain,"/",name,value);
    }

    @Override
    public void addCookie(String domain, String path, String name, String value) {
        HttpCookie httpCookie = new HttpCookie(name, value);
        httpCookie.setMaxAge(3600000);
        httpCookie.setDomain(domain);
        httpCookie.setPath(path);
        httpCookie.setVersion(0);
        httpCookie.setDiscard(false);
        addCookie(httpCookie);
    }

    @Override
    public void addCookie(HttpCookie httpCookie) {
        String domain = httpCookie.getDomain();
        if(null==domain||domain.isEmpty()){
            throw new IllegalArgumentException("cookie的domain属性不能为空!");
        }
        if (domain.startsWith(".")) {
            httpCookie.setDomain("." + domain);
        }
        if(null==httpCookie.getPath()||httpCookie.getPath().isEmpty()){
            httpCookie.setPath("/");
        }
        if (httpCookie.getMaxAge() <= 0) {
            httpCookie.setMaxAge(3600);
        }
        List<HttpCookie> httpCookieList = cookieManager.getCookieStore().getCookies();
        boolean httpCookieExists = false;
        for(HttpCookie httpCookie1:httpCookieList){
            if(httpCookie.getDomain().equalsIgnoreCase(httpCookie1.getDomain())&&httpCookie.getName().equalsIgnoreCase(httpCookie1.getName())){
                httpCookie1.setValue(httpCookie.getValue());
                httpCookieExists = true;
                break;
            }
        }
        if(!httpCookieExists){
            cookieManager.getCookieStore().add(null, httpCookie);
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
        List<HttpCookie> httpCookieList = getCookieList(domain);
        for(HttpCookie httpCookie:httpCookieList){
            cookieManager.getCookieStore().remove(null,httpCookie);
        }
    }

    @Override
    public void removeCookie(String domain, String name) {
        HttpCookie httpCookie = getCookie(domain,name);
        cookieManager.getCookieStore().remove(null,httpCookie);
    }

    @Override
    public void removeCookie(HttpCookie httpCookie) {
        cookieManager.getCookieStore().remove(null,httpCookie);
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
