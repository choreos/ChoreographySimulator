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

	private HashMap<String, WsMethod> methods = new HashMap<String, WsMethod>();
	private Host host;
	private String wsName;
	private List<String> workerMailboxes;
	private String[] mainArgs;
	private String myMailbox;
	private boolean ended = false;
	private int lastUsedMailboxIndex=0;

	public Service(Host host, String name, String[]args) {
		super(host,name,args);
	} 

	public void main(String[] args) throws MsgException {
		if (args.length < 6) {
			Msg.info("Each service must have a name, an ammount of parallel threads and at least one method description");
			System.exit(1);
		}

		
		/*if (((args.length - 2) % 3) != 0) {
			Msg.info("Each method must have 4 parameters: a name, computing size and output file size");
			System.exit(1);
		}*/

		//there will be no verifying of args
		
		
		int workersNumber = Integer.parseInt(args[0]);
		this.wsName = args[1];
		
		mainArgs=Arrays.copyOf(args, args.length+2);
		

		myMailbox = "WS_" + wsName + "_at_" + getHost().getName();
		mainArgs[mainArgs.length-2]= "END";
		mainArgs[mainArgs.length-1]= myMailbox;
		
		
		
		//System.out.println("mainArgs size : "+mainArgs.length+" from "+args.length);
		//System.out.println("mailbox: "+mainArgs[args.length]);
		//System.out.println("END: "+mainArgs[args.length+1]);
		
		
		ServiceRegistry.getInstance().putServiceMailbox(myMailbox);//service registring
		ServiceRegistry.getInstance().putServiceAndServiceMailbox(wsName, myMailbox);
		
		workerMailboxes = new ArrayList<String>();

		createWorkerThreads(workersNumber);

		if (ControlVariables.DEBUG || ControlVariables.PRINT_MAILBOXES)
			Msg.info("Receiving on '" + myMailbox + "' from Service '" + wsName
					+ "'");

		redirectTasks();

	}

	private void redirectTasks() {
		//int i = 0;
		Task currentTask;
		while (true) {
			try {
				double startTime = Msg.getClock();
				//calculate here a response time
				currentTask = receiveNewTask();//trying to receive taks
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
			}
			
			if (ended)
				break;
		}
	}

	private void processTask(Task currentTask)
			throws TransferFailureException, HostFailureException,
			TimeoutException {
		if (currentTask instanceof WsRequest) {
			//verifying if it's a new Request
			WsRequest currentRequest = ((WsRequest)currentTask);
			
			String mailbox;
			System.out.println("Service request to : "+currentRequest.serviceName);

		
			//it isn't new request, then it's a dependency to be executed at other Service,  from a workthread (so same service)	
			if( !currentRequest.serviceName.equals(this.wsName) ){
				Msg.info("Service: finding a ready service for a dependent request: "+currentRequest.getId()+" to service "+currentRequest.serviceName);				
				mailbox = ServiceRegistry.getInstance().findServiceMailBoxByServiceName(currentRequest.serviceName);
			}
			else// simple request from other Service 
				mailbox = getNextMailbox();
			
			if (ControlVariables.DEBUG || ControlVariables.PRINT_MAILBOXES)
				Msg.info("Request Task received at service " + wsName
						+ ". Redirecting to " + mailbox);
			redirectTask(currentTask, mailbox);
		}
		else if (currentTask instanceof ResponseTask) {
			//TODO: redirect to respective worker to complete the initial request
			String initialRequesterMailbox = ((ResponseTask) currentTask).getInitialSender();
			Msg.info("Response Task received at service " + wsName
					+ ". Redirecting to " + initialRequesterMailbox+ " to complete execution");
			redirectTask(currentTask, initialRequesterMailbox);
		}
		else if (currentTask instanceof FinalizeTask) {
			if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS)
				Msg.info("Received Finalize. So this is WS_" + wsName + "_at_"
						+ getHost().getName() + " saying Goodbye!");

			finalizeWorkers();
			ended = true;
		}
	}

	//private String getNextMailbox(int lastUsedMailboxIndex) {
	private String getNextMailbox() {
		int j;
		if (this.lastUsedMailboxIndex >= workerMailboxes.size())
			lastUsedMailboxIndex = 0;
		else
			lastUsedMailboxIndex++;

		String mailbox = workerMailboxes.get(lastUsedMailboxIndex);

		return mailbox;
	}

	private Task receiveNewTask() throws TransferFailureException,
			HostFailureException, TimeoutException {
		Task currentTask = Task.receive(myMailbox);
		if (ControlVariables.DEBUG || ControlVariables.PRINT_TASK_TRANSMISSION)
			Msg.info("Received task from " + currentTask.getSource().getName());
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
