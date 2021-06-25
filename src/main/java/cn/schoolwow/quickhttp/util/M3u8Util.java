package cn.schoolwow.quickhttp.util;

import cn.schoolwow.quickhttp.domain.m3u8.M3u8Type;
import cn.schoolwow.quickhttp.domain.m3u8.MasterPlaylist;
import cn.schoolwow.quickhttp.domain.m3u8.MediaPlaylist;
import cn.schoolwow.quickhttp.domain.m3u8.tag.*;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * m3u8工具类
 * */
public class M3u8Util {
    /**
     * 判断m3u8文件类型
     * */
    public static M3u8Type getM3u8Type(String content){
        if(content.contains("#EXT-X-MEIDA")||content.contains("#EXT-X-STREAM-INF")||content.contains("#EXT-X-I-FRAME-STREAM-INF")){
            return M3u8Type.MasterPlayList;
        }else{
            return M3u8Type.MediaPlayList;
        }
    }

    /**
     * 获取m3u8主播放文件信息
     * @param content m3u8内容
     * */
    public static MasterPlaylist getMasterPlaylist(String url, String content) throws Exception{
        MasterPlaylist masterPlaylist = new MasterPlaylist();
        BufferedReader br = new BufferedReader(new StringReader(content));
        Iterator<String> iterator = br.lines().iterator();
        //相对路径前缀
        String relativePath = url.substring(0,url.lastIndexOf("/")+1);
        while(iterator.hasNext()){
            String line = iterator.next();
            if(line.startsWith("#EXT")){
                String tag = line.contains(":")?line.substring(0,line.indexOf(":")):line;
                String attributes = line.contains(":")?line.substring(line.indexOf(":")+1):"";
                switch(tag){
                    case "#EXT-X-VERSION":{masterPlaylist.VERSION=attributes;}break;
                    case "#EXT-X-MEDIA":{
                        MEDIA media = getInstance(MEDIA.class,attributes);
                        masterPlaylist.mediaList.add(media);
                    }break;
                    case "#EXT-X-STREAM-INF":{
                        STREAMINF streaminf = getInstance(STREAMINF.class,attributes);
                        streaminf.URI = iterator.next();
                        if(!streaminf.URI.startsWith("http")){
                            streaminf.URI = relativePath + streaminf.URI;
                        }
                        masterPlaylist.streaminfList.add(streaminf);
                    }break;
                    case "#EXT-X-I-FRAME-STREAM-INF":{
                        IFRAMESTREAMINF iframestreaminf = getInstance(IFRAMESTREAMINF.class,attributes);
                        masterPlaylist.iframestreaminfList.add(iframestreaminf);
                    }break;
                    case "#EXT-X-SESSION-DATA":{
                        SESSIONDATA sessiondata = getInstance(SESSIONDATA.class,attributes);
                        masterPlaylist.sessiondataList.add(sessiondata);
                    }break;
                    case "#EXT-X-SESSION-KEY":{
                        SESSIONKEY sessionkey = getInstance(SESSIONKEY.class,attributes);
                        masterPlaylist.sessionkeyList.add(sessionkey);
                    }break;
                    case "#EXT-X-INDEPENDENT-SEGMENTS":{
                        masterPlaylist.INDEPENDENT_SEGMENTS = true;
                    }break;
                    case "#EXT-X-START":{
                        START start = getInstance(START.class,attributes);
                        masterPlaylist.start = start;
                    }break;
                    default:{

                    };
                }
            }
        }
        return masterPlaylist;
    }

