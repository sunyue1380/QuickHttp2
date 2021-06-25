package cn.schoolwow.quickhttp.domain.m3u8.tag;

/** I-frame帧定义*/
public class IFRAMESTREAMINF {
    /**路径*/
    public String URI;

    /**带宽,必选*/
    public String BANDWIDTH;

    /**平均切片传输速率*/
    public String AVERAGE_BANDWIDTH;

    /**逗号分隔的格式列表*/
    public String CODECS;

    /**最佳像素方案*/
    public String RESOLUTION;

    /**保护层级,可选值为TYPE-0或者NONE*/
    public String HDCP_LEVEL;

    @Override
    public String toString() {
        return "\n{\n" +
                "路径:" + URI + "\n" +
                "带宽:" + BANDWIDTH + "\n" +
                "平均切片传输速率:" + AVERAGE_BANDWIDTH + "\n" +
                "逗号分隔的格式列表:" + CODECS + "\n" +
                "最佳像素方案:" + RESOLUTION + "\n" +
                "保护层级:" + HDCP_LEVEL + "\n" +
                "}\n";
    }
}
