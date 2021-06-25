package cn.schoolwow.quickhttp.domain.m3u8.tag;

/**日期范围*/
public class DATERANGE {
    /**id*/
    public String ID;

    /**类*/
    public String CLASS;

    /**开始日期*/
    public String START_DATE;

    /**结束日期*/
    public String END_DATE;

    /**持续时间*/
    public int DURATION;

    @Override
    public String toString() {
        return "\n{\n" +
                "id:" + ID + "\n" +
                "类:" + CLASS + "\n" +
                "开始日期:" + START_DATE + "\n" +
                "结束日期:" + END_DATE + "\n" +
                "持续时间:" + DURATION + "\n" +
                "}\n";
    }
}
