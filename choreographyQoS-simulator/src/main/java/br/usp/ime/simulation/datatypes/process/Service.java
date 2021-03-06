package br.usp.ime.simulation.datatypes.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.simgrid.msg.Host;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.HostNotFoundException;
import org.simgrid.msg.Msg;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.Process;
import org.simgrid.msg.Task;
import org.simgrid.msg.TimeoutException;
import org.simgrid.msg.TransferFailureException;

import br.usp.ime.simulation.choreography.ChoreographyInstance;
import br.usp.ime.simulation.datatypes.task.ResponseTask;
import br.usp.ime.simulation.datatypes.task.WsMethod;
import br.usp.ime.simulation.datatypes.task.WsRequest;
import br.usp.ime.simulation.experiments.control.ControlVariables;
import br.usp.ime.simulation.monitoring.ChoreographyMonitor;
import br.usp.ime.simulation.shared.ServiceInvoker;
import br.usp.ime.simulation.shared.ServiceRegistry;

import commTime.FinalizeTask;

//public class Service extends Process {
public class Service extends ServiceInvoker {

	//private HashMap<String, WsMethod> methods = new HashMap<String, WsMethod>();
	private String wsName;
	private List<String> workerMailboxes;
	private String[] mainArgs;
	private String myMailbox;
	private boolean ended = false;
	private volatile int lastUsedMailboxIndex=0;

	public Service(Host host, String name, String[]args) {
		super(host,name,args);
	} 

	public void main(String[] args) throws MsgException {
		if (args.length < 6) {
			Msg.info("Each service must have a name, an ammount of parallel threads and at least one method description");
			System.exit(1);
		}

		int workersNumber = Integer.parseInt(args[0]);
		this.wsName = args[1];
		//meanResponseTimeDegradation = args[2];
		
		mainArgs=Arrays.copyOf(args, args.length+2);

		myMailbox = "WS_" + wsName + "_at_" + getHost().getName();
		mainArgs[mainArgs.length-2]= "END";
		mainArgs[mainArgs.length-1]= myMailbox;
		
		
		ServiceRegistry.getInstance().putServiceMailbox(myMailbox);//service registring
		ServiceRegistry.getInstance().putServiceAndServiceMailbox(wsName, myMailbox);
		
		workerMailboxes = new ArrayList<String>();

		createWorkerThreads(workersNumber);

		if (ControlVariables.DEBUG || ControlVariables.PRINT_MAILBOXES)
			Msg.info("["+this.wsName+"] Receiving on '" + myMailbox + "' from Service '" + wsName
					+ "'");

		redirectTasks();

	}

	/*
	 * Critic method, it's responsible for message receiving and sending too  
	 */
	private void redirectTasks() {
		//int i = 0;
		Task currentTask;
		while (true) {
			try {
				//double startTime = Msg.getClock();
				//calculate here a response time
				currentTask = receiveNewTask();//trying to receive taks
				if(currentTask== null)
					Msg.info(">>>>>>>>>><["+this.wsName+"]: task received is NULL!!>>>>>>>>>>>>");
				processTask( currentTask);
			} catch (TransferFailureException e) {
				System.out.println("Transfer Exception ");
				e.printStackTrace();
			} catch (HostFailureException e) {
				System.out.println("Host Failure Exception ");
				e.printStackTrace();
			} catch (TimeoutException e) {
				System.out.println("Timeout Exception ");
				e.printStackTrace();
			} catch (MsgException e) {
				System.out.println("MSG Exception ");
				e.printStackTrace();
			}
			
			if (ended)
				break;
		}
	}

	private void processTask(Task currentTask)
			throws MsgException {
		
		if (currentTask instanceof WsRequest) {
			//verifying if it's a new Request
			WsRequest currentRequest = ((WsRequest)currentTask);
			
			String mailbox;
		
			//it isn't new request, then it's a dependency to be executed at other Service,  from a workthread (so same service)	
			if( !currentRequest.serviceName.equals(this.wsName) ){
				mailbox = ServiceRegistry.getInstance().findServiceMailBoxByServiceName(currentRequest.serviceName);
				if (ControlVariables.DEBUG || ControlVariables.PRINT_MAILBOXES){
					Msg.info("["+this.wsName+"]-- Request but, Finding a ready service for a dependent request: "+currentRequest+
							" created by "+currentRequest.senderMailbox+" Redirecting to " + mailbox);
				}
				invokeWsMethod(currentRequest,currentRequest.senderMailbox, mailbox);
			}
			else{// simple request from other Service 
				mailbox = getNextMailbox();
				if (ControlVariables.DEBUG || ControlVariables.PRINT_MAILBOXES)
					Msg.info("["+this.wsName+"]Request Task received:  " + currentRequest+ ". Redirecting to " + mailbox);
				redirectTask(currentTask, mailbox);
			}
		}
		else if (currentTask instanceof ResponseTask) {
			//TODO: redirect to respective worker to complete the initial request
			String initialRequesterMailbox = ((ResponseTask) currentTask).getInitialSender();
			Msg.info("["+this.wsName+"] Response Task received at service " + wsName
					+ ". Redirecting to " + initialRequesterMailbox+ " to complete execution");
			redirectTask(currentTask, initialRequesterMailbox);
		}
		else if (currentTask instanceof FinalizeTask) {
			if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS)
				Msg.info("["+this.wsName+"] Received Finalize. So this is WS_" + wsName + "_at_"
						+ getHost().getName() + " saying Goodbye!");

			finalizeWorkers();
			ended = true;
		}
	}


	private String getNextMailbox() {
		if (  this.lastUsedMailboxIndex < (workerMailboxes.size()-1)  )
			lastUsedMailboxIndex++;
		else
			lastUsedMailboxIndex = 0;

		String mailbox = workerMailboxes.get(lastUsedMailboxIndex);

		return mailbox;
	}

	private Task receiveNewTask() throws TransferFailureException,
			HostFailureException, TimeoutException {
		Task currentTask = Task.receive(myMailbox);
		if (ControlVariables.DEBUG || ControlVariables.PRINT_TASK_TRANSMISSION)
			Msg.info("["+this.wsName+"] Received task from " + currentTask.getSource().getName());
		return currentTask;
	}

	private void redirectTask(Task task, String mailbox)
			throws TransferFailureException, HostFailureException,
			TimeoutException {
		if (ControlVariables.DEBUG || ControlVariables.PRINT_TASK_TRANSMISSION)
			Msg.info("Redirecting to worker thread at " + mailbox);
		
		try{
			task.send(mailbox);
		}catch(Exception e){
			System.out.println("Error sending: "+ e.getMessage());
			e.printStackTrace();
		}

	}

	private void createWorkerThreads(int workerAmmount) throws HostNotFoundException {
		for (int workerId = 0; workerId < workerAmmount; workerId++) {
			createsingleWorker(workerId);
		}
		if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS)
			Msg.info("Done creating worker threads.");
	}

	private void createsingleWorker(int workerThreadId) throws HostNotFoundException {
		String[] arguments = mainArgs.clone();
		arguments[0] = myMailbox + "_WORKER_THREAD_" + workerThreadId;
		workerMailboxes.add(arguments[0]);
		(new WorkerThread(arguments, getHost())).start();
	}

	private void finalizeWorkers() {
		for (String mailbox : workerMailboxes) {
			try {
				(new FinalizeTask()).send(mailbox);
			} catch (Exception e) {
				Msg.info("Could not finalize worker thread at " + mailbox);
			}
		}
	}

	@Override
	public void notifyCompletion(WsRequest request, ResponseTask response)
			throws MsgException {
		// TODO Auto-generated method stub
		
	}
}
