package se.lth.cs.tycho.transform.reduction;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import se.lth.cs.tycho.messages.MessageReporter;
import se.lth.cs.tycho.transform.Transformation;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.GenInstruction.Call;
import se.lth.cs.tycho.transform.util.GenInstruction.Test;
import se.lth.cs.tycho.transform.util.GenInstruction.Wait;

public class ConditionProbabilityController<S> extends ProbabilityBasedReducer<S> {
	private final Score score = new Score();
	
	private static final double CALL_SCORE = 2.0;
	private static final double DEFAULT_TEST_SCORE = 0.5;
	private static final double WAIT_SCORE = -1.0;
	
	private static final double MARGIN = 0.1;

	public ConditionProbabilityController(Controller<S> controller, Path dataPath, MessageReporter msg) {
		super(controller, dataPath, msg);
	}

	@Override
	protected List<GenInstruction<S>> select(List<GenInstruction<S>> instructions) {
		double max = instructions.stream().mapToDouble(this::score).max().orElse(WAIT_SCORE);
		return instructions.stream().filter(i -> score(i) + MARGIN >= max).collect(Collectors.toList());
	}

	private double score(GenInstruction<S> instr) {
		return instr.accept(score);
	}
	
	protected double defaultValue() {
		return DEFAULT_TEST_SCORE;
	}

	private class Score implements GenInstruction.Visitor<S, Double, Void> {

		@Override
		public Double visitCall(Call<S> call, Void parameter) {
			return CALL_SCORE;
		}

		@Override
		public Double visitTest(Test<S> test, Void parameter) {
			return probability(test.C());
		}

		@Override
		public Double visitWait(Wait<S> wait, Void parameter) {
			return WAIT_SCORE;
		}

	}
	
	public static <S> Transformation<Controller<S>> transformation(Path path, MessageReporter msg) {
		return controller -> new ConditionProbabilityController<>(controller, path, msg);
	}

}
