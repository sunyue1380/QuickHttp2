package cn.schoolwow.quickhttp.client;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.util.List;

/**
 * Cookie管理接口
 */
public interface CookieOption {
    /**
     * 指定domain下是否存在cookie
     *
     * @param domain 域名
     */
    boolean hasDomainCookie(String domain);

    /**
     * cookie是否存在
     *
     * @param domain 域名
     * @param name   cookie名称
     */
    boolean hasCookie(String domain, String name);

    /**
     * cookie是否存在
     *
     * @param domain 域名
     * @param name   cookie名称
     */
    boolean hasCookie(String domain, String name, String value);

    /**
     * 获取指定域名下Cookie头部
     * @param domain 域名
     * */
    String getCookieString(String domain);

    /**
     * 获取cookie
     *
     * @param domain 域名
     * @param name   cookie名称
     */
    HttpCookie getCookie(String domain, String name);

    /**
     * 获取指定域名下的Cookie列表
     *
     * @param domain 域名
     */
    List<HttpCookie> getCookieList(String domain);

    /**
     * 获取所有Cookie
     */
    List<HttpCookie> getCookieList();

    /**
     * 添加Cookie
     *
     * @param domain 域名
     * @param cookie Cookie字段
     */
    void addCookieString(String domain, String cookie);

    /**
     * 添加Cookie
     *
     * @param domain 域名
     * @param path 路径
     * @param cookie Cookie字段
     */
    void addCookieString(String domain, String path, String cookie);

    /**
     * 添加Cookie
     *
     * @param domain 域名
     * @param name   cookie键
     * @param value  cookie值
     */
    void addCookie(String domain, String name, String value);

    /**
     * 添加Cookie
     *
     * @param domain 域名
     * @param path 路径
     * @param name   cookie键
     * @param value  cookie值
     */
    void addCookie(String domain, String path, String name, String value);

    /**
     * 添加Cookie
     *
     * @param httpCookie Cookie对象
     */
    void addCookie(HttpCookie httpCookie);

    /**
     * 添加Cookie列表
     *
     * @param httpCookieList Cookie列表
     */
    void addCookie(List<HttpCookie> httpCookieList);

    /**
     * 删除指定域名下所有Cookie
     *
     * @param domain 域名
     */
    void removeCookie(String domain);

    /**
     * 删除指定域名下的指定Cookie
     *
     * @param domain 域名
     * @param name   Cookie名称
     */
    void removeCookie(String domain, String name);

    /**
     * 删除指定Cookie
     *
     * @param httpCookie httpCookie对象
     */
    void removeCookie(HttpCookie httpCookie);

    /**
     * 清空Cookie列表
     */
    boolean clearCookieList();

    /**
     * 设置Cookie策略
     *
     * @param cookiePolicy cookie策略
     */
    void setCookiePolicy(CookiePolicy cookiePolicy);

    /**
     * 获取CookieManage对象
     */
    CookieManager cookieManager();
}
