package cn.schoolwow.quickhttp.domain.m3u8.tag;

/**播放媒体列表备份源*/
public class STREAMINF extends IFRAMESTREAMINF {
    /**视屏最大帧率*/
    public String FRAME_RATE;

    /**与EXT-X-MEDIA标签的TYPE=AUDIO的GROUP_ID匹配*/
    public String AUDIO;

    /**与EXT-X-MEDIA标签的TYPE=VIDEO的GROUP_ID匹配*/
    public String VIDEO;

    /**与EXT-X-MEDIA标签的TYPE=SUBTITLES的GROUP_ID匹配*/
    public String SUBTITLES;

    /**与EXT-X-MEDIA标签的TYPE=CLOSED_CAPTIONS的GROUP_ID匹配*/
    public String CLOSED_CAPTIONS;

    @Override
    public String toString() {
        return "\n{\n" +
                "路径:" + URI + "\n" +
                "带宽:" + BANDWIDTH + "\n" +
                "平均切片传输速率:" + AVERAGE_BANDWIDTH + "\n" +
                "逗号分隔的格式列表:" + CODECS + "\n" +
                "最佳像素方案:" + RESOLUTION + "\n" +
                "保护层级:" + HDCP_LEVEL + "\n" +
                "视屏最大帧率:" + FRAME_RATE + "\n" +
                "AUDIO:" + AUDIO + "\n" +
                "VIDEO:" + VIDEO + "\n" +
                "SUBTITLES:" + SUBTITLES + "\n" +
                "CLOSED_CAPTIONS:" + CLOSED_CAPTIONS + "\n" +
                "}\n";
    }
}
