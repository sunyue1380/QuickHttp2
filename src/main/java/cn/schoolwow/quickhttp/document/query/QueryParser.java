package cn.schoolwow.quickhttp.document.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

public class QueryParser {
    private static Logger logger = LoggerFactory.getLogger(QueryParser.class);

    private static final char[] combinators = {'>', '+', '~', ' '};
    private static final Map<String, Evaluator> pseudoMap = new HashMap<>();

    static {
        pseudoMap.put(":first-child", new Evaluator.IsFirstChild());
        pseudoMap.put(":last-child", new Evaluator.IsLastChild());
        pseudoMap.put(":first-of-type", new Evaluator.IsFirstOfType());
        pseudoMap.put(":last-of-type", new Evaluator.IsLastOfType());
        pseudoMap.put(":only-child", new Evaluator.IsOnlyChild());
        pseudoMap.put(":only-of-type", new Evaluator.IsOnlyOfType());
        pseudoMap.put(":empty", new Evaluator.IsEmpty());
    }

    private char[] chars;
    private int pos;
    private Evaluator root;
    private final Stack<Evaluator> evaluatorStack = new Stack<>();
    private CombiningEvaluator.Or or;

    public static Evaluator parse(String cssQuery) {
        return new QueryParser(cssQuery).root;
    }

    private QueryParser(String cssQuery) {
        evaluatorStack.clear();

        chars = cssQuery.toCharArray();
        Selector[] selectors = Selector.values();
        while (pos < chars.length) {
            boolean find = false;
            for (Selector selector : selectors) {
                int count = selector.condition.apply(this);
                if (count > 0) {
                    pos += count;
                    find = true;
                    break;
                }
            }
            if (!find) {
                pos++;
            }
        }
        if (evaluatorStack.size() > 1) {
            CombiningEvaluator.And lastAnd = new CombiningEvaluator.And(new ArrayList<>());
            while (!evaluatorStack.isEmpty() && !(evaluatorStack.peek() instanceof StructuralEvaluator)) {
                lastAnd.evaluatorList.add(evaluatorStack.pop());
            }
            evaluatorStack.push(lastAnd);
        }
        logger.trace("[原始选择器列表]{}", evaluatorStack);
        //如果Or选择器存在,则将栈内剩余元素包括成And选择器加入到Or选择器中
        if (or != null) {
            CombiningEvaluator.And and = new CombiningEvaluator.And(new ArrayList<>());
            while (!evaluatorStack.isEmpty()) {
                and.evaluatorList.add(evaluatorStack.pop());
            }
            if (and.evaluatorList.size() == 1) {
                or.evaluatorList.add(and.evaluatorList.get(0));
            } else {
                or.evaluatorList.add(and);
            }
        }
        if (or != null) {
            root = or;
        } else if (evaluatorStack.size() == 1) {
            root = evaluatorStack.get(0);
        } else {
            List<Evaluator> evaluatorList = new ArrayList<>(evaluatorStack.size());
            while (!evaluatorStack.isEmpty()) {
                evaluatorList.add(evaluatorStack.pop());
            }
            root = new CombiningEvaluator.And(evaluatorList);
        }
        logger.trace("[最终选择器列表]{}", root);
    }

