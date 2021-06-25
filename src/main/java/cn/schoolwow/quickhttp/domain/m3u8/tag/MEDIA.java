package cn.schoolwow.quickhttp.domain.m3u8.tag;

/**播放媒体资源列表*/
public class MEDIA {
    /**类型,必选,可以为AUDIO,VIDEO,SUBTITLES,CLOSED-CAPTIONS*/
    public String TYPE;

    /**路径,可选*/
    public String URI;

    /**多语言翻译流组,必选*/
    public String GROUP_ID;

    /**指定流语言*/
    public String LANGUAGE;

    /**多语言版本*/
    public String ASSOC_LANGUAGE;

    /**描述信息,必选*/
    public String NAME;

    /**默认值*/
    public String DEFAULT;

    /**自动选择值*/
    public String AUTOSELECT;

    /**最佳值*/
    public String FORCED;

    /**切片语言版本*/
    public String INSTREAM_ID;

    /**UTI构成的字符串*/
    public String CHARACTERISTICS;

    /**参数字符串(多个用'/'分割)*/
    public String CHANNELS;

    @Override
    public String toString() {
        return "\n{\n" +
                "类型:" + TYPE + "\n" +
                "路径:" + URI + "\n" +
                "多语言翻译流组:" + GROUP_ID + "\n" +
                "指定流语言:" + LANGUAGE + "\n" +
                "多语言版本:" + ASSOC_LANGUAGE + "\n" +
                "描述信息:" + NAME + "\n" +
                "默认值:" + DEFAULT + "\n" +
                "自动选择值:" + AUTOSELECT + "\n" +
                "最佳值:" + FORCED + "\n" +
                "切片语言版本:" + INSTREAM_ID + "\n" +
                "UTI构成的字符串:" + CHARACTERISTICS + "\n" +
                "参数字符串:" + CHANNELS + "\n" +
                "}\n";
    }
}
