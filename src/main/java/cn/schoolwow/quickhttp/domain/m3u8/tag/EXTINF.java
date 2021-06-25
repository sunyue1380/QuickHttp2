package cn.schoolwow.quickhttp.domain.m3u8.tag;

/**指定片时长和标题*/
public class EXTINF {
    /**时长(秒)*/
    public String duration;

    /**标题*/
    public String title;

    @Override
    public String toString() {
        return "\n{\n" +
                "时长(秒):" + duration + "\n" +
                "标题:" + title + "\n" +
                "}\n";
    }
}
