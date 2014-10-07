package net.opendf.util;

import net.opendf.errorhandling.ErrorModule;
import net.opendf.interp.BasicActorMachineSimulator;
import net.opendf.interp.BasicChannel;
import net.opendf.interp.BasicEnvironment;
import net.opendf.interp.BasicInterpreter;
import net.opendf.interp.BasicNetworkSimulator;
import net.opendf.interp.Channel;
import net.opendf.interp.Environment;
import net.opendf.interp.Simulator;
import net.opendf.interp.exception.CALCompiletimeException;
import net.opendf.ir.decl.Decl;
import net.opendf.ir.entity.am.ActorMachine;
import net.opendf.ir.entity.cal.Actor;
import net.opendf.ir.entity.nl.NetworkDefinition;
import net.opendf.ir.net.Network;
import net.opendf.ir.util.DeclLoader;

public class Simulate {
	static final String usage = "Correct use: java net.opendf.util.Simulate path entityName" +
			"\nThe entityName should not include the file extension, i.e. use 'Top', not 'Top.nl'";

	public static void main(String[] args){
		int index = 0;
		int defaultStackSize = 100;
		int defaultChannelSize = 10;

		if(index+2 != args.length){
			System.err.println(usage);				
			return;
		}

		String path = args[index++];
		String entityName = args[index++];
		DeclLoader declLoader= new DeclLoader(path);
		Simulator simulator;
		try{
			Decl e = declLoader.getDecl(entityName);
			if(e instanceof NetworkDefinition){
				NetworkDefinition netDef = (NetworkDefinition)e;
				Network net = BasicNetworkSimulator.prepareNetworkDefinition(netDef, declLoader);
				simulator = new BasicNetworkSimulator(net, defaultChannelSize, defaultStackSize);
			} else if(e instanceof Actor){
				Actor actor = (Actor)e;
				ActorMachine actorMachine = BasicActorMachineSimulator.prepareActor(actor, declLoader);

				Channel.OutputEnd[] sourceChannelOutputEnd = new Channel.OutputEnd[actor.getInputPorts().size()];
				for(int i=0; i<sourceChannelOutputEnd.length; i++){
					Channel channel = new BasicChannel(0);  // no one is writing to this channel, so set size to 0
					sourceChannelOutputEnd[i] = channel.createOutputEnd();
				}
				Channel.InputEnd[] sinkChannelInputEnd = new Channel.InputEnd[actor.getOutputPorts().size()];
				for(int i=0; i<sinkChannelInputEnd.length; i++){
					Channel channel = new BasicChannel(defaultChannelSize);
					sinkChannelInputEnd[i] = channel.getInputEnd();
				}
				Environment env = new BasicEnvironment(sinkChannelInputEnd, sourceChannelOutputEnd, actorMachine);
				simulator = new BasicActorMachineSimulator(actorMachine, env, new BasicInterpreter(defaultStackSize));
			} else {
				System.err.println(entityName + " is not a network or actor.");
				return;
			}
		} catch(CALCompiletimeException error){
			ErrorModule em = error.getErrorModule();
			if(em != null){
				em.printErrors();
			} else {
				System.err.println("ERROR: " + error.getMessage());
			}
			System.err.println("Problems occured while compiling, aborting before simulation.");
			return;
		}

		// run the simulation
		try{
			while(simulator.step()){ 			}
		} catch(Exception error){
			StringBuffer sb = new StringBuffer();
			simulator.scopesToString(sb);
			System.out.println(sb);
			throw error;
		}

		// PRINT RESULT
		StringBuffer sb = new StringBuffer();
		simulator.scopesToString(sb);
		System.out.println(sb);
	}
}
