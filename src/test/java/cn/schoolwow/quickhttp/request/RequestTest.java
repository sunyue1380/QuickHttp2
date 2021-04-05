package cn.schoolwow.quickhttp.request;

import cn.schoolwow.quickhttp.QuickHttp;
import cn.schoolwow.quickhttp.client.CookieOption;
import cn.schoolwow.quickhttp.response.Response;
import cn.schoolwow.quickserver.QuickServer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpCookie;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RequestTest {
    @BeforeClass
    public static void beforeClass(){
        new Thread(()->{
            try {
                QuickServer.newInstance()
                        .register(RequestController.class)
                        .port(10002)
                        .start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        QuickHttp.clientConfig().origin("http://127.0.0.1:10002");
    }

    @Test
    public void basicAuth() throws IOException {
        Response response = QuickHttp.connect("/basicAuth")
                .method(Request.Method.GET)
                .basicAuth("quickHttp","123456")
                .execute();
        Assert.assertEquals("true",response.body());
    }

    @Test
    public void charset() throws IOException {
        String data = "这是一段GBK编码的文字";
        Response response = QuickHttp.connect("/charset")
                .method(Request.Method.POST)
                .charset("GBK")
                .requestBody(data)
                .execute();
        Assert.assertEquals(data,response.body());
    }

    @Test
    public void header() throws IOException {
        Response response = QuickHttp.connect("/header")
                .userAgent("userAgent")
                .referrer("referer")
                .setHeader("customerHeader","customerValue")
                .ajax()
                .ranges(0,100)
                .execute();
        Assert.assertEquals(200,response.statusCode());
    }

    @Test
    public void cookie() throws IOException {
        {
            Response response = QuickHttp.connect("/cookie")
                    .cookie("cookie=value;")
                    .cookie("cookie1","value")
                    .cookie(new HttpCookie("cookie2","value"))
                    .execute();
            Assert.assertEquals(200,response.statusCode());
        }
        {
            CookieOption cookieOption = QuickHttp.clientConfig().cookieOption();
            cookieOption.addCookie("127.0.0.1","quickhttp","value");
            Response response = QuickHttp.connect("/sendCookie")
                    .execute();
            Assert.assertEquals(200,response.statusCode());
        }
    }

    @Test
    public void parameter() throws IOException {
        Response response = QuickHttp.connect("/parameter")
                .parameter("parameter","value")
                .execute();
        Assert.assertEquals(200,response.statusCode());
    }

    @Test
    public void data() throws IOException {
        Response response = QuickHttp.connect("/data")
                .method(Request.Method.POST)
                .data("data","value")
                .execute();
        Assert.assertEquals(200,response.statusCode());
    }

    @Test
    public void dataFile() throws IOException {
        Path path = Paths.get(System.getProperty("user.dir")+"/pom.xml");
        Response response = QuickHttp.connect("/dataFile")
                .method(Request.Method.POST)
                .data("file", path)
                .data("data","value")
                .execute();
        Assert.assertEquals(Files.size(path)+"",response.body());
    }

    @Test
    public void dataFiles() throws IOException {
        Response response = QuickHttp.connect("/dataFiles")
                .method(Request.Method.POST)
                .data("files",
                        Paths.get(System.getProperty("user.dir")+"/pom.xml"),
                        Paths.get(System.getProperty("user.dir")+"/README.md")
                )
                .execute();
        Assert.assertEquals("2",response.body());
    }

    @Test
    public void multipart() throws IOException {
        Response response = QuickHttp.connect("/multipart")
                .method(Request.Method.POST)
                .contentType(Request.ContentType.MULTIPART_FORMDATA)
                .data("data","value")
                .data("data1","value")
                .execute();
        Assert.assertEquals(200,response.statusCode());
    }

    @Test
    public void requestBody() throws IOException {
        Response response = QuickHttp.connect("/requestBody")
                .method(Request.Method.POST)
                .requestBody("requestBody")
                .execute();
        Assert.assertEquals("requestBody",response.body());
    }

    @Test
    public void cloneTest() throws IOException {
        Request request = QuickHttp.connect("/clone")
                .setHeader("clone","value");
        Response response = request.clone()
                .setHeader("cloneTest","value")
                .execute();
        Assert.assertEquals("value",response.body());
        response = request.clone()
                .setHeader("cloneTest","value2")
                .execute();
        Assert.assertEquals("value2",response.body());
    }
}
