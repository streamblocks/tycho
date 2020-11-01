package se.lth.cs.tycho.ir.stmt.ssa;


import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.List;
import java.util.function.Consumer;


public class StmtLabeled extends Statement {

private final String label;
private final Statement originalStmt;
private ImmutableList<StmtLabeled> predecessors;
private ImmutableList<StmtLabeled> successors;
private StmtLabeled exit;

    public StmtLabeled(String label, Statement originalStmt){
        this(null, label, originalStmt, ImmutableList.empty(), ImmutableList.empty(), null);
    }

    public Statement getOriginalStmt() {
        return originalStmt;
    }

    public void setRelations(List<StmtLabeled> predecessors, List<StmtLabeled> successors){
        setPredecessors(predecessors);
        setSuccessors(successors);
    }

    public void setPredecessors(List<StmtLabeled> predecessors) {
        this.predecessors = ImmutableList.from(predecessors);
    }

    public void setSuccessors(List<StmtLabeled> successors){
        this.successors = ImmutableList.from(successors);
    }

    public ImmutableList<StmtLabeled> getPredecessors(){
        return predecessors;
    }

    public ImmutableList<StmtLabeled> getSuccessors(){
        return successors;
    }

    public boolean lastIsNull(){
        return exit == null;
    }

    public void setExit(StmtLabeled exit){
        this.exit = exit;
    }

    public StmtLabeled getExitBlock(){
        return (exit != null) ? exit : this;
    }

    private StmtLabeled(Statement original, String label, Statement originalStmt, ImmutableList<StmtLabeled> predecessors, ImmutableList<StmtLabeled> successors, StmtLabeled exit) {
        super(original);
        this.label = label;
        this.predecessors = predecessors;
        this.successors = successors;
        this.originalStmt = originalStmt;
        this.exit = exit;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {

    }

    @Override
    public Statement transformChildren(Transformation transformation) {
        return null;
    }
}
