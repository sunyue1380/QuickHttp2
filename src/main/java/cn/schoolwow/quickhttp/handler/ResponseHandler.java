package cn.schoolwow.quickhttp.handler;

import cn.schoolwow.quickhttp.document.Document;
import cn.schoolwow.quickhttp.document.element.Element;
import cn.schoolwow.quickhttp.document.element.Elements;
import cn.schoolwow.quickhttp.domain.Client;
import cn.schoolwow.quickhttp.domain.ResponseMeta;
import cn.schoolwow.quickhttp.response.ResponseImpl;
import cn.schoolwow.quickhttp.response.SpeedLimitInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.www.MessageHeader;
import sun.net.www.protocol.https.DelegateHttpsURLConnection;
import sun.net.www.protocol.https.HttpsURLConnectionImpl;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class ResponseHandler implements Handler{
    private static Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    @Override
    public Handler handle(Client client) throws IOException {
        getStatusCode(client);
        getRequestHeader(client);
        getResponseHeader(client);
        getBody(client);
        getCharset(client);
        return new EventSourceHandler();
    }

    /**
     * 获取响应状态码
     * */
    private void getStatusCode(Client client) throws IOException {
        HttpURLConnection httpURLConnection = client.responseMeta.httpURLConnection;
        client.responseMeta.statusCode = httpURLConnection.getResponseCode();
        client.responseMeta.statusMessage = httpURLConnection.getResponseMessage();
        if (null == client.responseMeta.statusMessage) {
            client.responseMeta.statusMessage = "";
        }
        //获取顶级域
        client.responseMeta.topHost = httpURLConnection.getURL().getHost();
        if(client.responseMeta.topHost.contains(".")){
            String substring = client.responseMeta.topHost.substring(0,client.responseMeta.topHost.lastIndexOf('.'));
            if(substring.contains(".")){
                client.responseMeta.topHost = client.responseMeta.topHost.substring(substring.lastIndexOf('.')+1);
            }
        }
    }

    /**
     * 获取请求头部
     */
    private void getRequestHeader(Client client){
        //提取请求头信息
        try {
            Field requestsMessageHeaderField = sun.net.www.protocol.http.HttpURLConnection.class.getDeclaredField("requests");
            requestsMessageHeaderField.setAccessible(true);
            MessageHeader requestsMessageHeader = null;
            if (client.responseMeta.httpURLConnection instanceof HttpsURLConnection) {
                Field delegateField = HttpsURLConnectionImpl.class.getDeclaredField("delegate");
                delegateField.setAccessible(true);
                DelegateHttpsURLConnection delegateHttpsURLConnection = (DelegateHttpsURLConnection) delegateField.get(client.responseMeta.httpURLConnection);
                requestsMessageHeader = (MessageHeader) requestsMessageHeaderField.get(delegateHttpsURLConnection);
            } else {
                requestsMessageHeader = (MessageHeader) requestsMessageHeaderField.get(client.responseMeta.httpURLConnection);
            }
            //添加请求头部
            Map<String, List<String>> headerMap = requestsMessageHeader.getHeaders();
            Set<Map.Entry<String, List<String>>> entrySet = headerMap.entrySet();
            client.requestMeta.headerMap.clear();
            for (Map.Entry<String, List<String>> entry : entrySet) {
                if (null == entry.getValue().get(0)) {
                    client.requestMeta.statusLine = entry.getKey();
                    continue;
                }
                client.requestMeta.headerMap.put(entry.getKey(), entry.getValue());
            }
            //提取Cookie信息
            URI uri = client.responseMeta.httpURLConnection.getURL().toURI();
            client.clientConfig.cookieManager.put(uri, client.responseMeta.httpURLConnection.getHeaderFields());
        }catch (Exception e){
            logger.error("获取实际请求头部信息失败", e);
        }
        client.response = new ResponseImpl(client);
    }

    /**
     * 获取响应头部
     * */
    private void getResponseHeader(Client client){
        try {
            Field responsesMessageHeaderField = sun.net.www.protocol.http.HttpURLConnection.class.getDeclaredField("responses");
            responsesMessageHeaderField.setAccessible(true);
            MessageHeader responsesMessageHeader = null;
            if(client.responseMeta.httpURLConnection instanceof HttpsURLConnection){
                Field delegateField = HttpsURLConnectionImpl.class.getDeclaredField("delegate");
                delegateField.setAccessible(true);
                DelegateHttpsURLConnection delegateHttpsURLConnection = (DelegateHttpsURLConnection) delegateField.get(client.responseMeta.httpURLConnection);
                responsesMessageHeader = (MessageHeader) responsesMessageHeaderField.get(delegateHttpsURLConnection);
            }else {
                responsesMessageHeader = (MessageHeader) responsesMessageHeaderField.get(client.responseMeta.httpURLConnection);
            }
            Map<String,List<String>> headerMap = responsesMessageHeader.getHeaders();
            Set<Map.Entry<String,List<String>>> entrySet = headerMap.entrySet();
            for(Map.Entry<String,List<String>> entry:entrySet){
                if(null==entry.getKey()){
                    client.responseMeta.statusLine = entry.getValue().get(0);
                    continue;
                }
                List<String> values = entry.getValue();
                List<String> newValues = new ArrayList<>(values.size());
                for(int i=0;i<values.size();i++){
                    if(null==values.get(i)){
                        continue;
                    }
                    newValues.add(new String(values.get(i).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
                }
                client.responseMeta.headerMap.put(entry.getKey(),newValues);
                if("Content-Type".equalsIgnoreCase(entry.getKey())){
                    client.responseMeta.contentType = newValues.get(0);
                }
            }
        }catch (Exception e){
            logger.error("获取实际响应头部信息失败", e);
        }
    }

    /**
     * 获取body
     * */
    private void getBody(Client client) throws IOException {
        String contentEncoding = client.responseMeta.httpURLConnection.getContentEncoding();
        InputStream inputStream = client.responseMeta.httpURLConnection.getErrorStream() != null ? client.responseMeta.httpURLConnection.getErrorStream() : client.responseMeta.httpURLConnection.getInputStream();
        if (contentEncoding != null && !contentEncoding.isEmpty()) {
            if (contentEncoding.equals("gzip")) {
                inputStream = new GZIPInputStream(inputStream);
            } else if (contentEncoding.equals("deflate")) {
                inputStream = new InflaterInputStream(inputStream, new Inflater(true));
            }
        }
        client.responseMeta.inputStream = inputStream;
        client.responseMeta.inputStream = new BufferedInputStream(inputStream);
        client.responseMeta.inputStream = new SpeedLimitInputStream(client.responseMeta.inputStream);
    }

    /**
     * 提取编码信息
     */
    private void getCharset(Client client) throws IOException {
        String contentType = client.responseMeta.httpURLConnection.getContentType();
        getCharsetFromContentType(contentType, client.responseMeta);
        if (client.responseMeta.charset == null&&null!=client.responseMeta.inputStream) {
            byte[] bytes = new byte[1024 * 5];
            client.responseMeta.inputStream.mark(bytes.length);
            client.responseMeta.inputStream.read(bytes, 0, bytes.length);
            boolean readFully = (client.responseMeta.inputStream.read() == -1);
            client.responseMeta.inputStream.reset();
            ByteBuffer firstBytes = ByteBuffer.wrap(bytes);
            getCharsetFromBOM(firstBytes, client.responseMeta);
            if (client.responseMeta.charset == null) {
                getCharsetFromMeta(firstBytes, readFully, client.responseMeta);
            }
        }
        if (client.responseMeta.charset == null) {
            client.responseMeta.charset = "utf-8";
        }
    }

    /**
     * 从meta标签里面获取编码信息
     */
    private void getCharsetFromMeta(ByteBuffer byteBuffer, boolean readFully, ResponseMeta responseMeta) {
        String docData = StandardCharsets.UTF_8.decode(byteBuffer).toString();
        //判断是否是HTML或者XML文档
        if (!docData.startsWith("<?xml") && !docData.startsWith("<!DOCTYPE")) {
            return;
        }
        Document doc = Document.parse(docData);
        if (doc.root() == null) {
            //不是HTML文档
            return;
        }
        Elements metaElements = doc.select("meta[http-equiv=content-type], meta[charset]");
        for (Element meta : metaElements) {
            if (meta.hasAttr("http-equiv")) {
                getCharsetFromContentType(meta.attr("content"), responseMeta);
            }
            if (responseMeta.charset == null && meta.hasAttr("charset")) {
                responseMeta.charset = meta.attr("charset");
            }
            break;
        }

        if (responseMeta.charset == null) {
            Element root = doc.root();
            if (doc.root().tagName().equals("?xml") && root.hasAttr("encoding")) {
                responseMeta.charset = root.attr("encoding");
            }
        }
        if (readFully) {
            responseMeta.document = doc;
        }
    }

    /**
     * 从BOM里面获取编码信息
     */
    private void getCharsetFromBOM(ByteBuffer byteBuffer, ResponseMeta responseMeta) throws IOException {
        final Buffer buffer = byteBuffer;
        buffer.mark();
        byte[] bom = new byte[4];
        if (byteBuffer.remaining() >= bom.length) {
            byteBuffer.get(bom);
            buffer.rewind();
        }
        if (bom[0] == 0x00 && bom[1] == 0x00 && bom[2] == (byte) 0xFE && bom[3] == (byte) 0xFF ||
                bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE && bom[2] == 0x00 && bom[3] == 0x00) {
            responseMeta.charset = "utf-32";
        } else if (bom[0] == (byte) 0xFE && bom[1] == (byte) 0xFF ||
                bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE) {
            responseMeta.charset = "utf-16";
        } else if (bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
            responseMeta.charset = "utf-8";
        }
        if (responseMeta.charset != null) {
            responseMeta.inputStream.skip(1);
        }
    }

    /**
     * 从Content-Type头部获取编码信息
     */
    private void getCharsetFromContentType(String contentType, ResponseMeta responseMeta) {
        String prefix = "charset=";
        if (contentType != null && contentType.contains(prefix)) {
            int startIndex = contentType.indexOf(prefix);
            if (startIndex >= 0) {
                int endIndex = contentType.lastIndexOf(';');
                if (endIndex > startIndex) {
                    responseMeta.charset = contentType.substring(startIndex + prefix.length(), endIndex).trim();
                } else if (endIndex < startIndex) {
                    responseMeta.charset = contentType.substring(startIndex + prefix.length()).trim();
                }
            }
        }
    }
}
