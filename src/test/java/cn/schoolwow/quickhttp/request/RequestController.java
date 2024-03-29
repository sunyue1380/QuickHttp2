package cn.schoolwow.quickhttp.request;

import cn.schoolwow.quickserver.controller.RequestMethod;
import cn.schoolwow.quickserver.controller.annotation.*;
import cn.schoolwow.quickserver.domain.MultipartFile;
import cn.schoolwow.quickserver.request.HttpRequest;
import org.junit.Assert;

@RestController
public class RequestController {
    @RequestMapping("/basicAuth")
    @BasicAuth(username = "quickHttp",password = "123456")
    public boolean basicAuth(){
        return true;
    }

    @RequestMapping(value = "/charset",method = RequestMethod.POST)
    public String charset(
            @RequestBody String data,
            HttpRequest httpRequest
    ) {
        Assert.assertEquals("GBK",httpRequest.charset());
        return data;
    }

    @RequestMapping(value = "/header")
    public void header(
            @RequestHeader(name = "User-Agent") String userAgent,
            @RequestHeader(name = "Referer") String referer,
            @RequestHeader(name = "customerHeader") String customerHeader
    ) {
        Assert.assertEquals("userAgent",userAgent);
        Assert.assertEquals("referer",referer);
        Assert.assertEquals("customerValue",customerHeader);
    }

    @RequestMapping(value = "/cookie")
    public void cookie(
            @CookieValue(name = "cookie") String cookie,
            @CookieValue(name = "cookie1") String cookie1,
            @CookieValue(name = "cookie2") String cookie2
    ) {
        Assert.assertEquals("value",cookie);
        Assert.assertEquals("value",cookie1);
        Assert.assertEquals("value",cookie2);
    }

    @RequestMapping(value = "/sendCookie")
    public void cookie(
            @CookieValue(name = "quickhttp") String quickhttp
    ) {
        Assert.assertEquals("value",quickhttp);
    }

    @RequestMapping(value = "/parameter")
    public void parameter(
            @RequestParam(name = "parameter") String parameter
    ) {
        Assert.assertEquals("value",parameter);
    }

    @RequestMapping(value = "/data",method = RequestMethod.POST)
    public void data(
            @RequestHeader(name = "Content-Type") String contentType,
            @RequestParam(name = "data") String data,
            HttpRequest httpRequest
    ) {
        Assert.assertEquals("application/x-www-form-urlencoded; charset="+httpRequest.charset(),contentType);
        Assert.assertEquals("value",data);
    }

    @RequestMapping(value = "/dataFile",method = RequestMethod.POST)
    public long dataFile(
            @RequestPart(name = "data") String data,
            @RequestPart(name = "file") MultipartFile file
    ) {
        Assert.assertEquals("value",data);
        return file.size;
    }

    @RequestMapping(value = "/dataFiles",method = RequestMethod.POST)
    public long dataFile2(
            @RequestPart(name = "files") MultipartFile[] files
    ) {
        return files.length;
    }

    @RequestMapping(value = "/multipart",method = RequestMethod.POST)
    public void multipart(
            @RequestPart(name = "data") String data,
            @RequestPart(name = "data1") String data1
    ) {
        Assert.assertEquals("value",data);
        Assert.assertEquals("value",data1);
    }

    @RequestMapping(value = "/requestBody",method = RequestMethod.POST)
    public String dataFile(
            @RequestBody String requestBody
    ) {
        return requestBody;
    }

    @RequestMapping(value = "/contentType")
    public String contentType(
            @RequestHeader(name = "Content-Type") String contentType
    ) {
        return contentType;
    }

    @RequestMapping(value = "/clone")
    public String clone(
            @RequestHeader(name = "clone") String clone,
            @RequestHeader(name = "cloneTest") String range
    ) {
        Assert.assertEquals("value",clone);
        return range;
    }
}
