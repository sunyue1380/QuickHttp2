# Proxy

There are three ways to set proxy: single request proxy, client proxy and global proxy.

> The priority: single request proxy > client proxy > global proxy.

## Single request proxy

```java
Response response = client.connect("https://www.google.com")
        .proxy("127.0.0.1",8888)
        .execute();
```

## Client proxy

```java
QuickHttpClient client = QuickHttp.newQuickHttpClient()
        .connect("https://www.google.com")
        .proxy("127.0.0.1",8888);
```

## Global proxy
```java
QuickHttp.proxy("127.0.0.1",8888);
```

## Proxy Dynamically

> removed since 1.0.9

This features is very nice if you want to debug in product environment.

Creating file ``QuickHttpConfig.json`` in directory of jar file.The content are as following:

```json
{
    "proxy": {
        "host": "127.0.0.1",
        "port": 8888
    }
}
```

QuickHttp will monitor the file change and set proxy dynamically.

It will appear ``QuickHttpConfigResult.txt`` If proxy is set successfully. 

Just delete file ``QuickHttpConfig.json`` If you don't need to debug. 