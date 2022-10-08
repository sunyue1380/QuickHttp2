package cn.schoolwow.quickhttp.response;

import cn.schoolwow.quickhttp.document.Document;
import cn.schoolwow.quickhttp.document.DocumentParser;
import cn.schoolwow.quickhttp.domain.ClientConfig;
import cn.schoolwow.quickhttp.domain.RequestMeta;
import cn.schoolwow.quickhttp.domain.ResponseMeta;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResponseImpl implements Response {
    private Logger logger = LoggerFactory.getLogger(ResponseImpl.class);
    private RequestMeta requestMeta;
    private ResponseMeta responseMeta;
    private ClientConfig clientConfig;

    public ResponseImpl(RequestMeta requestMeta, ResponseMeta responseMeta, ClientConfig clientConfig) {
        this.requestMeta = requestMeta;
        this.responseMeta = responseMeta;
        this.clientConfig = clientConfig;
    }

    @Override
    public String url() {
        return responseMeta.httpURLConnection.getURL().toString();
    }

    @Override
    public int statusCode() {
        return responseMeta.statusCode;
    }

    @Override
    public String statusMessage() {
        return responseMeta.statusMessage;
    }

    @Override
    public String charset() {
        return responseMeta.charset;
    }

    @Override
    public String contentType() {
        return responseMeta.httpURLConnection.getContentType();
    }

    @Override
    public long contentLength() {
        return responseMeta.httpURLConnection.getContentLengthLong();
    }

    @Override
    public String filename() {
        String contentDisposition = responseMeta.httpURLConnection.getHeaderField("Content-Disposition");
        if (null == contentDisposition) {
            throw new IllegalArgumentException("返回头部无文件名称信息!");
        }
        String fileName = null;
        if (contentDisposition.indexOf("filename*=") > 0) {
            fileName = contentDisposition.substring(contentDisposition.indexOf("filename*=") + "filename*=".length());
            String charset = fileName.substring(0, fileName.indexOf("''")).replace("\"", "");
            fileName = fileName.substring(fileName.indexOf("''") + 2).replace("\"", "");
            fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), Charset.forName(charset));
            try {
                fileName = URLDecoder.decode(fileName,charset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (contentDisposition.indexOf("filename=") > 0) {
            fileName = contentDisposition.substring(contentDisposition.indexOf("filename=") + "filename=".length());
            fileName = fileName.replace("\"", "").trim();
        }
        return fileName;
    }

    @Override
    public boolean acceptRanges() {
        return hasHeader("Accept-Ranges", "bytes");
    }

    @Override
    public String contentEncoding() {
        List<String> values = header("Content-Encoding");
        return null==values?null:values.get(0);
    }

    @Override
    public boolean hasHeader(String name) {
        return responseMeta.headerMap.containsKey(name);
    }

    @Override
    public boolean hasHeader(String name, String value) {
        return hasHeader(name) && responseMeta.headerMap.get(name).get(0).equals(value);
    }

    @Override
    public List<String> header(String name) {
        return responseMeta.headerMap.get(name);
    }

    @Override
    public Map<String, List<String>> headers() {
        return responseMeta.headerMap;
    }

    @Override
    public boolean hasCookie(String name) {
        return clientConfig.cookieOption.hasCookie(responseMeta.topHost,name);
    }

    @Override
    public boolean hasCookie(String name, String value) {
        HttpCookie httpCookie = clientConfig.cookieOption.getCookie(responseMeta.topHost,name);
        return null!=httpCookie&&httpCookie.getValue().equals(value);
    }

    @Override
    public Response maxDownloadSpeed(int maxDownloadSpeed) {
        if(null!=responseMeta.inputStream){
            SpeedLimitInputStream speedLimitInputStream = (SpeedLimitInputStream) responseMeta.inputStream;
            speedLimitInputStream.setMaxDownloadSpeed(maxDownloadSpeed);
        }
        return this;
    }

    @Override
    public String body() throws IOException {
        if (null == responseMeta.body) {
            bodyAsBytes();
        }
        return new String(responseMeta.body,Charset.forName(responseMeta.charset));
    }

    @Override
    public JSONObject bodyAsJSONObject() throws IOException {
        JSONObject object = JSON.parseObject(body());
        return object;
    }

    @Override
    public JSONArray bodyAsJSONArray() throws IOException {
        JSONArray array = JSON.parseArray(body());
        return array;
    }

    @Override
    public JSONObject jsonpAsJSONObject() throws IOException {
        String body = body();
        int startIndex = body.indexOf("(") + 1, endIndex = body.lastIndexOf(")");
        return JSON.parseObject(body.substring(startIndex, endIndex));
    }

    @Override
    public JSONArray jsonpAsJSONArray() throws IOException {
        String body = body();
        int startIndex = body.indexOf("(") + 1, endIndex = body.lastIndexOf(")");
        return JSON.parseArray(body.substring(startIndex, endIndex));
    }

    @Override
    public byte[] bodyAsBytes() throws IOException {
        if(null==responseMeta.inputStream){
            throw new IOException("http请求响应输入流获取失败!");
        }
        Path tempFilePath = Files.createTempFile("QuickHttp2.",".response");
        Files.copy(responseMeta.inputStream,tempFilePath,StandardCopyOption.REPLACE_EXISTING);
        responseMeta.body = Files.readAllBytes(tempFilePath);
        Files.deleteIfExists(tempFilePath);
        close();
        return responseMeta.body;
    }

    @Override
    public void bodyAsFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        bodyAsFile(path);
    }

    @Override
    public void bodyAsFile(Path file) throws IOException {
        if (null == responseMeta.inputStream) {
            throw new IOException("写入文件失败!输入流为空!url:" + url());
        }

        if (!Files.exists(file.getParent())) {
            Files.createDirectories(file.getParent());
        }

        long fileSize = Files.exists(file) ? Files.size(file) : 0;
        if (null != responseMeta.body) {
            if (Files.exists(file)) {
                Files.write(file, responseMeta.body, StandardOpenOption.APPEND);
            } else {
                Files.write(file, responseMeta.body, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            }
        } else {
            //处理超时异常,尝试重试
            int retryTimes = 1;
            long contentLength = contentLength();
            while (retryTimes <= requestMeta.retryTimes) {
                try {
                    if (null != responseMeta.httpURLConnection.getContentEncoding() || contentLength == -1) {
                        Files.deleteIfExists(file);
                        byte[] buffer = new byte[8192];
                        int length = 0;
                        FileOutputStream fos = new FileOutputStream(file.toFile());
                        while((length=responseMeta.inputStream.read(buffer,0,buffer.length))>=0){
                            fos.write(buffer,0,length);
                            if(Thread.currentThread().isInterrupted()){
                                logger.debug("[线程中断]文件下载任务停止!");
                                break;
                            }
                        }
                        fos.flush();
                        fos.close();
                    } else {
                        ReadableByteChannel readableByteChannel = Channels.newChannel(responseMeta.inputStream);
                        Set<StandardOpenOption> openOptions = null;
                        if (Files.exists(file)) {
                            openOptions = EnumSet.of(StandardOpenOption.APPEND);
                        } else {
                            openOptions = EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                        }
                        FileChannel fileChannel = FileChannel.open(file, openOptions);
                        try {
                            fileChannel.transferFrom(readableByteChannel, Files.size(file), contentLength);
                        }catch (ClosedByInterruptException e){
                            logger.debug("[线程中断]文件下载任务停止!");
                            Thread.currentThread().interrupt();
                            break;
                        }
                        fileChannel.close();
                    }
                    break;
                } catch (SocketTimeoutException e) {
                    logger.warn("[下载超时]重试{}/{},原因:{},链接:{}", retryTimes, requestMeta.retryTimes, e.getMessage(), url());
                    retryTimes++;
                }
            }
        }
        close();
        if(Thread.currentThread().isInterrupted()){
            return;
        }
        if (contentLength() > 0) {
            //检查是否下载成功
            long expectFileSize = fileSize + contentLength();
            if (!responseMeta.headerMap.containsKey("Content-Encoding")&&(Files.notExists(file) || Files.size(file) != expectFileSize)) {
                logger.warn("[文件下载失败]预期大小:{},实际大小:{},路径:{}", expectFileSize, Files.size(file), file);
                return;
            }
        }
    }

    @Override
    public InputStream bodyStream() {
        return responseMeta.inputStream;
    }

    @Override
    public Document parse() throws IOException {
        if (responseMeta.document == null) {
            if (responseMeta.body == null) {
                body();
            }
            responseMeta.document = Document.parse(body());
        }
        return responseMeta.document;
    }

    @Override
    public DocumentParser parser() throws IOException {
        if (responseMeta.documentParser == null) {
            if (responseMeta.body == null) {
                body();
            }
            responseMeta.documentParser = DocumentParser.parse(body());
        }
        return responseMeta.documentParser;
    }

    @Override
    public void close() {
        try {
            if (null != responseMeta.inputStream) {
                responseMeta.inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        try {
            if (null != responseMeta.inputStream) {
                responseMeta.inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        responseMeta.httpURLConnection.disconnect();
    }

    @Override
    public ResponseMeta responseMeta() {
        return responseMeta;
    }
}