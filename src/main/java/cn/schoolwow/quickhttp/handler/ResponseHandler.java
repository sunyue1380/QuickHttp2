package cn.schoolwow.quickhttp.handler;

import cn.schoolwow.quickhttp.document.Document;
import cn.schoolwow.quickhttp.document.element.Element;
import cn.schoolwow.quickhttp.document.element.Elements;
import cn.schoolwow.quickhttp.domain.MetaWrapper;
import cn.schoolwow.quickhttp.response.ResponseImpl;
import cn.schoolwow.quickhttp.response.SpeedLimitInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.www.MessageHeader;
import sun.net.www.protocol.https.DelegateHttpsURLConnection;
import sun.net.www.protocol.https.HttpsURLConnectionImpl;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
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

public class ResponseHandler extends AbstractHandler{
    private static Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    public ResponseHandler(MetaWrapper metaWrapper) {
        super(metaWrapper);
    }

    @Override
    public Handler handle() throws IOException {
        boolean statusError = false;
        if(requestMeta.ignoreHttpErrors||clientConfig.ignoreHttpErrors){
            try {
                getStatusCode();
            }catch (FileNotFoundException e){
                statusError = true;
                responseMeta.statusCode = 404;
                responseMeta.statusMessage = "Not Found";
            }catch (IOException e){
                statusError = true;
                String message = e.getMessage();
                if(message.startsWith("Server returned HTTP response code: ")){
                    responseMeta.statusCode = Integer.parseInt(message.substring("Server returned HTTP response code: ".length(),message.indexOf(" for URL: ")));
                    responseMeta.statusMessage = "";
                }
            }
        }else{
            getStatusCode();
        }
        getRequestHeader();
        getResponseHeader();
        if(!statusError){
            getBody();
            getCharset();
        }
        return new EventSourceHandler(metaWrapper);
    }

    /**
     * 获取响应状态码
     * @return 是否继续执行
     * */
    private void getStatusCode() throws IOException {
        HttpURLConnection httpURLConnection = responseMeta.httpURLConnection;
        responseMeta.statusCode = httpURLConnection.getResponseCode();
        responseMeta.statusMessage = httpURLConnection.getResponseMessage();
        if (null == responseMeta.statusMessage) {
            responseMeta.statusMessage = "";
        }
        //获取顶级域
        responseMeta.topHost = httpURLConnection.getURL().getHost();
        if(responseMeta.topHost.contains(".")){
            String substring = responseMeta.topHost.substring(0,responseMeta.topHost.lastIndexOf("."));
            if(substring.contains(".")){
                responseMeta.topHost = responseMeta.topHost.substring(substring.lastIndexOf(".")+1);
            }
        }
    }

