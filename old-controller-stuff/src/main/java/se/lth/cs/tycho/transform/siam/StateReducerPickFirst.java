package se.lth.cs.tycho.transform.siam;

import java.util.Collections;
import java.util.List;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.ICall;
import se.lth.cs.tycho.instance.am.ITest;
import se.lth.cs.tycho.instance.am.IWait;
import se.lth.cs.tycho.instance.am.Instruction;
import se.lth.cs.tycho.instance.am.State;
import se.lth.cs.tycho.instance.am.Transition;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.GenInstruction.Call;
import se.lth.cs.tycho.transform.util.GenInstruction.Test;
import se.lth.cs.tycho.transform.util.GenInstruction.Wait;

public class StateReducerPickFirst implements Controller<Integer> {
	
	private final ImmutableList<State> controller;
	private final QID instanceId;
	private final ActorMachine actorMachine;
	
	public StateReducerPickFirst(ActorMachine am, QID instanceId) {
		this.instanceId = instanceId;
		this.controller = am.getController();
		this.actorMachine = am;
	}

	@Override
	public List<GenInstruction<Integer>> instructions(Integer state) {
		Instruction i = controller.get(state).getInstructions().get(0);
		if (i instanceof ICall) {
			ICall c = (ICall) i;
			return instructionList(new Call<>(c.T(), c.S()));
		} else if (i instanceof ITest) {
			ITest t = (ITest) i;
			return instructionList(new Test<>(t.C(), t.S1(), t.S0()));
		} else if (i instanceof IWait) {
			IWait w = (IWait) i;
			return instructionList(new Wait<>(w.S()));
		}
		return null;
	}
	
	public List<GenInstruction<Integer>> instructionList(GenInstruction<Integer> i) {
		return Collections.singletonList(i);
	}

	@Override
	public Integer initialState() {
		return 0;
	}

	@Override
	public QID instanceId() {
		return instanceId;
	}

	@Override
	public Condition getCondition(int c) {
		return actorMachine.getCondition(c);
	}

	@Override
	public Transition getTransition(int t) {
		return actorMachine.getTransition(t);
	}

}