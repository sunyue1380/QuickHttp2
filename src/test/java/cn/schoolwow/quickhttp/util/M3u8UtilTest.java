package cn.schoolwow.quickhttp.util;

import cn.schoolwow.quickhttp.domain.m3u8.M3u8Type;
import cn.schoolwow.quickhttp.domain.m3u8.MasterPlaylist;
import cn.schoolwow.quickhttp.domain.m3u8.MediaPlaylist;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class M3u8UtilTest {
    /**主播放m3u8列表*/
    private String masterContent;
    /**媒体m3u8列表*/
    private String mediaContent;
    {
        try {
            masterContent = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir")+"/src/test/resources/m3u8/masterPlayList.m3u8")));
            mediaContent = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir")+"/src/test/resources/m3u8/mediaPlayList.m3u8")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getM3u8Type() {
        M3u8Type m3u8Type = M3u8Util.getM3u8Type(masterContent);
        Assert.assertEquals(M3u8Type.MasterPlayList,m3u8Type);
        m3u8Type = M3u8Util.getM3u8Type(mediaContent);
        Assert.assertEquals(M3u8Type.MediaPlayList,m3u8Type);
    }

    @Test
    public void getMasterPlaylist() throws Exception {
        MasterPlaylist masterPlaylist = M3u8Util.getMasterPlaylist("",masterContent);
        System.out.println(masterPlaylist);
    }

    @Test
    public void getMediaPlaylist() throws Exception {
        MediaPlaylist mediaPlaylist = M3u8Util.getMediaPlaylist("",mediaContent);
        System.out.println(mediaPlaylist);
    }
}