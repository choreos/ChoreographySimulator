package br.usp.ime.simulation.datatypes.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simgrid.msg.Host;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.Msg;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.Process;
import org.simgrid.msg.Task;
import org.simgrid.msg.TaskCancelledException;
import org.simgrid.msg.TimeoutException;
import org.simgrid.msg.TransferFailureException;

import br.usp.ime.simulation.choreography.ChoreographyInstance;
import br.usp.ime.simulation.choreography.model.ChoreographyModel;
import br.usp.ime.simulation.choreography.model.ChoreographyModel.GatewayType;
import br.usp.ime.simulation.choreography.model.ChoreographyModel.MessageInteractionType;
import br.usp.ime.simulation.choreography.model.ServiceOperation;
import br.usp.ime.simulation.datatypes.task.ResponseTask;
import br.usp.ime.simulation.datatypes.task.WsMethod;
import br.usp.ime.simulation.datatypes.task.WsRequest;
import br.usp.ime.simulation.experiments.control.ControlVariables;
import br.usp.ime.simulation.monitoring.ChoreographyMonitor;
import br.usp.ime.simulation.choreography.ManagerRequest;

import commTime.FinalizeTask;

public class WorkerThread extends Process {

	private HashMap<String, WsMethod> methods;
	private String wsName;
	private String myMailbox;
	private String myServiceMailbox;
	
    //only of dependencies with request_response type
	private Map<Integer, WsMethod>  outstandingExecutionMethods = new HashMap<Integer, WsMethod>();//< idWsRequest, wsmethod >
	
	
	public WorkerThread(String[] mainArgs, Host host) {
		super(host, "WsRequestSender", mainArgs);
	}

	@Override
	public void main(String[] args) throws MsgException {
		wsName = args[1];
		myMailbox = args[0];
		methods = new HashMap<String, WsMethod>();
		
		this.myServiceMailbox= args[args.length-1];
		// host = getHost();

		createWebMethods(args);
		

		if (ControlVariables.DEBUG || ControlVariables.PRINT_MAILBOXES)
			Msg.info("Receiving on '" + myMailbox + "'");

		workerThreadExecution();

	}

	private void createWebMethods(String[] args) {

//		System.out.println("Args lenght: "+args.length);
//		System.out.println("Arg["+0+"]= "+args[0]);
//		System.out.println("Arg["+1+"]= "+args[1]);
//		System.out.println("Arg["+(args.length-2)+"]= "+args[args.length-2]);
//		System.out.println("Arg["+(args.length-1)+"]= "+args[args.length-1]);
//		
		
		for (int i = 2; i < args.length; ) {
			System.out.println("Arg["+i+"]= "+args[i]);
			if( !args[i].equals("method") ){
				Msg.info("WorkerThread: Error parsing at method creating, not found method value tag, i="+i);
				break;
			}	
			WsMethod currentMethod= createMethod(this.wsName, args[i+1], args[i + 2], args[i + 3]);
			this.methods.put(currentMethod.getName(), currentMethod);
			
			i=i+4;
			System.out.println("Arg["+i+"]= "+args[i]);
			
			if(args[i].equals("dependency")){//only a single dependency
				System.out.println("Have dependency");
				WsMethod dependentMethod= createMethod(args[i+1], args[i + 2], args[i + 3], args[i + 4]);
				//currentMethod.addDependency(dependentMethod.getServiceName(), dependentMethod);
				currentMethod.addDependency(dependentMethod.getName(), dependentMethod);
				System.out.println("dependencie: "+ dependentMethod.getName()+" of service "+ dependentMethod.getServiceName());
				//currentMethod.addDependency(dependentMethod.getServiceName(), dependentMethod);
				i=i+5;
				System.out.println("Arg["+i+"]= "+args[i]);
			}
			if(args[i].equals("END") ){
				System.out.println("END");
				break;
			}	
		}
			
		
		
	}

