# 代理设置

QuickHttp支持单个请求代理,客户端代理和全局代理.

> 代理优先级顺序为单个请求代理>客户端代理>全局代理

## 单个请求设置代理
```java
Response response = client.connect("https://www.baidu.com")
                .proxy("127.0.0.1",8888)
                .execute();
```

## 客户端代理
```java
QuickHttpClient client = QuickHttp.newQuickHttpClient()
                .proxy("127.0.0.1",8888);
```

## 全局代理
```java
QuickHttp.proxy("127.0.0.1",8888);
```

## 动态设置http代理

适用场景: 在生产环境中需要设置http请求代理以便调试问题时.

在应用程序(jar包)所在目录创建QuickHttpConfig.json文件.文件内容如下:

```json
{
    "proxy": {
        "host": "127.0.0.1",
        "port": 80
    }
}
```

QuickHttp会自动检测文件的创建和修改.此时若成功设置了代理,当前目录下会出现QuickHttpConfigResult.txt文件.该文件记录的配置项的设置情况.

当不再需要设置http代理时,删掉QuickHttpConfig.json即可.