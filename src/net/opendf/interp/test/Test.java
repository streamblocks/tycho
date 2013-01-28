package net.opendf.interp.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.opendf.analysis.BinOpToFunc;
import net.opendf.analysis.UnOpToFunc;
import net.opendf.analysis.VariableBindings;
import net.opendf.interp.BasicFixedSizeChannel;
import net.opendf.interp.Channel;
import net.opendf.interp.Memory;
import net.opendf.interp.Sim;
import net.opendf.interp.attr.Variables.VariableDeclaration;
import net.opendf.interp.attr.Variables.VariableUse;
import net.opendf.interp.preprocess.EvaluateLiterals;
import net.opendf.interp.preprocess.SetChannelIds;
import net.opendf.interp.preprocess.SetScopeInitializers;
import net.opendf.interp.preprocess.SetVariablePositions;
import net.opendf.interp.values.BasicRef;
import net.opendf.interp.values.ConstRef;
import net.opendf.interp.values.RefView;
import net.opendf.interp.values.predef.Predef;
import net.opendf.ir.IRNode;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Scope;
import net.opendf.ir.am.Scope.ScopeKind;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.common.Decl;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprLiteral;
import net.opendf.ir.common.ExprVariable;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.PortDecl;
import net.opendf.ir.common.PortName;
import net.opendf.parser.lth.CalParser;
import net.opendf.trans.caltoam.ActorToActorMachine;

public class Test {
	public static void main(String[] args) throws FileNotFoundException {
		//File calFile = new File("../dataflow/examples/SimpleExamples/Add.cal");
		File calFile = new File("../dataflow/examples/MPEG4_SP_Decoder/ACPred.cal");

		CalParser parser = new CalParser();
		Actor actor = parser.parse(calFile);
		// net.opendf.util.PrettyPrint print = new
		// net.opendf.util.PrettyPrint();
		// print.print(actor);

		List<Decl> actorArgs = new ArrayList<Decl>();
		actorArgs.add(varDecl("MAXW_IN_MB", lit(121)));
		actorArgs.add(varDecl("MB_COORD_SZ", lit(8)));
		actorArgs.add(varDecl("SAMPLE_SZ", lit(13)));
		Scope argScope = new Scope(ScopeKind.Persistent, actorArgs);

		ActorToActorMachine trans = new ActorToActorMachine();
		ActorMachine actorMachine = trans.translate(actor, argScope);

		// net.opendf.ir.am.util.ControllerToGraphviz.print(new
		// PrintStream("controller.gv"), actorMachine, "Controller");

		BinOpToFunc binOpToFunc = new BinOpToFunc();
		binOpToFunc.transformActorMachine(actorMachine);

		UnOpToFunc unOpToFunc = new UnOpToFunc();
		unOpToFunc.transformActorMachine(actorMachine);

		VariableBindings varBind = new VariableBindings();
		VariableBindings.Bindings b = varBind.bindVariables(actorMachine);

		SetVariablePositions setVarPos = new SetVariablePositions();
		int memSize = setVarPos.setVariablePositions(actorMachine);
		for (Entry<IRNode, IRNode> binding : b.getVariableBindings().entrySet()) {
			VariableDeclaration decl = (VariableDeclaration) binding.getValue();
			int pos = decl.getVariablePosition();
			boolean stack = decl.isVariableOnStack();
			VariableUse use = (VariableUse) binding.getKey();
			use.setVariablePosition(pos, stack);
		}
		
		EvaluateLiterals evalLit = new EvaluateLiterals();
		evalLit.evaluateLiterals(actorMachine);

		Map<PortName, Integer> portMap = new HashMap<PortName, Integer>();
		{
			int i = 0;
			for (PortDecl in : actorMachine.getInputPorts().getChildren()) {
				portMap.put(new PortName(in.getLocalName()), i++);
			}
			for (PortDecl out : actorMachine.getOutputPorts().getChildren()) {
				portMap.put(new PortName(out.getLocalName()), i++);
			}
		}

		SetScopeInitializers si = new SetScopeInitializers();
		si.setScopeInitializers(actorMachine);

		SetChannelIds ci = new SetChannelIds();
		ci.setChannelIds(actorMachine, portMap);

		Channel[] channels = { new BasicFixedSizeChannel(1), new BasicFixedSizeChannel(1), new BasicFixedSizeChannel(1) };
		channels[0].write(ConstRef.of(3));
		channels[1].write(ConstRef.of(5));
		Sim s = new Sim(actorMachine, channels, 100, 100);
		Memory mem = s.actorMachineEnvironment().getMemory();

		int pos = memSize;
		Map<String, RefView> predef = Predef.predef();
		for (IRNode n : b.getFreeVariables()) {
			ExprVariable e = (ExprVariable) n;
			System.out.println("Free var: " + e.getName());
			e.setVariablePosition(pos, false);
			predef.get(e.getName()).assignTo(mem.declare(pos));
			pos += 1;
		}
		s.actorMachineRunner().step();
		if (channels[2].tokens(1)) {
			BasicRef r = new BasicRef();
			channels[2].peek(0, r);
			System.out.println(r.getLong());
		} else {
			System.out.println("error");
		}
	}
	private static DeclVar varDecl(String name, Expression expr) {
		return new DeclVar(null, name, null, expr, false);
	}
	
	private static ExprLiteral lit(int i) {
		return new ExprLiteral(ExprLiteral.litInteger, Integer.toString(i));
	}

}
