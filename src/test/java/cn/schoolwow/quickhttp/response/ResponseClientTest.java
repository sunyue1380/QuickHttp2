package cn.schoolwow.quickhttp.response;

import cn.schoolwow.quickhttp.QuickHttp;
import cn.schoolwow.quickhttp.client.QuickHttpClient;
import cn.schoolwow.quickhttp.request.Request;
import cn.schoolwow.quickserver.QuickServer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResponseClientTest {
    private static QuickHttpClient client = QuickHttp.newQuickHttpClient();
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
        client.clientConfig().origin("http://127.0.0.1:10003");
    }

    @Test
    public void statusCode() throws IOException {
        Response response = client.connect("/statusCode")
                .method(Request.Method.GET)
                .execute();
        Assert.assertEquals(200,response.statusCode());
        Assert.assertEquals("OK",response.statusMessage());
        Assert.assertEquals("http://127.0.0.1:10003/statusCode",response.url());
        response.disconnect();
    }

    @Test
    public void charset() throws IOException {
        String data = "这是一个表单参数";
        Response response = client.connect("/charset")
                .method(Request.Method.GET)
                .parameter("data",data)
                .execute();
        Assert.assertEquals("GBK",response.charset());
        Assert.assertEquals(data,response.body());
        response.disconnect();
    }

    @Test
    public void contentType() throws IOException {
        Path path = Paths.get(System.getProperty("user.dir")+"/pom.xml");
        Response response = client.connect("/contentType")
                .method(Request.Method.POST)
                .data("file",path)
                .acceptEncoding(false)
                .execute();
        Assert.assertEquals("text/xml; charset=utf-8",response.contentType());
        Assert.assertEquals(Files.size(path),response.contentLength());
        response.disconnect();
    }

    @Test
    public void filename() throws IOException {
        Response response = client.connect("/filename")
                .execute();
        Assert.assertEquals("中文测试pom.xml",response.filename());
        response.disconnect();
    }

    @Test
    public void acceptRanges() throws IOException {
        Response response = client.connect("/acceptRanges")
                .execute();
        Assert.assertTrue(response.acceptRanges());
        response.disconnect();
    }

    @Test
    public void hasHeader() throws IOException {
        Response response = client.connect("/hasHeader")
                .execute();
        Assert.assertTrue(response.hasHeader("hasHeader"));
        Assert.assertTrue(response.hasHeader("hasHeader","value"));
        response.disconnect();
    }

    @Test
    public void hasCookie() throws IOException {
        Response response = client.connect("/hasCookie")
                .execute();
        Assert.assertTrue(response.hasCookie("hasCookie"));
        Assert.assertTrue(response.hasCookie("hasCookie","value"));
        response.disconnect();
    }

    @Test
    public void bodyAsFile() throws IOException {
        Response response = client.connect("/bodyAsFile")
                .acceptEncoding(false)
                .execute();
        Path tempFilePath = Files.createTempFile("QuickHttp2","xml");
        try {
            response.bodyAsFile(tempFilePath);
            Assert.assertTrue(Files.exists(tempFilePath));
            Assert.assertEquals(response.contentLength(),Files.size(tempFilePath));
            Assert.assertEquals(response.contentLength(),Files.size(tempFilePath));
            response.disconnect();
        }finally {
            Files.deleteIfExists(tempFilePath);
        }
    }
}