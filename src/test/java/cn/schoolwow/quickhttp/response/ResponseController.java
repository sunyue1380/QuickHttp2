package cn.schoolwow.quickhttp.response;

import cn.schoolwow.quickserver.controller.RequestMethod;
import cn.schoolwow.quickserver.controller.annotation.RequestMapping;
import cn.schoolwow.quickserver.controller.annotation.RequestParam;
import cn.schoolwow.quickserver.controller.annotation.RequestPart;
import cn.schoolwow.quickserver.controller.annotation.RestController;
import cn.schoolwow.quickserver.domain.MultipartFile;
import cn.schoolwow.quickserver.response.HttpResponse;
import cn.schoolwow.quickserver.util.MIMEUtil;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class ResponseController {
    @RequestMapping(value = "/statusCode")
    public void statusCode() {}

    @RequestMapping(value = "/charset")
    public String charset(
            @RequestParam(name = "data") String data,
            HttpResponse httpResponse
    ) {
        httpResponse.charset("GBK");
        return data;
    }

    @RequestMapping(value = "/contentType",method = RequestMethod.POST)
    public void contentType(
            @RequestPart(name = "file") MultipartFile file,
            HttpResponse httpResponse
    ) throws IOException {
        httpResponse.setContentType(MIMEUtil.getMIMEType(file.suffixFileName));
        httpResponse.getBodyStream().write(file.bytes);
    }

    @RequestMapping(value = "/filename")
    public void filename(
            HttpResponse httpResponse
    ) throws IOException {
        httpResponse.setHeader("Content-Disposition","attachment;filename*=UTF-8''"+ URLEncoder.encode("中文测试","UTF-8")+"pom.xml");
    }
    
    @RequestMapping(value = "/acceptRanges")
    public void acceptRanges(
            HttpResponse httpResponse
    ) throws IOException {
        httpResponse.setHeader("Accept-Ranges","bytes");
    }

    @RequestMapping(value = "/hasHeader")
    public void hasHeader(
            HttpResponse httpResponse
    ) throws IOException {
        httpResponse.setHeader("hasHeader","value");
    }

    @RequestMapping(value = "/hasCookie")
    public void hasCookie(
            HttpResponse httpResponse
    ) throws IOException {
        httpResponse.addCookie(new HttpCookie("hasCookie","value"));
    }

    @RequestMapping(value = "/bodyAsFile")
    public void bodyAsFile(
            HttpResponse httpResponse
    ) throws IOException {
        Path path = Paths.get(System.getProperty("user.dir")+"/pom.xml");
        httpResponse.setContentType(MIMEUtil.getMIMEType("xml"));
        httpResponse.setContentLength(Files.size(path));
        httpResponse.getBodyStream().write(Files.readAllBytes(path));
    }
}
