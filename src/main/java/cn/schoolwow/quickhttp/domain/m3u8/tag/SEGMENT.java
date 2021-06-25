package cn.schoolwow.quickhttp.domain.m3u8.tag;

/**媒体片段*/
public class SEGMENT {
    /**媒体路径*/
    public String URI;

    /**指定媒体片段*/
    public EXTINF extinf;

    /**资源部分截取*/
    public BYTERANGE byterange;

    /**是否存在中断*/
    public boolean DISCONTINUITY;

    /**媒体资源加密*/
    public KEY KEY;

    /**获取媒体初始化块*/
    public MAP MAP;

    /**样本取样时间*/
    public String PROGRAM_DATE_TIME;

    /**日期范围*/
    public DATERANGE daterange;

    @Override
    public String toString() {
        return "\n{\n" +
                "媒体路径:" + URI + "\n" +
                "指定媒体片段:" + extinf + "\n" +
                "资源部分截取:" + byterange + "\n" +
                "是否存在中断:" + DISCONTINUITY + "\n" +
                "媒体资源加密:" + KEY + "\n" +
                "获取媒体初始化块:" + MAP + "\n" +
                "样本取样时间:" + PROGRAM_DATE_TIME + "\n" +
                "日期范围:" + daterange + "\n" +
                "}\n";
    }
}
