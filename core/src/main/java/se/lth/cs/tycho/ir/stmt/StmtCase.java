package se.lth.cs.tycho.ir.stmt;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.expr.pattern.Pattern;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class StmtCase extends Statement {

	public static class Alternative extends AbstractIRNode {

		private Pattern pattern;
		private ImmutableList<Expression> guards;
		private ImmutableList<Statement> statements;

		public Alternative(Pattern pattern, List<Expression> guards, List<Statement> statements) {
			this(null, pattern, guards, statements);
		}

		public Alternative(IRNode original, Pattern pattern, List<Expression> guards, List<Statement> statements) {
			super(original);
			this.pattern = pattern;
			this.guards = ImmutableList.from(guards);
			this.statements = ImmutableList.from(statements);
		}

		public Pattern getPattern() {
			return pattern;
		}

		public ImmutableList<Expression> getGuards() {
			return guards;
		}

		public ImmutableList<Statement> getStatements() {
			return statements;
		}

		public Alternative copy(Pattern pattern, List<Expression> guards, List<Statement> statements) {
			if (Objects.equals(getPattern(), pattern) && Lists.sameElements(getGuards(), guards) && Lists.sameElements(getStatements(), statements)) {
				return this;
			} else {
				return new Alternative(this, pattern, guards, statements);
			}
		}

		@Override
		public void forEachChild(Consumer<? super IRNode> action) {
			action.accept(getPattern());
			guards.forEach(action);
			statements.forEach(action);
		}

		@Override
		public IRNode transformChildren(Transformation transformation) {
			return copy((Pattern) transformation.apply(getPattern()), transformation.mapChecked(Expression.class, getGuards()), transformation.mapChecked(Statement.class, getStatements()));
		}
	}

	private Expression expression;
	private ImmutableList<Alternative> alternatives;
	private Statement default_;

	public StmtCase(Expression expression, List<Alternative> alternatives, Statement default_) {
		this(null, expression, alternatives, default_);
	}

	public StmtCase(Statement original, Expression expression, List<Alternative> alternatives, Statement default_) {
		super(original);
		this.expression = expression;
		this.alternatives = ImmutableList.from(alternatives);
		this.default_ = default_;
	}

	public Expression getExpression() {
		return expression;
	}

	public ImmutableList<Alternative> getAlternatives() {
		return alternatives;
	}

	public Statement getDefault() {
		return default_;
	}

	public StmtCase copy(Expression expression, List<Alternative> alternatives, Statement default_) {
		if (Objects.equals(getExpression(), expression) && Lists.sameElements(getAlternatives(), alternatives) && Objects.equals(getDefault(), default_)) {
			return this;
		} else {
			return new StmtCase(this, expression, alternatives, default_);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getExpression());
		getAlternatives().forEach(action);;
		action.accept(getDefault());
	}

	@Override
	public Statement transformChildren(Transformation transformation) {
		return copy((Expression) transformation.apply(getExpression()), transformation.mapChecked(Alternative.class, getAlternatives()), (Statement) transformation.apply(getDefault()));
	}
}