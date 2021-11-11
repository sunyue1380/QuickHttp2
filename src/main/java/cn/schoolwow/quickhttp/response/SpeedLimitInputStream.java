package cn.schoolwow.quickhttp.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class SpeedLimitInputStream extends InputStream {
    private Logger logger = LoggerFactory.getLogger(SpeedLimitInputStream.class);

    /**
     * 最大速率下载1s内下载的字节数
     */
    private volatile long bytesPerSecond;
    /**
     * 记录总共接收字节大小
     */
    private volatile long totalReceivedBytes;
    /**
     * 记录上次接收字节大小
     */
    private volatile long lastReceivedBytes;
    /**
     * 上次记录时间
     */
    private volatile long lastRecordTime = System.nanoTime();
    /**
     * 包装的输入流
     */
    private InputStream inputStream;

    public SpeedLimitInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * 设置最大下载速率(kb/s)
     */
    public synchronized void setMaxDownloadSpeed(int setMaxDownloadSpeed) {
        bytesPerSecond = setMaxDownloadSpeed * 1024;
    }

    /**
     * 限速下载操作
     */
    public synchronized void limit(int length) {
        totalReceivedBytes += length;

        while (bytesPerSecond > 0 && totalReceivedBytes - lastReceivedBytes > bytesPerSecond) {
            long waitNanoTime = 1000000000L - (System.nanoTime() - lastRecordTime);
            if (waitNanoTime > 0) {
                try {
                    Thread.sleep(waitNanoTime / 1000000, (int) (waitNanoTime % 1000000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            lastRecordTime = System.nanoTime();
            lastReceivedBytes += bytesPerSecond;
        }
    }

    @Override
    public int read() throws IOException {
        limit(1);
        return inputStream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        limit(len);
        return inputStream.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (b.length > 0) {
            limit(b.length);
        }
        return inputStream.read(b);
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }
}
