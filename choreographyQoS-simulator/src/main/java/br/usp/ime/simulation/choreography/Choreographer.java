package br.usp.ime.simulation.choreography;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.simgrid.msg.Host;
import org.simgrid.msg.Msg;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.Process;
import org.simgrid.msg.Task;

import commTime.FinalizeTask;

import br.usp.ime.simulation.choreography.model.ChoreographyModel;
import br.usp.ime.simulation.datatypes.task.ResponseTask;
import br.usp.ime.simulation.datatypes.task.WsMethod;
import br.usp.ime.simulation.datatypes.task.WsRequest;
import br.usp.ime.simulation.experiments.control.ControlVariables;
import br.usp.ime.simulation.experiments.control.Statistics;
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
	private Integer nro_requests=1;
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

		int numberOfInstances=1;
		this.nro_requests = ChoreographyMonitor.getNumberRequests();
		this.log.open("sim_chor_"+this.nro_requests+".log");
		Statistics.openDataset();
		
		ControlVariables.DEBUG =true; ControlVariables.PRINT_ALERTS=true;
		
		
		
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
		//wait(1000);
		String mailbox= ServiceRegistry.getInstance().findServiceMailBoxByServiceName(entryServiceName);
		return mailbox;
	}

	private void enact() throws MsgException {
		
		fireRequests();
		
		int pendingRequests= this.nro_requests;
		while(pendingRequests>0){
			ResponseTask response = (ResponseTask) getResponse(myMailbox);
			
			if(response==null){
				System.out.println("Response Null!");
				continue;
			}
			
			Double startTime = response.requestServed.startTime;
			Double finishTime = Msg.getClock();
			Statistics.statsResponseTime.addValue(finishTime-startTime);
			log.record(startTime, finishTime,response.serviceMethod);
			//log.record(start, finish,response.serviceMethod);
			//System.out.println("TR: "+(finish-start));
			//System.out.println("<"+ startTime+" , "+Msg.getClock()+">");
			
			if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS)
				Msg.info("Task " + response.serviceMethod
						+ " completed for instance " + response.instanceId);
	
			//orch.notifyTaskConclusion(response.requestServed);
			pendingRequests--;
		}
		
		Statistics.recordDescriptiveStatistics(this.nro_requests);
		
		finalizeAll();

	}

	private void fireRequests() throws MsgException {

		//sending concurrent requests
		for (int i = 0; i< this.nro_requests; i++){
			ChoreographyInstance chorInstance = ChoreographyMonitor.nextChoreographyInstance();
			Msg.info("Choreography Instance ID:  "+chorInstance.getCompositionId());
			Msg.info("initRequest ");
			
			WsRequest requestTask = new WsRequest(this.entryServiceName, this.entryServiceNameMethod 
													,this.inputMessageSize ,this.myMailbox);
			requestTask.setCompositionId(chorInstance.getCompositionId());
			requestTask.startTime= Msg.getClock();
			invokeWsMethod(requestTask, myMailbox, entryMailbox);
		}
		
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
		Statistics.closeDataset();
	}
	
	
	
}
