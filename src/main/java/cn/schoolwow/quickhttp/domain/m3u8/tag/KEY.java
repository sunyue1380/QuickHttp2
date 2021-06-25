package cn.schoolwow.quickhttp.domain.m3u8.tag;

/**媒体资源加密*/
public class KEY {
    /**加密方法*/
    public String METHOD;

    /**密钥路径*/
    public String URI;

    /**AES-128算法加密向量*/
    public String IV;

    /**密钥文件存储方式,要求VERSION>=5*/
    public String KEYFORMAT;

    /**指定具体版本,要求VERSION>=5*/
    public String KEYFORMATVERSIONS;

    @Override
    public String toString() {
        return "\n{\n" +
                "加密方法:" + METHOD + "\n" +
                "密钥路径:" + URI + "\n" +
                "AES-128算法加密向量:" + IV + "\n" +
                "密钥文件存储方式:" + KEYFORMAT + "\n" +
                "指定具体版本:" + KEYFORMATVERSIONS + "\n" +
                "}\n";
    }
}
