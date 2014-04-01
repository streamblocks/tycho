package net.opendf.backend.c.att;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javarag.Module;
import javarag.Synthesized;
import net.opendf.backend.c.util.Joiner;
import net.opendf.ir.IRNode;
import net.opendf.ir.common.ExprApplication;
import net.opendf.ir.common.ExprVariable;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.StmtCall;
import net.opendf.ir.common.Variable;

public class FunctionApplications extends Module<FunctionApplications.Required> {
	private final Map<String, String> binOps;
	private final Map<String, String> preOps;
	private final Map<String, String> postOps;
	private final Joiner comma = new Joiner(", ");

	public FunctionApplications() {
		Map<String, String> binOps = new HashMap<>();
		Map<String, String> preOps = new HashMap<>();
		Map<String, String> postOps = new HashMap<>();

		binOps.put("$BinaryOperation.&", "&");
		binOps.put("$BinaryOperation.|", "|");
		binOps.put("$BinaryOperation.and", "&&");
		binOps.put("$BinaryOperation.or", "||");
		binOps.put("$BinaryOperation.+", "+");
		binOps.put("$BinaryOperation.-", "-");
		binOps.put("$BinaryOperation.*", "*");
		binOps.put("$BinaryOperation./", "/");
		binOps.put("$BinaryOperation.=", "==");
		binOps.put("$BinaryOperation.!=", "!=");
		binOps.put("$BinaryOperation.<", "<");
		binOps.put("$BinaryOperation.<=", "<=");
		binOps.put("$BinaryOperation.>", ">");
		binOps.put("$BinaryOperation.>=", ">=");
		binOps.put("$BinaryOperation.>>", ">>");
		binOps.put("$BinaryOperation.<<", "<<");
		binOps.put("$BinaryOperation.^", "^");
		binOps.put("lshift", "<<");
		binOps.put("rshift", ">>");
		binOps.put("bitor", "|");
		binOps.put("bitand", "&");
		this.binOps = Collections.unmodifiableMap(binOps);

		preOps.put("$UnaryOperation.-", "-");
		preOps.put("$UnaryOperation.not", "!");
		preOps.put("$UnaryOperation.~", "~");
		this.preOps = Collections.unmodifiableMap(preOps);

		this.postOps = Collections.unmodifiableMap(postOps);
	}

	@Synthesized
	public String functionApplication(ExprVariable func, ExprApplication apply) {
		assert func == apply.getFunction();
		IRNode decl = get().declaration(func.getVariable());
		if (decl == null) {
			int numArgs = apply.getArgs().size();
			String name = func.getVariable().getName();
			if (numArgs == 2 && binOps.containsKey(name)) {
				String op = binOps.get(name);
				Expression left = apply.getArgs().get(0);
				Expression right = apply.getArgs().get(1);
				return get().parenthesizedExpression(left) + " " + op + " " + get().parenthesizedExpression(right);
			} else if (numArgs == 1 && preOps.containsKey(name)) {
				return preOps.get(name) + get().parenthesizedExpression(apply.getArgs().get(0));
			} else if (numArgs == 1 && postOps.containsKey(name)) {
				return postOps.get(name) + get().parenthesizedExpression(apply.getArgs().get(0));
			} else {
				throw new Error();
			}
		} else {
			String name = get().functionName(decl);
			ArrayList<String> args = new ArrayList<>();
			for (Expression arg : apply.getArgs()) {
				args.add(get().simpleExpression(arg));
			}
			return name + "(" + comma.join(args) + ")";
		}
	}

	@Synthesized
	public String procedureCall(ExprVariable proc, StmtCall call) {
		IRNode decl = get().declaration(proc.getVariable());
		String name = get().functionName(decl);
		ArrayList<String> args = new ArrayList<>();
		for (Expression arg : call.getArgs()) {
			args.add(get().simpleExpression(arg));
		}
		return name + "(" + comma.join(args) + ");\n";
	}

	interface Required {
		IRNode declaration(Variable var);
		String parenthesizedExpression(Expression e);
		String simpleExpression(Expression e);
		String functionName(IRNode decl);
	}

}