    /**
     * 获取m3u8媒体播放列表
     * @param url m3u8请求url
     * @param content m3u8文件内容
     * */
    public static MediaPlaylist getMediaPlaylist(String url, String content) throws Exception {
        MediaPlaylist mediaPlaylist = new MediaPlaylist();

        SEGMENT segment = new SEGMENT();
        BYTERANGE lastByteRange = null;
        KEY key = null;
        MAP map = null;

        BufferedReader br = new BufferedReader(new StringReader(content));
        Iterator<String> iterator = br.lines().iterator();
        while(iterator.hasNext()){
            String line = iterator.next();
            if(line.startsWith("#EXT")){
                String tag = line.contains(":")?line.substring(0,line.indexOf(":")):line;
                String attributes = line.contains(":")?line.substring(line.indexOf(":")+1):"";
                switch(tag){
                    case "#EXT-X-VERSION":{
                        mediaPlaylist.VERSION=attributes;
                    }break;
                    case "#EXTINF":{
                        EXTINF extinf = new EXTINF();
                        extinf.duration = attributes.substring(0,attributes.indexOf(","));
                        extinf.title = attributes.substring(attributes.indexOf(",")+1);
                        segment.extinf = extinf;
                    }break;
                    case "#EXT-X-BYTERANGE":{
                        BYTERANGE byterange = new BYTERANGE();
                        if(attributes.contains("@")){
                            byterange.n = Integer.parseInt(attributes.substring(0,attributes.indexOf("@")));
                            byterange.o = Integer.parseInt(attributes.substring(attributes.indexOf("@")+1));
                        }else{
                            byterange.n = Integer.parseInt(attributes);
                            if(null!=lastByteRange){
                                byterange.o = lastByteRange.n + lastByteRange.o + 1;
                            }
                        }
                        lastByteRange = byterange;
                        segment.byterange = byterange;
                    }break;
                    case "#EXT-X-DISCONTINUITY":{
                        segment.DISCONTINUITY = true;
                    }break;
                    case "#EXT-X-KEY":{
                        key = getInstance(KEY.class,attributes);
                    }break;
                    case "#EXT-X-MAP":{
                        map = getInstance(MAP.class,attributes);
                    }break;
                    case "#EXT-X-PROGRAM-DATE-TIME":{
                        segment.PROGRAM_DATE_TIME = attributes;
                    }break;
                    case "#EXT-X-DATERANGE":{
                        DATERANGE daterange = getInstance(DATERANGE.class,attributes);
                        segment.daterange = daterange;
                    }break;
                    case "SCTE35-CMD":
                    case "SCTE35-OUT":
                    case "SCTE35-IN":
                    case "END-ON-NEXT":{};break;
                    case "#EXT-X-TARGETDURATION":{
                        mediaPlaylist.TARGETDURATION = Integer.parseInt(attributes);
                    };break;
                    case "#EXT-X-MEDIA-SEQUENCE":{
                        mediaPlaylist.MEDIA_SEQUENCE = Integer.parseInt(attributes);
                    };break;
                    case "#EXT-X-DISCONTINUITY-SEQUENCE":{
                        mediaPlaylist.DISCONTINUITY_SEQUENCE = Integer.parseInt(attributes);
                    };break;
                    case "#EXT-X-ENDLIST":{
                        return mediaPlaylist;
                    }
                    case "#EXT-X-PLAYLIST-TYPE":{
                        mediaPlaylist.PLAYLIST_TYPE = attributes;
                    };break;
                    case "#EXT-X-I-FRAMES-ONLY":{
                        mediaPlaylist.I_FRAMES_ONLY = true;
                    };break;
                    case "#EXT-X-INDEPENDENT-SEGMENTS":{
                        mediaPlaylist.INDEPENDENT_SEGMENTS = true;
                    };break;
                    case "#EXT-X-START":{
                        START start = getInstance(START.class,attributes);
                        mediaPlaylist.start = start;
                    }break;
                    default:{ };break;
                }
            }else if(line.startsWith("#")){
                //注释 跳过
            }else if(!line.equals("")){
                segment.KEY = key;
                segment.MAP = map;
                segment.URI = line;
                mediaPlaylist.segmentList.add(segment);
                segment = new SEGMENT();
            }
        }
        //补充相对路径
        String relativePath = url.substring(0,url.lastIndexOf("/")+1);
        for(SEGMENT segment2:mediaPlaylist.segmentList){
            if(!segment2.URI.startsWith("http")){
                segment2.URI = relativePath + segment2.URI;
            }
        }
        return mediaPlaylist;
    }

    private static <T> T getInstance(Class<T> clazz, String attributes) throws Exception {
        JSONObject o = new JSONObject(true);
        //从左往右开始扫描
        int currentIndex = 0;
        int startIndex = 0;
        boolean inQuote = false;
        String key = null,value = null;
        while(currentIndex< attributes.length()){
            switch(attributes.charAt(currentIndex)){
                case '=':{
                    key = attributes.substring(startIndex,currentIndex).trim();
                    startIndex = currentIndex + 1;
                }break;
                case '\"':{
                    if(inQuote){
                        inQuote = false;
                        value = attributes.substring(startIndex,currentIndex);
                    }else{
                        inQuote = true;
                        startIndex = currentIndex + 1;
                    }
                    if(currentIndex==attributes.length()-1){
                        o.put(key,value);
                    }
                }break;
                case ',':{
                    if(attributes.charAt(currentIndex-1)!='\"'){
                        o.put(key,attributes.substring(startIndex,currentIndex));
                    }else if(!inQuote){
                        o.put(key,value);
                    }
                    startIndex = currentIndex+1;
                }break;
                default:{

                };
            }
            if(currentIndex==attributes.length()-1&&attributes.charAt(currentIndex)!='\"'){
                o.put(key,attributes.substring(startIndex));
            }
            currentIndex++;
        }

        Field[] fields = getAllField(clazz);
        T instance = clazz.newInstance();
        for(Field field:fields){
            String property = field.getName().replace("_","-");
            if(o.containsKey(property)){
                field.set(instance,o.getString(property));
            }
        }
        return instance;
    }

    /**
     * 获得该类所有字段(包括父类字段)
     * @param clazz 类
     * */
    private static Field[] getAllField(Class clazz){
        List<Field> fieldList = new ArrayList<>();
        Class tempClass = clazz;
        while (null != tempClass) {
            Field[] fields = tempClass.getDeclaredFields();
            Field.setAccessible(fields, true);
            for (Field field : fields) {
                //排除静态变量和常量
                if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                fieldList.add(field);
            }
            tempClass = tempClass.getSuperclass();
            if (null!=tempClass&&"java.lang.Object".equals(tempClass.getName())) {
                break;
            }
        }
        return fieldList.toArray(new Field[0]);
    }
}