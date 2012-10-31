package br.usp.ime.simulation.choreography;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.simgrid.msg.Host;
import org.simgrid.msg.Msg;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.Process;
import org.simgrid.msg.Task;

import commTime.FinalizeTask;

import br.usp.ime.simulation.datatypes.task.ResponseTask;
import br.usp.ime.simulation.datatypes.task.WsMethod;
import br.usp.ime.simulation.datatypes.task.WsRequest;
import br.usp.ime.simulation.experiments.control.ControlVariables;
import br.usp.ime.simulation.log.Log;
import br.usp.ime.simulation.monitoring.ChoreographyMonitor;
import br.usp.ime.simulation.orchestration.Orchestration;
import br.usp.ime.simulation.shared.ServiceInvoker;
import br.usp.ime.simulation.shared.ServiceRegistry;
import br.usp.ime.simulation.shared.WsRequestSender;

public class Choreographer extends ServiceInvoker {
	
	private String myMailbox = "choreographer";
	private Integer throughput;
	private Double inputMessageSize;
	private Double outputMessageSize;
	private String entryMailbox="";
	private String entryServiceName="";
	private String entryServiceNameMethod="";
	
	private Log log = new Log();
	
	
	//public Choreographer(String[] mainArgs, Host host) {
	public Choreographer(Host host, String name, String[]args) {
		super(host, "choreographer", args);
	}

	@Override
	public void main(String[] args) throws MsgException {
		if (args.length != 5) {
			Msg.info("The choreographer must receive 5 input: request per sec rate");
			System.exit(1);
		}

		this.log.open();
		ControlVariables.DEBUG =true; ControlVariables.PRINT_ALERTS=true; 


		ChoreographyParser parser= new ChoreographyParser("smallChoreographySpecification.xml");
		parser.generateChoreographyModel();
		
		int numberOfInstances=1;
		this.throughput = Integer.valueOf(args[0]);
		
		this.entryServiceName = args[1];
		this.entryServiceNameMethod= args[2];
		this.inputMessageSize = Double.valueOf(args[3]);
		this.outputMessageSize = Double.valueOf(args[4]);
		//computeSize?
		//String entryHost= args[4] ;//Host.getByName("...") 
		
		try {
			this.entryMailbox =   findServiceMailbox(this.entryServiceName);//"WS_" + entryServiceName + "_at_" + entryHost;
		} catch (InterruptedException e) {
			System.out.println("entryMailbox of entry service failed!");
			e.printStackTrace();
		}  

				
		//if (ControlVariables.DEBUG || ControlVariables.PRINT_MAILBOXES)
			Msg.info("Choreographer mailbox: '" + myMailbox + "'");
			
			this.enact();
	}

	private String findServiceMailbox(String entryServiceName) throws InterruptedException {
		wait(1000);
		String mailbox= ServiceRegistry.getInstance().findServiceMailBoxByServiceName(entryServiceName);
		return mailbox;
	}

	private void enact() throws MsgException {
		
		initRequests();
			
			ResponseTask response = (ResponseTask) getResponse(myMailbox);
			double startTime = response.requestServed.startTime;
			log.record(startTime, Msg.getClock(),response.serviceMethod);
			//Orchestration orch = orchestrationInstances.get(response.instanceId);
			if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS)
				Msg.info("Task " + response.serviceMethod
						+ " completed for instance " + response.instanceId);

			//orch.notifyTaskConclusion(response.requestServed);

		
		finalizeAll();

	}

	private void initRequests() throws MsgException {
		ChoreographyInstance chorInstance = ChoreographyMonitor.nextChoreographyInstance();
		Msg.info("initRequest ");
		WsRequest requestTask = new WsRequest(this.entryServiceName, this.entryServiceNameMethod 
												,this.inputMessageSize ,this.myMailbox);
		requestTask.setCompositionId(chorInstance.getCompositionId());
		invokeWsMethod(requestTask, myMailbox, entryMailbox);
		
	}

	@Override
	public void notifyCompletion(WsRequest request, ResponseTask response)
			throws MsgException {
		// TODO Auto-generated method stub
		if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS)
			Msg.info("Choreography  is done. Bye!");	
	}

	private void finalizeAll() throws MsgException {
		
		
		Msg.info("Ending choreography...");
		
		//FinalizeTask task = new FinalizeTask();
		//if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS || ControlVariables.PRINT_TASK_TRANSMISSION)
		//	Msg.info(" Sending termination signal to " + this.entryMailbox);
		//task.send(this.entryMailbox);
		for (String smb:ServiceRegistry.getInstance().getServiceMailboxes()) {
			FinalizeTask task = new FinalizeTask();
			if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS || ControlVariables.PRINT_TASK_TRANSMISSION)
				Msg.info(" Sending termination signal to " + smb);
			task.send(smb);
		}
		
		if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS)
			Msg.info("Choreography is done. Bye!");

		ServiceRegistry.getInstance().reset();
		this.log.close();
	}
	
	
}