    private enum Selector {
        ByIdOrClass((queryParser) -> {
            char[] chars = queryParser.chars;
            int pos = queryParser.pos;

            if (chars[pos] != '#' && chars[pos] != '.') {
                return 0;
            }
            int last = pos;
            while (last < chars.length - 1 && !isCombinators(chars[last])) {
                last++;
            }
            int count = last - pos;
            if (last == chars.length - 1) {
                count++;
            }
            String content = new String(chars, pos, count);
            if (chars[pos] == '#') {
                Evaluator.Id idEvaluator = new Evaluator.Id(content.substring(1));
                queryParser.evaluatorStack.push(idEvaluator);
                logger.trace("[添加id选择器]{}", idEvaluator);
            } else if (chars[pos] == '.') {
                Evaluator.Class aClassEvaluator = new Evaluator.Class(content.substring(1));
                queryParser.evaluatorStack.push(aClassEvaluator);
                logger.trace("[添加class选择器]{}", aClassEvaluator);
            }
            return content.length();
        }),
        ByTag((queryParser) -> {
            char[] chars = queryParser.chars;
            int pos = queryParser.pos;

            if (!Character.isLetterOrDigit(chars[pos])) {
                return 0;
            }
            int last = pos;
            while (last < chars.length - 1 && Character.isLetterOrDigit(chars[last])) {
                last++;
            }
            int count = last - pos;
            if (last == chars.length - 1) {
                count++;
            }
            String content = new String(chars, pos, count);
            Evaluator.Tag tag = new Evaluator.Tag(content);
            queryParser.evaluatorStack.push(tag);
            logger.trace("[添加tag选择器]{}", tag);
            return content.length();
        }),
        ByAll((queryParser) -> {
            char[] chars = queryParser.chars;
            int pos = queryParser.pos;

            if (chars[pos] != '*') {
                return 0;
            }
            queryParser.evaluatorStack.push(new Evaluator.AllElements());
            return 1;
        }),
        ByAttribute((queryParser) -> {
            char[] chars = queryParser.chars;
            int pos = queryParser.pos;

            if (chars[pos] != '[') {
                return 0;
            }
            int last = pos;
            while (last < chars.length - 1 && chars[last] != ']') {
                last++;
            }
            int count = last - pos + 1;
            String content = new String(chars, pos, count);
            String escapeContent = content.substring(1, content.length() - 1).replaceAll("[\"|']]", "");
            Evaluator evaluator = null;
            if (content.charAt(1) == '^') {
                evaluator = new Evaluator.AttributeStarting(content.substring(2, content.length() - 1));
            } else if (content.contains("^=")) {
                String key = escapeContent.substring(0, escapeContent.indexOf("^="));
                String value = escapeContent.substring(escapeContent.indexOf("^=") + 2);
                evaluator = new Evaluator.AttributeWithValueStarting(key, value);
            } else if (content.contains("$=")) {
                String key = escapeContent.substring(0, escapeContent.indexOf("$="));
                String value = escapeContent.substring(escapeContent.indexOf("$=") + 2);
                evaluator = new Evaluator.AttributeWithValueEnding(key, value);
            } else if (content.contains("*=")) {
                String key = escapeContent.substring(0, escapeContent.indexOf("*="));
                String value = escapeContent.substring(escapeContent.indexOf("*=") + 2);
                evaluator = new Evaluator.AttributeWithValueContaining(key, value);
            } else if (content.contains("~=")) {
                String key = escapeContent.substring(0, escapeContent.indexOf("~="));
                String pattern = escapeContent.substring(escapeContent.indexOf("~=") + 2);
                evaluator = new Evaluator.AttributeWithValueMatching(key, Pattern.compile(pattern));
            } else if (content.contains("=")) {
                String key = escapeContent.substring(0, escapeContent.indexOf('='));
                String value = escapeContent.substring(escapeContent.indexOf('=') + 1);
                evaluator = new Evaluator.AttributeWithValue(key, value);
            } else {
                evaluator = new Evaluator.Attribute(content.substring(1, content.length() - 1));
            }
            queryParser.evaluatorStack.push(evaluator);
            logger.trace("[添加{}选择器]{}", evaluator.getClass().getSimpleName(), evaluator);
            return content.length();
        }),
        ByOr((queryParser) -> {
            char[] chars = queryParser.chars;
            int pos = queryParser.pos;

            if (chars[pos] != ' ' && chars[pos] != ',') {
                return 0;
            }
            int last = pos;
            while (last < chars.length - 1 && (chars[last] == ' ' || chars[last] == ',')) {
                last++;
            }
            String content = new String(chars, pos, last - pos);
            if (!content.contains(",")) {
                return 0;
            }
            //弹出选择器,直到栈为空或者碰到一个And选择器
            CombiningEvaluator.And and = new CombiningEvaluator.And(new ArrayList<>());
            while (!queryParser.evaluatorStack.isEmpty() && !(queryParser.evaluatorStack.peek() instanceof CombiningEvaluator.And)) {
                and.evaluatorList.add(queryParser.evaluatorStack.pop());
            }
            if (queryParser.or == null) {
                queryParser.or = new CombiningEvaluator.Or(new ArrayList<>());
                logger.trace("[添加Or选择器]{}", queryParser.or);
            }
            if (and.evaluatorList.size() == 1) {
                queryParser.or.evaluatorList.add(and.evaluatorList.get(0));
            } else {
                queryParser.or.evaluatorList.add(and);
            }
            logger.trace("[Or选择器中添加And选择器]{}", and);
            return content.length();
        }),
        ByCombination((queryParser) -> {
            char[] chars = queryParser.chars;
            int pos = queryParser.pos;

            if (!isCombinators(chars[pos])) {
                return 0;
            }
            int last = pos;
            while (last < chars.length - 1 && isCombinators(chars[last])) {
                last++;
            }
            String content = new String(chars, pos, last - pos);
            //弹出所有非StructuralEvaluator
            List<Evaluator> evaluatorList = new ArrayList<>();
            while (!queryParser.evaluatorStack.isEmpty() && !(queryParser.evaluatorStack.peek() instanceof StructuralEvaluator)) {
                evaluatorList.add(queryParser.evaluatorStack.pop());
            }
            Evaluator lastEvaluator = null;
            if (evaluatorList.size() == 1) {
                lastEvaluator = evaluatorList.get(0);
            } else {
                lastEvaluator = new CombiningEvaluator.And(evaluatorList);
            }
            if (!queryParser.evaluatorStack.isEmpty()) {
                CombiningEvaluator.And and = new CombiningEvaluator.And(new ArrayList<>());
                and.evaluatorList.add(lastEvaluator);
                and.evaluatorList.add(queryParser.evaluatorStack.pop());
                lastEvaluator = and;
            }

            StructuralEvaluator evaluator = null;
            if (content.contains(">")) {
                evaluator = new StructuralEvaluator.ImmediateParent(lastEvaluator);
            } else if (content.contains("+")) {
                evaluator = new StructuralEvaluator.ImmediatePreviousSibling(lastEvaluator);
            } else if (content.contains("~")) {
                evaluator = new StructuralEvaluator.PreviousSibling(lastEvaluator);
            } else if (content.contains(" ")) {
                evaluator = new StructuralEvaluator.Parent(lastEvaluator);
            }
            queryParser.evaluatorStack.push(evaluator);
            logger.trace("[添加Combination选择器]{}", evaluator);
            return content.length();
        }),
        ByPseudoCommon((queryParser) -> {
            char[] chars = queryParser.chars;
            int pos = queryParser.pos;

            int count = ":first-of-type".length();
            if (pos + count >= chars.length) {
                count = chars.length - pos;
            }
            String prefix = new String(chars, pos, count);
            Set<String> keySet = pseudoMap.keySet();
            String targetKey = null;
            for (String key : keySet) {
                if (prefix.startsWith(key)) {
                    targetKey = key;
                    break;
                }
            }
            if (targetKey == null) {
                return 0;
            }
            queryParser.evaluatorStack.push(pseudoMap.get(targetKey));
            return targetKey.length();
        }),
        ByNth((queryParser) -> {
            char[] chars = queryParser.chars;
            int pos = queryParser.pos;

            if (pos + 5 >= chars.length) {
                return 0;
            }
            String prefix = new String(chars, pos, 5);
            if (!prefix.equals(":nth-")) {
                return 0;
            }
            int last = pos;
            while (last < chars.length - 1 && chars[last] != ')') {
                last++;
            }
            int count = last - pos + 1;
            String content = new String(chars, pos, count);
            String data = content.substring(content.indexOf('(') + 1, content.lastIndexOf(')'));
            int a = -1, b = -1;
            if (data.contains("n")) {
                String token1 = data.substring(0, data.indexOf('n'));
                String token2 = data.substring(data.indexOf('n') + 1);
                if (!token1.equals('-')) {
                    a = Integer.parseInt(token1);
                }
                b = Integer.parseInt(token2);
            } else {
                a = 0;
                b = Integer.parseInt(data);
            }
            if (a < 0 && b < 0) {
                return 0;
            }
            Evaluator structuralEvaluator = null;
            if (content.startsWith(":nth-child(")) {
                structuralEvaluator = new Evaluator.IsNthChild(a, b);
            } else if (content.startsWith(":nth-last-child(")) {
                structuralEvaluator = new Evaluator.IsNthLastChild(a, b);
            } else if (content.startsWith(":nth-of-type(")) {
                structuralEvaluator = new Evaluator.IsNthOfType(a, b);
            } else if (content.startsWith(":nth-last-of-type(")) {
                structuralEvaluator = new Evaluator.IsNthLastOfType(a, b);
            }
            queryParser.evaluatorStack.push(structuralEvaluator);
            logger.trace("[添加Nth选择器]{}", structuralEvaluator);
            return content.length();
        }),
        ByPseudo((queryParser) -> {
            char[] chars = queryParser.chars;
            int pos = queryParser.pos;

            if (chars[pos] != ':') {
                return 0;
            }
            int last = pos;
            while (last < chars.length - 1 && chars[last] != ')') {
                last++;
            }
            String content = new String(chars, pos, last - pos + 1);
            String data = content.substring(content.indexOf('(') + 1, content.lastIndexOf(')'));
            Evaluator evaluator = null;
            if (content.contains(":lt")) {
                evaluator = new Evaluator.IndexLessThan(Integer.parseInt(data));
            } else if (content.contains(":gt")) {
                evaluator = new Evaluator.IndexGreaterThan(Integer.parseInt(data));
            } else if (content.contains(":eq")) {
                evaluator = new Evaluator.IndexEquals(Integer.parseInt(data));
            } else if (content.contains(":has")) {
                evaluator = new StructuralEvaluator.Has(queryParser.evaluatorStack.pop());
            } else if (content.contains(":not")) {
                evaluator = new StructuralEvaluator.Not(queryParser.evaluatorStack.pop());
            } else if (content.contains(":containsOwn")) {
                evaluator = new Evaluator.ContainsOwnText(data);
            } else if (content.contains(":matchesOwn")) {
                evaluator = new Evaluator.MatchesOwn(Pattern.compile(data));
            } else if (content.contains(":contains")) {
                evaluator = new Evaluator.ContainsText(data);
            } else if (content.contains(":matches")) {
                evaluator = new Evaluator.Matches(Pattern.compile(data));
            }
            queryParser.evaluatorStack.push(evaluator);
            logger.trace("[添加伪类选择器]{}", evaluator);
            return content.length();
        });
        private Function<QueryParser, Integer> condition;

        Selector(Function<QueryParser, Integer> condition) {
            this.condition = condition;
        }
    }

    private static boolean isCombinators(char c) {
        for (char combinator : combinators) {
            if (c == combinator) {
                return true;
            }
        }
        return false;
    }
}
