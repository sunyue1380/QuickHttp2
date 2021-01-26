package cn.schoolwow.quickhttp.domain;

import cn.schoolwow.quickhttp.QuickHttp;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuickHttpConfig {
    private static Logger logger = LoggerFactory.getLogger(QuickHttpConfig.class);
    /**
     * 全局代理
     */
    public static Proxy proxy;

    /**
     * 监听配置文件更改线程池
     */
    private static ExecutorService fileWatchThreadPool = Executors.newSingleThreadExecutor();

    /**
     * 配置文件路径
     */
    private static Path configPath = Paths.get(System.getProperty("user.dir") + "/QuickHttpConfig.json");

    /**
     * 配置文件执行结果路径
     */
    private static Path configResultPath = Paths.get(System.getProperty("user.dir") + "/QuickHttpConfigResult.txt");

    static {
        //打开限制头部
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        //禁止httpUrlConnection自动重试
        System.setProperty("sun.net.http.retryPost", "false");

        //注册文件监听
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            configPath.getParent().register(watchService, new WatchEvent.Kind[]{
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE,
            });
            logger.info("[监听配置文件路径]{}", configPath);
            fileWatchThreadPool.execute(() -> {
                applyConfiguration();
                while (true) {
                    try {
                        WatchKey watchKey = watchService.take();
                        if (watchKey == null) {
                            return;
                        }
                        for (WatchEvent<?> event : watchKey.pollEvents()) {
                            if (!event.context().toString().equals(configPath.getFileName().toString())) {
                                continue;
                            }
                            if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                                try {
                                    Files.deleteIfExists(configResultPath);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                QuickHttp.clientConfig().proxy(null);
                            }
                            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE
                                    || event.kind() == StandardWatchEventKinds.ENTRY_MODIFY
                            ) {
                                applyConfiguration();
                            }
                        }
                        //监听复位
                        watchKey.reset();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            logger.warn("[加载监听服务失败]{}", e.getMessage());
        }
    }

    /**
     * 应用配置文件项
     */
    private static void applyConfiguration() {
        if (!Files.exists(configPath)) {
            return;
        }
        StringBuilder result = new StringBuilder();
        try {
            byte[] bytes = Files.readAllBytes(configPath);
            String content = new String(bytes, StandardCharsets.UTF_8);
            JSONObject config = JSON.parseObject(content);
            //应用代理
            if (config.containsKey("proxy")) {
                JSONObject proxy = config.getJSONObject("proxy");
                QuickHttpConfig.proxy = new Proxy(Proxy.Type.HTTP,new InetSocketAddress(proxy.getString("host"), proxy.getIntValue("port")));
                result.append(LocalDateTime.now() + " [应用全局代理]" + QuickHttpConfig.proxy + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            result.append(LocalDateTime.now() + " [读取配置文件异常]" + e.getMessage() + "\n");
        } finally {
            try {
                if (result.length() > 0) {
                    Files.write(configResultPath, result.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
