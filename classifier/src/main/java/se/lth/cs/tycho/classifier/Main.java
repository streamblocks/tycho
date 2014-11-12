package se.lth.cs.tycho.classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.parsing.cal.CalParser;
import se.lth.cs.tycho.parsing.cal.ParseException;
import se.lth.cs.tycho.transform.caltoam.CalActorStates;
import se.lth.cs.tycho.transform.caltoam.ActorToActorMachine;
import se.lth.cs.tycho.transform.reduction.PrioritizeCallReducer;
import se.lth.cs.tycho.transform.util.Controller;

public class Main {
	
	private final ActorToActorMachine actorToActorMachine;
	
	public Main() {
		actorToActorMachine = new ActorToActorMachine() {
			@Override
			protected Controller<CalActorStates.State> getStateHandler(Controller<CalActorStates.State> stateHandler) {
				return new PrioritizeCallReducer<>(stateHandler);
			}
		};
	}
	
	public static void main(String[] args) throws FileNotFoundException, ParseException {
		if (args.length == 0) {
			printUsage();
			System.exit(1);
		}
		Main m = new Main();
		for (String arg : args) {
			m.analyze(arg);
		}
	}

	private void analyze(String arg) throws FileNotFoundException, ParseException {
		File file = new File(arg);
		CalParser parser = new CalParser(new FileInputStream(file));
		String name = file.getName();
		System.out.println(name);
		System.out.println(name.replaceAll(".", "="));
		CalActor calActor = (CalActor) parser.ActorDecl().getEntity();
		ActorMachine actorMachine = actorToActorMachine.translate(calActor, null, QID.empty());
		//ErrorModule errors = parser.getErrorModule();
		Classifier classifier = Classifier.getInstance(actorMachine);
		for (String c : classifier.getClasses()) {
			long t = System.nanoTime();
			System.out.println(c + ": " + classifier.isOfClass(c) + " [" + (System.nanoTime()-t)/1000000 + " ms]");
		}
		System.out.println();
	}

	private static void printUsage() {
		System.out.println("java " + Main.class.getName() + " <file>...");
	}

}
