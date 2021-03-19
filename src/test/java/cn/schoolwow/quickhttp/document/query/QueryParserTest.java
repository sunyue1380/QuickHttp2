package cn.schoolwow.quickhttp.document.query;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryParserTest {
    @Test
    public void testEvaluator() {
        Map<String, Class> evaluatorMap = new LinkedHashMap<>();
        evaluatorMap.put("#id", Evaluator.Id.class);
        evaluatorMap.put(".class", Evaluator.Class.class);
        evaluatorMap.put("[attr]", Evaluator.Attribute.class);
        evaluatorMap.put("[^attrPrefix]", Evaluator.AttributeStarting.class);
        evaluatorMap.put("[attr=val]", Evaluator.AttributeWithValue.class);
        evaluatorMap.put("[attr=\"val\"]", Evaluator.AttributeWithValue.class);
        evaluatorMap.put("[attr^=valPrefix]", Evaluator.AttributeWithValueStarting.class);
        evaluatorMap.put("[attr$=valSuffix]", Evaluator.AttributeWithValueEnding.class);
        evaluatorMap.put("[attr*=valContaining]", Evaluator.AttributeWithValueContaining.class);
        evaluatorMap.put("[attr~=regex]", Evaluator.AttributeWithValueMatching.class);
        Set<String> keySet = evaluatorMap.keySet();
        for (String key : keySet) {
            Evaluator evaluator = QueryParser.parse(key);
            Assert.assertEquals(evaluatorMap.get(key), evaluator.getClass());
        }
    }

    @Test
    public void testCombination() {
        Map<String, Class> evaluatorMap = new LinkedHashMap<>();
        evaluatorMap.put("div p", StructuralEvaluator.Parent.class);
        evaluatorMap.put("div > p", StructuralEvaluator.ImmediateParent.class);
        evaluatorMap.put("div + p", StructuralEvaluator.ImmediatePreviousSibling.class);
        evaluatorMap.put("div ~ p", StructuralEvaluator.PreviousSibling.class);
        Set<String> keySet = evaluatorMap.keySet();
        for (String key : keySet) {
            Evaluator evaluator = QueryParser.parse(key);
            CombiningEvaluator.And and = (CombiningEvaluator.And) evaluator;
            List<Evaluator> evaluatorList = and.evaluatorList;
            Assert.assertEquals(evaluatorMap.get(key), evaluatorList.get(evaluatorList.size() - 1).getClass());
        }
    }

    @Test
    public void testOr() {
        Evaluator evaluator = QueryParser.parse("div , p");
        Assert.assertEquals(CombiningEvaluator.Or.class, evaluator.getClass());
    }

    @Test
    public void testCommonPseudo() {
        Map<String, Class> evaluatorMap = new LinkedHashMap<>();
        evaluatorMap.put(":first-child", Evaluator.IsFirstChild.class);
        evaluatorMap.put(":last-child", Evaluator.IsLastChild.class);
        evaluatorMap.put(":first-of-type", Evaluator.IsFirstOfType.class);
        evaluatorMap.put(":last-of-type", Evaluator.IsLastOfType.class);
        evaluatorMap.put(":only-child", Evaluator.IsOnlyChild.class);
        evaluatorMap.put(":only-of-type", Evaluator.IsOnlyOfType.class);
        evaluatorMap.put(":empty", Evaluator.IsEmpty.class);
        Set<String> keySet = evaluatorMap.keySet();
        for (String key : keySet) {
            Evaluator evaluator = QueryParser.parse(key);
            Assert.assertEquals(evaluatorMap.get(key), evaluator.getClass());
        }
    }

    @Test
    public void testPseudo() {
        Map<String, Class> evaluatorMap = new LinkedHashMap<>();
        evaluatorMap.put("div:lt(0)", Evaluator.IndexLessThan.class);
        evaluatorMap.put("div:gt(0)", Evaluator.IndexGreaterThan.class);
        evaluatorMap.put("div:eq(0)", Evaluator.IndexEquals.class);
        evaluatorMap.put("div:has(p)", StructuralEvaluator.Has.class);
        evaluatorMap.put("div:not(p)", StructuralEvaluator.Not.class);
        evaluatorMap.put("div:contains(quickhttp)", Evaluator.ContainsText.class);
        evaluatorMap.put("div:matches(\\\\d+)", Evaluator.Matches.class);
        evaluatorMap.put("div:containsOwn(quickhttp)", Evaluator.ContainsOwnText.class);
        evaluatorMap.put("div:matchesOwn(\\\\d+)", Evaluator.MatchesOwn.class);
        Set<String> keySet = evaluatorMap.keySet();
        for (String key : keySet) {
            Evaluator evaluator = QueryParser.parse(key);
            if (evaluator instanceof StructuralEvaluator) {
                Assert.assertEquals(evaluatorMap.get(key), evaluator.getClass());
            } else {
                Assert.assertEquals(CombiningEvaluator.And.class, evaluator.getClass());
                CombiningEvaluator.And andEvaluator = (CombiningEvaluator.And) evaluator;
                List<Evaluator> evaluatorList = andEvaluator.evaluatorList;
                Assert.assertEquals(evaluatorMap.get(key), evaluatorList.get(0).getClass());
            }
        }
    }

    @Test
    public void testNth() {
        Map<String, Class> evaluatorMap = new LinkedHashMap<>();
        evaluatorMap.put("div:nth-child(10n-1)", Evaluator.IsNthChild.class);
        evaluatorMap.put("div:nth-last-child(-n+2)", Evaluator.IsNthLastChild.class);
        evaluatorMap.put("div:nth-of-type(2n+1)", Evaluator.IsNthOfType.class);
        evaluatorMap.put("div:nth-last-of-type(2n+1)", Evaluator.IsNthLastOfType.class);
        Set<String> keySet = evaluatorMap.keySet();
        for (String key : keySet) {
            Evaluator evaluator = QueryParser.parse(key);
            Assert.assertEquals(CombiningEvaluator.And.class, evaluator.getClass());
            CombiningEvaluator.And andEvaluator = (CombiningEvaluator.And) evaluator;
            List<Evaluator> evaluatorList = andEvaluator.evaluatorList;
            Assert.assertEquals(evaluatorMap.get(key), evaluatorList.get(0).getClass());
        }
    }

    @Test
    public void testAllElement() {
        Map<String, Class> evaluatorMap = new LinkedHashMap<>();
        evaluatorMap.put("*", Evaluator.AllElements.class);
        Set<String> keySet = evaluatorMap.keySet();
        for (String key : keySet) {
            Evaluator evaluator = QueryParser.parse(key);
            Assert.assertEquals(evaluatorMap.get(key), evaluator.getClass());
        }
    }

    @Test
    public void testCompositeEvaluator() {
        String cssQuery = "#id > div:nth-child(5)";
        Evaluator evaluator = QueryParser.parse(cssQuery);
        Assert.assertEquals(CombiningEvaluator.And.class, evaluator.getClass());
        CombiningEvaluator.And and = (CombiningEvaluator.And) evaluator;
        List<Evaluator> evaluatorList = and.evaluatorList;
        Assert.assertEquals(CombiningEvaluator.And.class, evaluatorList.get(0).getClass());
        {
            CombiningEvaluator combiningEvaluator = (CombiningEvaluator) evaluatorList.get(0);
            List<Evaluator> combiningEvaluatorList = combiningEvaluator.evaluatorList;
            Assert.assertEquals(2,combiningEvaluatorList.size());
            Assert.assertEquals(Evaluator.IsNthChild.class, combiningEvaluatorList.get(0).getClass());
            Assert.assertEquals(Evaluator.Tag.class, combiningEvaluatorList.get(1).getClass());
        }
        Assert.assertEquals(StructuralEvaluator.ImmediateParent.class, evaluatorList.get(1).getClass());
        {
            StructuralEvaluator.ImmediateParent immediateParent = (StructuralEvaluator.ImmediateParent) evaluatorList.get(1);
            Assert.assertEquals(StructuralEvaluator.Id.class, immediateParent.evaluator.getClass());
        }
    }

    @Test
    public void testMeta() {
        String cssQuery = "meta[http-equiv=content-type], meta[charset]";
        Evaluator evaluator = QueryParser.parse(cssQuery);
        Assert.assertEquals(CombiningEvaluator.Or.class, evaluator.getClass());
    }
}
