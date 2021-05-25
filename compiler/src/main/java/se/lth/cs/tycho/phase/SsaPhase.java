package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.expr.*;
import se.lth.cs.tycho.ir.stmt.*;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.ir.stmt.ssa.*;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.transformation.ssa.SSABlock;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static se.lth.cs.tycho.ir.Variable.variable;

/**
 * The Ssa phase.
 */
public class SsaPhase implements Phase {

    //private final Map<Variable, Map<StmtBlock, Expression>> currentDef = new HashMap<>();
    //private static final CollectOrReplaceExpressions subExprCollectorOrReplacer = MultiJ.from(CollectOrReplaceExpressions.class).instance();

    /**
     * Instantiates a new Ssa phase.
     *
     * @param cfgOnly only build cfg
     */
    public SsaPhase(boolean cfgOnly) {
    }

    @Override
    public String getDescription() {
        return "Creates a CFG and optionally applies SSA transformation";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        Transformation transformation = MultiJ.from(SsaPhase.Transformation.class)
                .bind("declarations").to(task.getModule(VariableDeclarations.key))
                .instance();
        return task.transformChildren(transformation);
    }


//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------//


    @Module
    interface Transformation extends IRNode.Transformation {

        @Binding(BindingKind.INJECTED)
        VariableDeclarations declarations();

        @Override
        default IRNode apply(IRNode node) {
            return node.transformChildren(this);
        }

        /**
         * Applies SSA to an ExprProc
         *
         * @param proc the ExprProc
         * @return the updated ExprProc
         */
        default IRNode apply(ExprProc proc) {
            SSABlock programEntry = new SSABlock(declarations());
            SSABlock exit = programEntry.fill(proc.getBody());
            programEntry.removeTrivialPhis();
            List<Statement> res = Arrays.asList(programEntry.getStmtBlock());

            return proc.withBody(res);
        }
/*
        default IRNode apply(Transition transition) {

            StmtBlock body = (StmtBlock) transition.getBody().get(0);
            List<Statement> statements;
            if (!(body.getVarDecls().isEmpty() && body.getTypeDecls().isEmpty())) {
                StmtBlock startingBlock = new StmtBlock(body.getTypeDecls(), body.getVarDecls(), body.getStatements());
                statements = ImmutableList.of(startingBlock);
            } else {
                statements = body.getStatements();
            }

            SSABlock programEntry = new SSABlock(declarations());
            SSABlock exit = programEntry.fill(transition.getBody());
            programEntry.removeTrivialPhis();
            List<Statement> res = ImmutableList.of(programEntry.getStmtBlock());
            //List<Statement> res = Arrays.asList(new StmtBlock(
            //        programEntry.getTypeDecls(), programEntry.getVarDecls(), programEntry.getStmts()));
            return transition.withBody(res);
            //return transition.withBody(programEntry.getStmtBlock().getStatements());
        }
*/

        default IRNode apply(Action action){
            SSABlock programEntry = new SSABlock(action.getVarDecls(), declarations());
            SSABlock exit = programEntry.fill(action.getBody());
            programEntry.removeTrivialPhis();
            List<Statement> res = ImmutableList.of(programEntry.getStmtBlock());

            return action.withBody(res);
        }


    }


}