package cn.schoolwow.quickhttp.domain.m3u8.tag;

/**截取部分媒体资源*/
public class BYTERANGE {
    /**截取片段大小*/
    public int n;

    /**截取起始位置*/
    public int o;

    @Override
    public String toString() {
        return "\n{\n" +
                "截取片段大小:" + n + "\n" +
                "截取起始位置:" + o + "\n" +
                "}\n";
    }
}
