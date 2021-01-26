package cn.schoolwow.quickhttp.document.query;

import cn.schoolwow.quickhttp.document.element.Element;

import java.util.List;

public abstract class CombiningEvaluator extends Evaluator {
    protected List<Evaluator> evaluatorList;

    public CombiningEvaluator(List<Evaluator> evaluatorList) {
        this.evaluatorList = evaluatorList;
    }

    public abstract boolean matches(Element element);

    public static final class And extends CombiningEvaluator {
        public And(List<Evaluator> evaluatorList) {
            super(evaluatorList);
        }

        @Override
        public boolean matches(Element element) {
            for (Evaluator evaluator : evaluatorList) {
                if (!evaluator.matches(element)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return "[AND]" + evaluatorList;
        }
    }

    public static final class Or extends CombiningEvaluator {
        public Or(List<Evaluator> evaluatorList) {
            super(evaluatorList);
        }

        @Override
        public boolean matches(Element element) {
            for (Evaluator evaluator : evaluatorList) {
                if (evaluator.matches(element)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "[OR]" + evaluatorList;
        }
    }
}
