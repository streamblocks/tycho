package se.lth.cs.tycho.transform.util;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.entity.cal.ProcessDescription;
import se.lth.cs.tycho.ir.entity.cal.ScheduleFSM;
import se.lth.cs.tycho.ir.entity.cal.Transition;
import se.lth.cs.tycho.ir.util.ImmutableList;

public interface ActorTransformer<P> extends BasicTransformer<P> {
	public CalActor transformActor(CalActor calActor, P param);

	public Action transformAction(Action action, P param);
	public ImmutableList<Action> transformActions(ImmutableList<Action> actions, P param);

	public InputPattern transformInputPattern(InputPattern input, P param);
	public ImmutableList<InputPattern> transformInputPatterns(ImmutableList<InputPattern> inputs, P param);

	public OutputExpression transformOutputExpression(OutputExpression output, P param);
	public ImmutableList<OutputExpression> transformOutputExpressions(ImmutableList<OutputExpression> output, P param);

	public ImmutableList<ImmutableList<QID>> transformPriorities(ImmutableList<ImmutableList<QID>> prios, P param);

	public ScheduleFSM transformSchedule(ScheduleFSM schedule, P param);

	public ProcessDescription transformProcessDescription(ProcessDescription process, P param);

	public Transition transformScheduleTransition(Transition transition, P param);
	public ImmutableList<Transition> transformScheduleTransitions(ImmutableList<Transition> transitions, P param);
	
	public QID transformTag(QID tag, P param);
	public ImmutableList<QID> transformTags(ImmutableList<QID> tags, P param);
	
	public PortDecl transformInputPort(PortDecl port, P param);
	public ImmutableList<PortDecl> transformInputPorts(ImmutableList<PortDecl> port, P param);

	public PortDecl transformOutputPort(PortDecl port, P param);
	public ImmutableList<PortDecl> transformOutputPorts(ImmutableList<PortDecl> port, P param);
}