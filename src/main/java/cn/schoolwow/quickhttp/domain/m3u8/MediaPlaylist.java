package cn.schoolwow.quickhttp.domain.m3u8;

import cn.schoolwow.quickhttp.domain.m3u8.tag.SEGMENT;
import cn.schoolwow.quickhttp.domain.m3u8.tag.START;

import java.util.ArrayList;
import java.util.List;

/**媒体播放列表*/
public class MediaPlaylist {
    /**HLS版本号*/
    public String VERSION;

    /**媒体资源*/
    public List<SEGMENT> segmentList = new ArrayList<>();

    /**视频分段最大时长(秒)*/
    public int TARGETDURATION;

    /**播放列表首文件序列号*/
    public int MEDIA_SEQUENCE;

    /**切片中断序列号*/
    public int DISCONTINUITY_SEQUENCE;

    /**流媒体类型(VOD或者EVENT)*/
    public String PLAYLIST_TYPE;

    /**是否是I-Frame*/
    public boolean I_FRAMES_ONLY;

    /**是否可以独立解码*/
    public boolean INDEPENDENT_SEGMENTS;

    /**指定播放列表起始位置*/
    public START start;

    @Override
    public String toString() {
        return "\n{\n" +
                "HLS版本号:" + VERSION + "\n" +
                "媒体资源:" + segmentList + "\n" +
                "视频分段最大时长(秒):" + TARGETDURATION + "\n" +
                "播放列表首文件序列号:" + MEDIA_SEQUENCE + "\n" +
                "切片中断序列号:" + DISCONTINUITY_SEQUENCE + "\n" +
                "流媒体类型:" + PLAYLIST_TYPE + "\n" +
                "是否是I-Frame:" + I_FRAMES_ONLY + "\n" +
                "是否可以独立解码:" + INDEPENDENT_SEGMENTS + "\n" +
                "指定播放列表起始位置:" + start + "\n" +
                "}\n";
    }
}