    /**
     * 获取请求头部
     *
     */
    private void getRequestHeader(){
        //提取请求头信息
        try {
            Field requestsMessageHeaderField = sun.net.www.protocol.http.HttpURLConnection.class.getDeclaredField("requests");
            requestsMessageHeaderField.setAccessible(true);
            MessageHeader requestsMessageHeader = null;
            if (responseMeta.httpURLConnection instanceof HttpsURLConnection) {
                Field delegateField = HttpsURLConnectionImpl.class.getDeclaredField("delegate");
                delegateField.setAccessible(true);
                DelegateHttpsURLConnection delegateHttpsURLConnection = (DelegateHttpsURLConnection) delegateField.get(responseMeta.httpURLConnection);
                requestsMessageHeader = (MessageHeader) requestsMessageHeaderField.get(delegateHttpsURLConnection);
            } else {
                requestsMessageHeader = (MessageHeader) requestsMessageHeaderField.get(responseMeta.httpURLConnection);
            }
            //添加请求头部
            Map<String, List<String>> headerMap = requestsMessageHeader.getHeaders();
            Set<Map.Entry<String, List<String>>> entrySet = headerMap.entrySet();
            requestMeta.headerMap.clear();
            for (Map.Entry<String, List<String>> entry : entrySet) {
                if (null == entry.getValue().get(0)) {
                    requestMeta.statusLine = entry.getKey();
                    continue;
                }
                requestMeta.headerMap.put(entry.getKey(), entry.getValue());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //提取Cookie信息
        try {
            URI uri = responseMeta.httpURLConnection.getURL().toURI();
            clientConfig.cookieManager.put(uri, responseMeta.httpURLConnection.getHeaderFields());
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        metaWrapper.response = new ResponseImpl(requestMeta, responseMeta,clientConfig);
    }

    /**
     * 获取响应头部
     * */
    private void getResponseHeader(){
        try {
            Field responsesMessageHeaderField = sun.net.www.protocol.http.HttpURLConnection.class.getDeclaredField("responses");
            responsesMessageHeaderField.setAccessible(true);
            MessageHeader responsesMessageHeader = null;
            if(responseMeta.httpURLConnection instanceof HttpsURLConnection){
                Field delegateField = HttpsURLConnectionImpl.class.getDeclaredField("delegate");
                delegateField.setAccessible(true);
                DelegateHttpsURLConnection delegateHttpsURLConnection = (DelegateHttpsURLConnection) delegateField.get(responseMeta.httpURLConnection);
                responsesMessageHeader = (MessageHeader) responsesMessageHeaderField.get(delegateHttpsURLConnection);
            }else {
                responsesMessageHeader = (MessageHeader) responsesMessageHeaderField.get(responseMeta.httpURLConnection);
            }
            Map<String,List<String>> headerMap = responsesMessageHeader.getHeaders();
            Set<Map.Entry<String,List<String>>> entrySet = headerMap.entrySet();
            for(Map.Entry<String,List<String>> entry:entrySet){
                if(null==entry.getKey()){
                    responseMeta.statusLine = entry.getValue().get(0);
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
                responseMeta.headerMap.put(entry.getKey(),newValues);
                if("Content-Type".equalsIgnoreCase(entry.getKey())){
                    responseMeta.contentType = newValues.get(0);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取body
     * */
    private void getBody() throws IOException {
        if(!requestMeta.ignoreHttpErrors&&!clientConfig.ignoreHttpErrors&&responseMeta.statusCode>=400){
            logger.warn("[跳过获取请求体]当前状态码无法获取请求体,当前状态码:{}",responseMeta.statusCode);
            return;
        }
        try {
            String contentEncoding = responseMeta.httpURLConnection.getContentEncoding();
            InputStream inputStream = responseMeta.httpURLConnection.getErrorStream() != null ? responseMeta.httpURLConnection.getErrorStream() : responseMeta.httpURLConnection.getInputStream();
            if (contentEncoding != null && !contentEncoding.isEmpty()) {
                if (contentEncoding.equals("gzip")) {
                    inputStream = new GZIPInputStream(inputStream);
                } else if (contentEncoding.equals("deflate")) {
                    inputStream = new InflaterInputStream(inputStream, new Inflater(true));
                }
            }
            responseMeta.inputStream = inputStream;
            responseMeta.inputStream = new BufferedInputStream(inputStream);
            responseMeta.inputStream = new SpeedLimitInputStream(responseMeta.inputStream);
        } catch (IOException e) {
            logger.warn("[读取输入流失败]{}",e.getMessage());
        }
    }

    /**
     * 提取编码信息
     */
    private void getCharset() throws IOException {
        getCharsetFromContentType(responseMeta.httpURLConnection.getContentType());
        if (responseMeta.charset == null&&null!=responseMeta.inputStream) {
            byte[] bytes = new byte[1024 * 5];
            responseMeta.inputStream.mark(bytes.length);
            responseMeta.inputStream.read(bytes, 0, bytes.length);
            boolean readFully = (responseMeta.inputStream.read() == -1);
            responseMeta.inputStream.reset();
            ByteBuffer firstBytes = ByteBuffer.wrap(bytes);
            getCharsetFromBOM(firstBytes);
            if (responseMeta.charset == null) {
                getCharsetFromMeta(firstBytes, readFully);
            }
        }
        if (responseMeta.charset == null) {
            responseMeta.charset = "utf-8";
        }
    }

    /**
     * 从meta标签里面获取编码信息
     */
    private void getCharsetFromMeta(ByteBuffer byteBuffer, boolean readFully) {
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
                getCharsetFromContentType(meta.attr("content"));
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
    private void getCharsetFromBOM(ByteBuffer byteBuffer) throws IOException {
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
    private void getCharsetFromContentType(String contentType) {
        String prefix = "charset=";
        if (contentType != null && contentType.contains(prefix)) {
            int startIndex = contentType.indexOf(prefix);
            if (startIndex >= 0) {
                int endIndex = contentType.lastIndexOf(";");
                if (endIndex > startIndex) {
                    responseMeta.charset = contentType.substring(startIndex + prefix.length(), endIndex).trim();
                } else if (endIndex < startIndex) {
                    responseMeta.charset = contentType.substring(startIndex + prefix.length()).trim();
                }
            }
        }
    }
}
