package cn.schoolwow.quickhttp.response;

import cn.schoolwow.quickhttp.document.Document;
import cn.schoolwow.quickhttp.document.DocumentParser;
import cn.schoolwow.quickhttp.domain.Client;
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
import java.net.ConnectException;
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

    private Client client;

    public ResponseImpl(Client client) {
        this.client = client;
    }

    @Override
    public String url() {
        return client.responseMeta.httpURLConnection.getURL().toString();
    }

    @Override
    public int statusCode() {
        return client.responseMeta.statusCode;
    }

    @Override
    public String statusMessage() {
        return client.responseMeta.statusMessage;
    }

    @Override
    public String charset() {
        return client.responseMeta.charset;
    }

    @Override
    public String contentType() {
        return client.responseMeta.httpURLConnection.getContentType();
    }

    @Override
    public long contentLength() {
        return client.responseMeta.httpURLConnection.getContentLengthLong();
    }

    @Override
    public String filename() {
        String contentDisposition = client.responseMeta.httpURLConnection.getHeaderField("Content-Disposition");
        if (null == contentDisposition) {
            throw new IllegalArgumentException("返回头部无文件名称信息!");
        }
        String fileName = null;
        if (contentDisposition.contains("filename*=")) {
            fileName = contentDisposition.substring(contentDisposition.indexOf("filename*=") + "filename*=".length());
            String charset = fileName.substring(0, fileName.indexOf("''")).replace("\"", "");
            fileName = fileName.substring(fileName.indexOf("''") + 2).replace("\"", "");
            fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), Charset.forName(charset));
            try {
                fileName = URLDecoder.decode(fileName,charset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (contentDisposition.contains("filename=")) {
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
        return client.responseMeta.headerMap.containsKey(name);
    }

    @Override
    public boolean hasHeader(String name, String value) {
        return hasHeader(name) && client.responseMeta.headerMap.get(name).get(0).equals(value);
    }

    @Override
    public List<String> header(String name) {
        return client.responseMeta.headerMap.get(name);
    }

    @Override
    public Map<String, List<String>> headers() {
        return client.responseMeta.headerMap;
    }

    @Override
    public boolean hasCookie(String name) {
        return client.clientConfig.cookieOption.hasCookie(client.responseMeta.topHost,name);
    }

    @Override
    public boolean hasCookie(String name, String value) {
        HttpCookie httpCookie = client.clientConfig.cookieOption.getCookie(client.responseMeta.topHost,name);
        return null!=httpCookie&&httpCookie.getValue().equals(value);
    }

    @Override
    public Response maxDownloadSpeed(int maxDownloadSpeed) {
        if(null!=client.responseMeta.inputStream){
            SpeedLimitInputStream speedLimitInputStream = (SpeedLimitInputStream) client.responseMeta.inputStream;
            speedLimitInputStream.setMaxDownloadSpeed(maxDownloadSpeed);
        }
        return this;
    }

    @Override
    public String body() throws IOException {
        if (null == client.responseMeta.body) {
            bodyAsBytes();
        }
        return new String(client.responseMeta.body,Charset.forName(client.responseMeta.charset));
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
        int startIndex = body.indexOf('(') + 1, endIndex = body.lastIndexOf(')');
        return JSON.parseObject(body.substring(startIndex, endIndex));
    }

    @Override
    public JSONArray jsonpAsJSONArray() throws IOException {
        String body = body();
        int startIndex = body.indexOf('(') + 1, endIndex = body.lastIndexOf(')');
        return JSON.parseArray(body.substring(startIndex, endIndex));
    }

    @Override
    public byte[] bodyAsBytes() throws IOException {
        if(null==client.responseMeta.inputStream){
            throw new IOException("http请求响应输入流获取失败!");
        }
        Path tempFilePath = Files.createTempFile("QuickHttp2.",".response");
        Files.copy(client.responseMeta.inputStream,tempFilePath,StandardCopyOption.REPLACE_EXISTING);
        client.responseMeta.body = Files.readAllBytes(tempFilePath);
        Files.deleteIfExists(tempFilePath);
        close();
        return client.responseMeta.body;
    }

    @Override
    public void bodyAsFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        bodyAsFile(path);
    }

    @Override
    public void bodyAsFile(Path file) throws IOException {
        if (null == client.responseMeta.inputStream) {
            throw new IOException("写入文件失败!输入流为空!url:" + url());
        }

        if (!file.getParent().toFile().exists()) {
            file.getParent().toFile().mkdirs();
        }

        if (null != client.responseMeta.body) {
            if (file.toFile().exists()) {
                Files.write(file, client.responseMeta.body, StandardOpenOption.APPEND);
            } else {
                Files.write(file, client.responseMeta.body, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            }
            return;
        }

        int retryTimes = 1;
        IOException e = null;
        long contentLength = contentLength();
        //处理超时异常,尝试重试
        while (retryTimes <= client.requestMeta.retryTimes) {
            try {
                if (null != client.responseMeta.httpURLConnection.getContentEncoding() || contentLength == -1) {
                    Files.deleteIfExists(file);
                    byte[] buffer = new byte[8192];
                    int length = 0;
                    try (FileOutputStream fos = new FileOutputStream(file.toFile());){
                        while((length=client.responseMeta.inputStream.read(buffer,0,buffer.length))>=0){
                            fos.write(buffer,0,length);
                            if(Thread.currentThread().isInterrupted()){
                                logger.debug("线程中断,文件下载任务停止!");
                                break;
                            }
                        }
                        fos.flush();
                    }
                } else {
                    Set<StandardOpenOption> openOptions = null;
                    if (file.toFile().exists()) {
                        openOptions = EnumSet.of(StandardOpenOption.APPEND);
                    } else {
                        openOptions = EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                    }
                    try (ReadableByteChannel readableByteChannel = Channels.newChannel(client.responseMeta.inputStream);
                         FileChannel fileChannel = FileChannel.open(file, openOptions);){
                        fileChannel.transferFrom(readableByteChannel, Files.size(file), contentLength);
                    }catch (ClosedByInterruptException ex){
                        logger.debug("线程中断,文件下载任务停止!");
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                break;
            } catch (SocketTimeoutException | ConnectException ex) {
                e = ex;
                logger.debug("下载超时,重试{}/{},链接:{}", retryTimes, client.requestMeta.retryTimes, url());
                retryTimes++;
            }
        }
        close();
        if(retryTimes>client.requestMeta.retryTimes&&null!=e){
            throw e;
        }
        if(Thread.currentThread().isInterrupted()){
            logger.debug("线程中断,文件下载任务停止!");
            return;
        }
        if (contentLength() > 0) {
            long fileSize = file.toFile().exists() ? file.toFile().length() : 0;
            //检查是否下载成功
            long expectFileSize = fileSize + contentLength();
            if (!client.responseMeta.headerMap.containsKey("Content-Encoding")&&(!file.toFile().exists() || Files.size(file) != expectFileSize)) {
                logger.warn("文件下载失败,预期大小:{},实际大小:{},路径:{}", expectFileSize, Files.size(file), file);
                return;
            }
        }
    }

    @Override
    public InputStream bodyStream() {
        return client.responseMeta.inputStream;
    }

    @Override
    public Document parse() throws IOException {
        if (client.responseMeta.document == null) {
            if (client.responseMeta.body == null) {
                body();
            }
            client.responseMeta.document = Document.parse(body());
        }
        return client.responseMeta.document;
    }

    @Override
    public DocumentParser parser() throws IOException {
        if (client.responseMeta.documentParser == null) {
            if (client.responseMeta.body == null) {
                body();
            }
            client.responseMeta.documentParser = DocumentParser.parse(body());
        }
        return client.responseMeta.documentParser;
    }

    @Override
    public void close() {
        try {
            if (null != client.responseMeta.inputStream) {
                client.responseMeta.inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        try {
            if (null != client.responseMeta.inputStream) {
                client.responseMeta.inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.responseMeta.httpURLConnection.disconnect();
    }

    @Override
    public ResponseMeta responseMeta() {
        return client.responseMeta;
    }
}