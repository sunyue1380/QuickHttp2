package cn.schoolwow.quickhttp.domain.m3u8.tag;

/**描述会话数据*/
public class SESSIONDATA {
    /**特定数据值*/
    public String DATA_ID;

    /**指定值*/
    public String VALUE;

    /**资源路径,必须为JSON字符串*/
    public String URI;

    /**语言信息*/
    public String LANGUAGE;

    @Override
    public String toString() {
        return "\n{\n" +
                "特定数据值:" + DATA_ID + "\n" +
                "指定值:" + VALUE + "\n" +
                "资源路径:" + URI + "\n" +
                "语言信息:" + LANGUAGE + "\n" +
                "}\n";
    }
}
