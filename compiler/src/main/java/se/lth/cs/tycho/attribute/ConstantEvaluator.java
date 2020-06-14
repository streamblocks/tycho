package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprGlobalVariable;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprUnaryOp;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.Optional;
import java.util.OptionalLong;

@Module
public interface ConstantEvaluator {

	ModuleKey<ConstantEvaluator> key = task -> MultiJ.from(ConstantEvaluator.class)
            .bind("varDecls").to(task.getModule(VariableDeclarations.key))
            .bind("globalNames").to(task.getModule(GlobalNames.key))
            .instance();

	@Binding(BindingKind.INJECTED)
	VariableDeclarations varDecls();

	@Binding(BindingKind.INJECTED)
	GlobalNames globalNames();

	default OptionalLong intValue(Expression e) {
		return OptionalLong.empty();
	}

	default OptionalLong intValue(VarDecl decl) {
		if (decl.isConstant() && decl.getValue() != null) {
			return intValue(decl.getValue());
		} else {
			return OptionalLong.empty();
		}
	}
/*
	default OptionalLong intValue(ParameterVarDecl decl) {
		if (decl.isConstant() && decl.getDefaultValue() != null) {
			return intValue(decl.getDefaultValue());
		} else {
			return OptionalLong.empty();
		}
	}
*/

	default OptionalLong intValue(ExprVariable var) {
		VarDecl declaration = varDecls().declaration(var.getVariable());
		return declaration == null ? OptionalLong.empty() : intValue(declaration);
	}

	default OptionalLong intValue(ExprGlobalVariable var) {
		return intValue(globalNames().varDecl(var.getGlobalName(), false));
	}

	default OptionalLong intValue(ExprLiteral literal) {
		switch (literal.getKind()) {
			case Integer: try {
				String text = literal.getText();
				int radix = 10;
				if (text.startsWith("0x")) {
					text = text.substring(2);
					radix = 16;
				}
				return OptionalLong.of(Long.parseLong(text, radix));
			} catch (NumberFormatException e) {
				return OptionalLong.empty();
			}
			default: {
				return OptionalLong.empty();
			}
		}
	}

	default OptionalLong intValue(ExprUnaryOp unary) {
		OptionalLong v = intValue(unary.getOperand());
		if (v.isPresent()) {
			long vv = v.getAsLong();
			switch (unary.getOperation()) {
				case "-": return OptionalLong.of(-vv);
				case "~": return OptionalLong.of(~vv);
				default: return OptionalLong.empty();
			}
		} else {
			return OptionalLong.empty();
		}
	}

	default OptionalLong intValue(ExprBinaryOp binary) {
		OptionalLong a = intValue(binary.getOperands().get(0));
		OptionalLong b = intValue(binary.getOperands().get(1));
		if (a.isPresent() && b.isPresent()) {
			long aa = a.getAsLong();
			long bb = b.getAsLong();
			switch(binary.getOperations().get(0)) {
				case "+": return OptionalLong.of(aa + bb);
				case "-": return OptionalLong.of(aa - bb);
				case "*": return OptionalLong.of(aa * bb);
				case "/": return OptionalLong.of(aa / bb);
				case "<<": return OptionalLong.of(aa << bb);
				case ">>": return OptionalLong.of(aa >> bb);
				case "&": return OptionalLong.of(aa & bb);
				case "|": return OptionalLong.of(aa | bb);
				case "^": return OptionalLong.of(aa ^ bb);
				default: return OptionalLong.empty();
			}
		} else {
			return OptionalLong.empty();
		}
	}

	Optional<Boolean> boolValue(Expression expr);
}
