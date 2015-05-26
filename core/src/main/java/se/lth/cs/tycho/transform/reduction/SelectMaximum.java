package se.lth.cs.tycho.transform.reduction;

import java.util.List;

import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.Condition.ConditionKind;
import se.lth.cs.tycho.instance.am.PortCondition;
import se.lth.cs.tycho.transform.Transformation;
import se.lth.cs.tycho.transform.reduction.util.ControllerWrapper;
import se.lth.cs.tycho.transform.reduction.util.Extremes;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.FilteredController;
import se.lth.cs.tycho.transform.util.GenInstruction;

public abstract class SelectMaximum<S> extends FilteredController<S> {

	public SelectMaximum(Controller<S> controller) {
		super(controller);
	}

	@Override
	public List<GenInstruction<S>> instructions(S state) {
		return original.instructions(state).stream().collect(Extremes.maxBy(this::compare));
	}

	protected abstract int compare(GenInstruction<S> a, GenInstruction<S> b);

	private static abstract class SelectIsMax<S> extends SelectMaximum<S> {

		SelectIsMax(Controller<S> original) {
			super(original);
		}

		protected abstract boolean isMax(GenInstruction<S> i);

		@Override
		protected int compare(GenInstruction<S> a, GenInstruction<S> b) {
			boolean aIsMax = isMax(a);
			boolean bIsMax = isMax(b);
			if (aIsMax == bIsMax) {
				return 0;
			} else if (aIsMax) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	public static <S> ControllerWrapper<S, S> selectCall() {
		return ctrl -> new SelectIsMax<S>(ctrl) {
			@Override
			protected boolean isMax(GenInstruction<S> i) {
				return i.isCall();
			}
		};
	}

	public static <S> ControllerWrapper<S, S> selectTest() {
		return ctrl -> new SelectIsMax<S>(ctrl) {
			@Override
			protected boolean isMax(GenInstruction<S> i) {
				return i.isTest();
			}
		};
	}

	public static <S> ControllerWrapper<S, S> selectWait() {
		return ctrl -> new SelectIsMax<S>(ctrl) {
			@Override
			protected boolean isMax(GenInstruction<S> i) {
				return i.isWait();
			}
		};
	}

	public static <S> ControllerWrapper<S, S> selectPredicateTest() {
		return ctrl -> new SelectIsMax<S>(ctrl) {
			@Override
			protected boolean isMax(GenInstruction<S> i) {
				if (i instanceof GenInstruction.Test<?>) {
					int cond = ((GenInstruction.Test<?>) i).C();
					return getCondition(cond).kind() == ConditionKind.predicate;
				} else {
					return false;
				}
			}
		};
	}

	public static <S> ControllerWrapper<S, S> selectInputTest() {
		return ctrl -> new SelectIsMax<S>(ctrl) {
			@Override
			protected boolean isMax(GenInstruction<S> i) {
				if (i instanceof GenInstruction.Test<?>) {
					int cond = ((GenInstruction.Test<?>) i).C();
					return getCondition(cond).kind() == ConditionKind.input;
				} else {
					return false;
				}
			}
		};
	}

	public static <S> ControllerWrapper<S, S> selectTestOfManyTokens() {
		return ctrl -> new SelectMaximum<S>(ctrl) {
			@Override
			protected int compare(GenInstruction<S> a, GenInstruction<S> b) {
				return Integer.compare(score(a), score(b));
			}

			private int score(GenInstruction<S> i) {
				if (i instanceof GenInstruction.Test) {
					GenInstruction.Test<?> t = (GenInstruction.Test<?>) i;
					Condition c = getCondition(t.C());
					if (c instanceof PortCondition) {
						return ((PortCondition) c).N();
					}
				}
				return 0;
			}

		};

	}

	public static <S> ControllerWrapper<S, S> selectTestOfFewTokens() {
		return ctrl -> new SelectMaximum<S>(ctrl) {
			@Override
			protected int compare(GenInstruction<S> a, GenInstruction<S> b) {
				return Integer.compare(score(a), score(b));
			}

			private int score(GenInstruction<S> i) {
				if (i instanceof GenInstruction.Test) {
					GenInstruction.Test<?> t = (GenInstruction.Test<?>) i;
					Condition c = getCondition(t.C());
					if (c instanceof PortCondition) {
						return -((PortCondition) c).N();
					}
				}
				return Integer.MIN_VALUE;
			}

		};

	}

}