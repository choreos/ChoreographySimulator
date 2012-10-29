package br.usp.ime.simulation.datatypes.process;

import java.util.HashMap;

import org.simgrid.msg.Host;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.Msg;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.Process;
import org.simgrid.msg.Task;
import org.simgrid.msg.TimeoutException;
import org.simgrid.msg.TransferFailureException;

import br.usp.ime.simulation.datatypes.task.ResponseTask;
import br.usp.ime.simulation.datatypes.task.WsMethod;
import br.usp.ime.simulation.datatypes.task.WsMethod.MessageInteractionType;
import br.usp.ime.simulation.datatypes.task.WsRequest;
import br.usp.ime.simulation.experiments.control.ControlVariables;

import commTime.FinalizeTask;

public class WorkerThread extends Process {

	private HashMap<String, WsMethod> methods;
	private Host host;
	private String wsName;
	private String myMailbox;
	private String myServiceMailbox;
	
	private HashMap<String, WsMethod>  outstandingExecutionMethods = new HashMap<String, WsMethod>();

	public WorkerThread(String[] mainArgs, Host host) {
		super(host, "WsRequestSender", mainArgs);
	}

	@Override
	public void main(String[] args) throws MsgException {
		wsName = args[0];
		myMailbox = args[1];
		methods = new HashMap<String, WsMethod>();
		this.myServiceMailbox= args[args.length-1];
		// host = getHost();

		createWebMethods(args);

		if (ControlVariables.DEBUG || ControlVariables.PRINT_MAILBOXES)
			Msg.info("Receiving on '" + myMailbox + "'");

		workerThreadExecution();

	}

	private void createWebMethods(String[] args) {
		//for (int i = 2; i < args.length; i += 3) {
		for (int i = 2; i < args.length-1; i += 3) {
			createMethod(args[i], args[i + 1], args[i + 2]);
		}
	}

	private void workerThreadExecution() throws TransferFailureException,
			HostFailureException, TimeoutException, MsgException {
		while (true) {
			double startTime = Msg.getClock();
			Task task = Task.receive(myMailbox);
			if (ControlVariables.DEBUG
					|| ControlVariables.PRINT_TASK_TRANSMISSION)
				Msg.info("WorkerThread: Received task from " + task.getSource().getName());
			if (task instanceof WsRequest) {
				Msg.info("WorkerThread: task instanceof WsRequest, to execute " );
				WsRequest wsRequest = (WsRequest) task;
				
				wsRequest.startTime = startTime;
				executeMethod(wsRequest);
			}
			else if(task instanceof ResponseTask){
				
			}

			else if (task instanceof FinalizeTask)
				break;
		}
	}

	public void executeMethod(WsRequest request) throws MsgException {
		WsMethod currentMethod = requestWsMethodTask(request.serviceMethod);
		
		if(currentMethod.hasDependency()){
			for (WsMethod dependentMethod : currentMethod.getDependencies().values()){
				
				WsRequest requestDependentTask = new WsRequest(dependentMethod.getServiceName(),
						dependentMethod.getName(), dependentMethod.getInputFileSizeInBytes(),this.myMailbox);
				if(!dependentMethod.wasExecuted())
					redirectTask(requestDependentTask);//redirecting Task to Service
				//is needed a waiting response?
				//if( currentMethod.isSynchronous() ){
				if( currentMethod.getMessageInteractionType()==MessageInteractionType.Request ){
					currentMethod.execute();		
					currentMethod.setWasExecuted(true);
				}
				else{
					Msg.info("Waiting a response of dependent service ");
					this.outstandingExecutionMethods.put(currentMethod.getName(),currentMethod);
					
				}
				
			}
		}
		else{
			currentMethod.execute();
			currentMethod.setWasExecuted(true);
			if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS)
				Msg.info("Task completed");
			String responseMailbox = request.senderMailbox;
			double outputFileSize = currentMethod.getOutputFileSizeInBytes();
			sendResponseTask(request, responseMailbox, outputFileSize);//currently send directly to a Service Invoker
			//but should be to send to a Service, and frome there to send to a ServiceInvoker
		}
	}

	private void sendResponseTask(WsRequest request, String responseMailbox,
			double outputFileSize) throws TransferFailureException,
			HostFailureException, TimeoutException {
		ResponseTask response = new ResponseTask(outputFileSize);
		response.serviceName = wsName;
		response.instanceId = request.instanceId;
		response.requestServed = request;
		response.serviceMethod = request.serviceMethod;
		if (ControlVariables.DEBUG || ControlVariables.PRINT_TASK_TRANSMISSION)
			Msg.info("Sending response from " + request.destination);
		response.send(responseMailbox);
	}

	public WsMethod requestWsMethodTask(String wsMethodName) {
		if (ControlVariables.DEBUG)
			Msg.info("WorkerThread: requestWsMethodTask, retrieving the method: "+wsMethodName + " of "+this.methods.size()+" methods");
		WsMethod method = methods.get(wsMethodName);//critic point
		if(method== null && ControlVariables.DEBUG)
			Msg.info("WorkerThread: requestWsMethodTask - method null!");
		
		WsMethod cloneMethod = new WsMethod(method.getName(),
				method.getComputeDuration(), 0,
				method.getOutputFileSizeInBytes());
		return cloneMethod;
	}

	private void redirectTask(Task task)
			throws TransferFailureException, HostFailureException,
			TimeoutException {
		if (ControlVariables.DEBUG || ControlVariables.PRINT_TASK_TRANSMISSION)
			Msg.info("WorkerThread: Redirecting to service " + myServiceMailbox);
		task.send(myServiceMailbox);

	}
	private void createMethod(String name, String computeSize,
			String outputFileSize) {
		WsMethod method = new WsMethod(name, Double.parseDouble(computeSize),
				0, Double.parseDouble(outputFileSize));

		Msg.info("WorkerThread: createMethod : "+name);
		methods.put(name, method);
	}

}
