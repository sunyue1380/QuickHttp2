package cn.schoolwow.quickhttp;

import cn.schoolwow.quickhttp.client.QuickHttpClient;
import cn.schoolwow.quickhttp.request.Request;
import cn.schoolwow.quickhttp.response.Response;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class QuickHttpTest {
    Logger logger = LoggerFactory.getLogger(QuickHttpTest.class);

    @Test
    public void testBaiDu() throws IOException {
        Response response = QuickHttp.connect("https://www.baidu.com").execute();
        Assert.assertEquals(200, response.statusCode());
        Assert.assertEquals("utf-8", response.charset());
        Assert.assertEquals("gzip", response.header("Content-Encoding"));
        Assert.assertEquals(true, QuickHttp.clientConfig().cookieOption().hasCookie(".baidu.com", "H_PS_PSSID"));
    }

    @Test
    public void testDownload() throws IOException {
        QuickHttpClient client = QuickHttp.newQuickHttpClient();
        Response response = client.connect("https://ss0.baidu.com/7Po3dSag_xI4khGko9WTAnF6hhy/zhidao/pic/item/9c16fdfaaf51f3de9ba8ee1194eef01f3a2979a8.jpg").execute();
        Path path = Paths.get(System.getProperty("user.dir") + "/9c16fdfaaf51f3de9ba8ee1194eef01f3a2979a8.jpg");
        response.bodyAsFile(path);
        Assert.assertTrue(Files.exists(path) && Files.size(path) > 0);
        Files.deleteIfExists(path);
    }

    @Test
    public void testIgnoreHttpError() throws IOException {
        Response response = QuickHttp.connect("https://nomads.ncep.noaa.gov/pub/data/nccf/com/gfs/prod/gfs.20210107/00/gfs.t00z.gtg.0p25.f003.grib2gfs.t00z.gtg.0p25.f006.grib2")
                .ignoreHttpErrors(true)
                .method(Request.Method.HEAD)
                .execute();
        Assert.assertEquals(404, response.statusCode());
    }

    @Test
    public void testClone() {
        Request request = QuickHttp.connect("https://www.baidu.com");
        Request requestClone = request.clone();
        Assert.assertEquals(request.requestMeta().url, requestClone.requestMeta().url);
    }
}
