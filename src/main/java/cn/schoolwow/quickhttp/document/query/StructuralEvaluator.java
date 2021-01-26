package cn.schoolwow.quickhttp.document.query;

import cn.schoolwow.quickhttp.document.element.Element;

public abstract class StructuralEvaluator extends Evaluator {
    protected Evaluator evaluator;

    public StructuralEvaluator(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    public abstract boolean matches(Element element);

    public static class Has extends StructuralEvaluator {
        public Has(Evaluator evaluator) {
            super(evaluator);
        }

        public boolean matches(Element element) {
            for (Element e : element.getAllElements()) {
                if (e != element && evaluator.matches(e))
                    return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format(":has(%s)", evaluator);
        }
    }

    public static class Not extends StructuralEvaluator {
        public Not(Evaluator evaluator) {
            super(evaluator);
        }

        public boolean matches(Element element) {
            return !evaluator.matches(element);
        }

        @Override
        public String toString() {
            return String.format(":not %s", evaluator);
        }
    }

    public static class Parent extends StructuralEvaluator {
        public Parent(Evaluator evaluator) {
            super(evaluator);
        }

        public boolean matches(Element element) {
            if (element.parent() == null) {
                return false;
            }
            Element parent = element.parent();
            while (true) {
                if (evaluator.matches(parent))
                    return true;
                if (parent.parent() == null)
                    break;
                parent = parent.parent();
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format(":parent %s", evaluator);
        }
    }

    public static class ImmediateParent extends StructuralEvaluator {
        public ImmediateParent(Evaluator evaluator) {
            super(evaluator);
        }

        public boolean matches(Element element) {
            if (element.parent() == null) {
                return false;
            }
            Element parent = element.parent();
            return parent != null && evaluator.matches(parent);
        }

        @Override
        public String toString() {
            return String.format(":ImmediateParent %s", evaluator);
        }
    }

    public static class PreviousSibling extends StructuralEvaluator {
        public PreviousSibling(Evaluator evaluator) {
            super(evaluator);
        }

        public boolean matches(Element element) {
            if (element.parent() == null) {
                return false;
            }
            Element prev = element.previousElementSibling();

            while (prev != null) {
                if (evaluator.matches(prev)) {
                    return true;
                }
                prev = prev.previousElementSibling();
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format(":prev* %s", evaluator);
        }
    }

    public static class ImmediatePreviousSibling extends StructuralEvaluator {
        public ImmediatePreviousSibling(Evaluator evaluator) {
            super(evaluator);
        }

        public boolean matches(Element element) {
            if (element.parent() == null) {
                return false;
            }
            Element prev = element.previousElementSibling();
            return prev != null && evaluator.matches(prev);
        }

        @Override
        public String toString() {
            return String.format(":prev %s", evaluator);
        }
    }
}