	private void workerThreadExecution() throws TransferFailureException,
			HostFailureException, TimeoutException, MsgException {
		while (true) {
			double startTime = Msg.getClock();
			Task task = Task.receive(myMailbox);
			
			//if (ControlVariables.DEBUG || ControlVariables.PRINT_TASK_TRANSMISSION)
				Msg.info("["+this.myMailbox+"] Received task, sender " + task.getSource().getName());//etSender());
			if (task instanceof WsRequest) {
				
				WsRequest wsRequest = (WsRequest) task;
				Msg.info("["+this.myMailbox+"] Receiving the request  "+wsRequest+" , to execute " );
				wsRequest.startTime = startTime;
				executeMethod(wsRequest);
			}
			else if(task instanceof ResponseTask){
				//TODO receive dependent response Task (ok or not) to complete outstanding execution
				Msg.info("["+this.myMailbox+"] task instanceof ResponseTask, so, verifying if there are dependencies" );
				ResponseTask responseTask = ((ResponseTask) task);
				//if responseTask.requestServed.done==false ?

				//if(outstandingExecutionRequests.get( responseTask.requestServed.getId() )!=null){//
				//if(outstandingExecutionRequests.get( responseTask.requestServed.getId() )!=null){////response of a dependent method from a current request?

				//Msg.info("WorkerThread: original request of response: "+responseTask.requestServed.getId());
				ChoreographyInstance chorInstance= ChoreographyMonitor.findChoreographyInstance(responseTask.requestServed.getCompositionId());
				//Msg.info("WorkerThread: chorInstance: "+chorInstance.getCompositionId());
				
				
				WsRequest parentRequest= chorInstance.getManagerRequest().getParentDependency(responseTask.requestServed);
				//Msg.info("WorkerThread: parentrequest id: "+parentRequest.getId());

				
				//Msg.info("WorkerThread:handling received response: 
				//response of a dependent method from a current request(responseTask.requestServed)?
				if(  parentRequest!=null &&  responseTask.requestServed.done ){ //

					Msg.info("WorkerThread: It was completed the dependency: "+responseTask.serviceName+":"+responseTask.serviceMethod);
					//next, execute or waiting to complete the another request dependencies according to gateway
					ManagerRequest managerRequest = chorInstance.getManagerRequest();
					managerRequest.notifyDependentTaskConclusion(responseTask.requestServed);//
					
					if(managerRequest.areCompletedRequestDependencies(parentRequest)){ //complete and ready to finally be executed
						Msg.info("WorkerThread: "+parentRequest.serviceName+":"+parentRequest.serviceMethod+" ready to execute, finally");
						WsMethod pendingMethod = this.outstandingExecutionMethods.get(parentRequest.id);
						if(pendingMethod==null)
							Msg.info("WorkerThread: There is no pending method of request "+parentRequest.id);
						directExecuteMethod(parentRequest, pendingMethod);//executing
						managerRequest.notifyTaskConclusion(parentRequest);//notifying
					}
					else{
						//waiting completion of remainder dependencies 
					}
					
				}
				else{// request don't completed
					
				}
				
			}

			else if (task instanceof FinalizeTask)
				break;
		}
	}

	
	private void directExecuteMethod(WsRequest request, WsMethod method) throws HostFailureException, TaskCancelledException, TransferFailureException, TimeoutException {

		method.execute();
		//currentMethod.setWasExecuted(true);
		if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS)
			Msg.info("Task completed");
		String responseMailbox = request.senderMailbox;
		double outputFileSize = method.getOutputFileSizeInBytes();
		sendResponseTask(request, responseMailbox, outputFileSize);//currently send directly to a Service Invoker
		this.outstandingExecutionMethods.remove(request.getId());
		//but should be to send to a Service, and from there to send to a ServiceInvoker
	}

	
	/*
	 * 
	 */
	public void executeMethod(WsRequest request) throws MsgException {
		
		ChoreographyInstance chorInstance = ChoreographyMonitor.findChoreographyInstance(request.getCompositionId());
		System.out.println("chorInstance ID : " +chorInstance.getCompositionId()+", request: "+
					request.id+":"+request.serviceName+"-"+request.getName()+ " ,  at "+this.myMailbox);
		chorInstance.getManagerRequest().addRequest(request); //new request to execute
		WsMethod currentMethod = requestWsMethodTask(request.serviceMethod);
		
		ServiceOperation so = ChoreographyModel.findServiceOperation(currentMethod.getServiceName(), currentMethod.getName());
		
		//if(currentMethod.hasDependency()){
		if(so.getDependencies() !=null && !so.getDependencies().isEmpty()){
			GatewayType gateway= so.getGatewayTypeToDependencies();//currentMethod.getGateway();
			if(so.isSequential()){
				Msg.info("WorkerThread: SEQUENCE " );
				handleSequenceFlow(so, currentMethod, request);
			}
			else{
				switch(gateway){
					case EXCLUSIVE://data-based
						Msg.info("WorkerThread: EXCLUSIVE " );
						 handleExclusiveGateway(currentMethod, request);
						 break;
					case PARALLEL:
						Msg.info("WorkerThread: PARALLEL " );
						handleParallelGateway(so, currentMethod, request);
						break;
					case INCLUSIVE:
						Msg.info("WorkerThread: INCLUSIVE " );
						//TODO
						break;
					case JOIN:
						//TODO
						Msg.info("WorkerThread: JOIN " );
						break;
					case EVENT_BASED:
						//TODO
						Msg.info("WorkerThread: EVENT_BASED " );
						break;
					default :
						Msg.info("WorkerThread: SEQUENCE (default)" );
						handleSequenceFlow(so,currentMethod, request);
				}
			}	
			//only when all requests are asynchronous
			//this.outstandingExecutionMethods.put(request.getId(), currentMethod);
		}
		else{//there is no dependencies
			System.out.println("Request without dependencies, so execute it ");
			directExecuteMethod(request, currentMethod);
		}
	}//end 


	
	private void handleSequenceFlow(ServiceOperation so, WsMethod currentMethod, WsRequest currentRequest) throws TransferFailureException, HostFailureException, TimeoutException, TaskCancelledException {

		System.out.println("Dependencies: "+currentMethod.getDependencies().size());
		//System.out.println("Dependencies SO: "+so.getDependencies().size());
		//System.out.println("current Method: "+currentMethod.getName()+" of service "+currentMethod.getServiceName());
		WsMethod dependentMethod = currentMethod.getDependencies().values().iterator().next();

		WsRequest requestDependentTask = new WsRequest(dependentMethod.getServiceName(),
				dependentMethod.getName(), dependentMethod.getInputFileSizeInBytes(),this.myMailbox);
		
		ChoreographyInstance chorInstance= ChoreographyMonitor.findChoreographyInstance(currentRequest.getCompositionId());
		requestDependentTask.setCompositionId(currentRequest.getCompositionId());
		
		//chorInstance.getManagerRequest().addRequest(requestDependentTask);//it was maked at Service redirecting
		
		System.out.println("ManagerRequest: current list: "+chorInstance.getManagerRequest().getRequests() );

		
		
		String serviceOperationKey= dependentMethod.getServiceName()+"_"+dependentMethod.getName();
		
		
		//if( so.getMiTypeDependencies().get(dependentMethod.getName())==MessageInteractionType.Request_Response){
		if( so.getMiTypeDependencies().get(serviceOperationKey)==MessageInteractionType.Request_Response){

			Msg.info("["+this.myMailbox+"]"+" Waiting a response of dependent service for "+currentRequest+", new request: "+requestDependentTask.toString());

			//adding dependency
			chorInstance.getManagerRequest().addDependency(currentRequest, requestDependentTask);//in order to waiting a response			
			this.outstandingExecutionMethods.put(currentRequest.getId(), currentMethod);
			redirectTask(requestDependentTask);//redirecting Task to Service
		}
		else{
			Msg.info("No Waiting, only was needed send and there is no response (async), then can be execute ");
			//Only Request with no response: then to execute currentRequest
			//To improve this implementation, to think in this question: What to do when the interaction is async?
			redirectTask(requestDependentTask);//redirecting Task to Service
			directExecuteMethod(currentRequest, currentMethod);
		}
	}

	
	private void handleParallelGateway(ServiceOperation so, WsMethod currentMethod, WsRequest currentRequest) throws HostFailureException, TaskCancelledException, TransferFailureException, TimeoutException {
		// TODO Auto-generated method stub
		for (WsMethod dependentMethod : currentMethod.getDependencies().values()){
			WsRequest requestDependentTask = new WsRequest(dependentMethod.getServiceName(),
					dependentMethod.getName(), dependentMethod.getInputFileSizeInBytes(),this.myMailbox);
			requestDependentTask.setCompositionId(currentRequest.getCompositionId());
			//String serviceOperationKey= dependentMethod.getServiceName()+"_"+dependentMethod.getName();
			//if( currentMethod.getMIType(serviceOperationKey)==MessageInteractionType.Request_Response ){
			if(so.getMiTypeDependencies().get(dependentMethod.getName())==MessageInteractionType.Request_Response){

				Msg.info("Waiting a response of dependent service: "+dependentMethod.getServiceName());
				//String outstandingRequestKey=  currentRequest.instanceId+"_cu" ;
				//this.outstandingExecutionRequests.put(currentRequest.getId(),currentRequest);
				//addOutstandingResponse(currentRequest, requestDependentTask);
				
			}
			else{
				//Only Request with no response: Nothing to do
				//What to do when the interaction is asyn?????
			}
			
			redirectTask(requestDependentTask);//redirecting Task to Service
			
				
		}//for

	}

	private void handleExclusiveGateway(WsMethod currentMethod, WsRequest currentRequest) {
		// TODO Auto-generated method stub
	}

	private void sendResponseTask(WsRequest request, String responseMailbox,
			double outputFileSize) throws TransferFailureException,
			HostFailureException, TimeoutException {
		ResponseTask response = new ResponseTask(outputFileSize);
		response.serviceName = wsName;
		response.instanceId = request.getId();
		response.requestServed = request;
		response.serviceMethod = request.serviceMethod;
		if (ControlVariables.DEBUG || ControlVariables.PRINT_TASK_TRANSMISSION)
			Msg.info("Sending response from " + request.destination);//TODO destination?
		response.requestServed.done=true;//TODO verify?
		response.send(responseMailbox);
	}

	//public WsMethod requestWsMethodTask(String wsMethodName) {
	public WsMethod requestWsMethodTask(String wsMethodName) {
		if (ControlVariables.DEBUG)
			Msg.info("WorkerThread: requestWsMethodTask, retrieving the method: "+wsMethodName + " of "+this.methods.size()+" methods");
		//WsMethod method = methods.get(wsMethodName);//critic point
		WsMethod method = methods.get(wsMethodName);//critic point
		if(method== null && ControlVariables.DEBUG)
			Msg.info("WorkerThread: requestWsMethodTask - method null!");
		
		WsMethod cloneMethod = new WsMethod(method.getServiceName(),method.getName(),
				method.getComputeDuration(), 0,
				method.getOutputFileSizeInBytes());
		//cloning dependencies
		for(WsMethod dependency: method.getDependencies().values()){
			WsMethod cloneMethodDependency=new WsMethod(dependency.getServiceName(), dependency.getName(),
					dependency.getComputeDuration(), 0, dependency.getOutputFileSizeInBytes());
			cloneMethod.addDependency(cloneMethodDependency.getName(), cloneMethodDependency);
		}
		return cloneMethod;
	}

	private void redirectTask(Task task)
			throws TransferFailureException, HostFailureException,
			TimeoutException {
		if (ControlVariables.DEBUG || ControlVariables.PRINT_TASK_TRANSMISSION)
			Msg.info("WorkerThread: Redirecting to service" + myServiceMailbox);
		//System.out.println("Trying to send to "+myServiceMailbox);
		task.send(myServiceMailbox);

	}
	private WsMethod createMethod(String serviceName, String name, String computeSize,
			String outputFileSize) {
		WsMethod method = new WsMethod(serviceName,name, Double.parseDouble(computeSize),
				0, Double.parseDouble(outputFileSize));
		
		//String serviceOperationKey= method.getServiceName()+"_"+method.getName();
		Msg.info("WorkerThread: createMethod : "+name);
		return method;
	}
	
	
}
