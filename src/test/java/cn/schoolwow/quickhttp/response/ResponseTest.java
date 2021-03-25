package cn.schoolwow.quickhttp.response;

import cn.schoolwow.quickhttp.QuickHttp;
import cn.schoolwow.quickhttp.request.Request;
import cn.schoolwow.quickserver.QuickServer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResponseTest {
    @BeforeClass
    public static void beforeClass(){
        new Thread(()->{
            try {
                QuickServer.newInstance()
                        .register(ResponseController.class)
                        .port(10003)
                        .start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        QuickHttp.clientConfig().origin("http://127.0.0.1:10003");
    }

    @Test
    public void statusCode() {
        Response response = QuickHttp.connect("/statusCode")
                .method(Request.Method.GET)
                .execute();
        Assert.assertEquals(200,response.statusCode());
        Assert.assertEquals("OK",response.statusMessage());
        Assert.assertEquals("http://127.0.0.1:10003/statusCode",response.url());
    }

    @Test
    public void redirect() {
        Response response = QuickHttp.connect("/redirect")
                .method(Request.Method.GET)
                .execute();
        Assert.assertEquals(200,response.statusCode());
        Assert.assertEquals("OK",response.statusMessage());
        Assert.assertEquals("http://127.0.0.1:10003/statusCode",response.url());
    }

    @Test
    public void charset() throws IOException {
        String data = "这是一个表单参数";
        Response response = QuickHttp.connect("/charset")
                .method(Request.Method.GET)
                .parameter("data",data)
                .execute();
        Assert.assertEquals("GBK",response.charset());
        Assert.assertEquals(data,response.body());
    }

    @Test
    public void contentType() throws IOException {
        Path path = Paths.get(System.getProperty("user.dir")+"/pom.xml");
        Response response = QuickHttp.connect("/contentType")
                .method(Request.Method.POST)
                .data("file",path)
                .acceptEncoding(false)
                .execute();
        Assert.assertEquals("text/xml; charset=utf-8",response.contentType());
        Assert.assertEquals(Files.size(path),response.contentLength());
    }

    @Test
    public void filename() throws IOException {
        Response response = QuickHttp.connect("/filename")
                .execute();
        Assert.assertEquals("中文测试pom.xml",response.filename());
    }

    @Test
    public void acceptRanges() throws IOException {
        Response response = QuickHttp.connect("/acceptRanges")
                .execute();
        Assert.assertTrue(response.acceptRanges());
    }

    @Test
    public void hasHeader() throws IOException {
        Response response = QuickHttp.connect("/hasHeader")
                .execute();
        Assert.assertTrue(response.hasHeader("hasHeader"));
        Assert.assertTrue(response.hasHeader("hasHeader","value"));
    }

    @Test
    public void hasCookie() throws IOException {
        Response response = QuickHttp.connect("/hasCookie")
                .execute();
        Assert.assertTrue(response.hasCookie("hasCookie"));
        Assert.assertTrue(response.hasCookie("hasCookie","value"));
    }

    @Test
    public void bodyAsFile() throws IOException {
        Response response = QuickHttp.connect("/bodyAsFile")
                .acceptEncoding(false)
                .execute();
        Path tempFilePath = Files.createTempFile("QuickHttp2","xml");
        try {
            response.bodyAsFile(tempFilePath);
            Assert.assertTrue(Files.exists(tempFilePath));
            Assert.assertEquals(response.contentLength(),Files.size(tempFilePath));
        }finally {
            Files.deleteIfExists(tempFilePath);
        }
        byte[] bytes = response.bodyAsBytes();
        Assert.assertEquals(response.contentLength(),bytes.length);
    }
}