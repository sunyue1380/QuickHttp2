package cn.schoolwow.quickhttp.document.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {
    /**
     * 节点名称
     */
    public String tagName;
    /**
     * 是否是单节点
     */
    public boolean isSingleNode;
    /**
     * 父节点
     */
    public Node parent;
    /**
     * 属性
     */
    public Map<String, String> attributes = new HashMap<>();
    /**
     * 文本内容
     */
    public String textContent = "";
    /**
     * 子节点
     */
    public List<Node> childList = new ArrayList<>();
}
