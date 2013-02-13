package br.usp.ime.simulation.datatypes.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.ExponentialDistribution;
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
import br.usp.ime.simulation.monitoring.QoSInfo;
import br.usp.ime.simulation.qos_model.QoSParameter;
import br.usp.ime.simulation.qos_model.QoSParameter.QoSAttribute;
import br.usp.ime.simulation.choreography.ManagerRequest;

import commTime.FinalizeTask;

public class WorkerThread extends Process {

	private HashMap<String, WsMethod> methods;
	private String wsName;
	private String myMailbox;
	private String myServiceMailbox;
	private Double meanResponseTimeDegradation; 
	
	private ExponentialDistribution exponentialDistribution;
	
    //only of dependencies with request_response type
	private Map<Integer, WsMethod>  outstandingExecutionMethods = new HashMap<Integer, WsMethod>();//< idWsRequest, wsmethod >
	
	
	public WorkerThread(String[] mainArgs, Host host) {
		super(host, "WsRequestSender", mainArgs);
		//this.exponentialDistribution = new ExponentialDistribution(10000);//with a mean of 10s
	}

	@Override
	public void main(String[] args) throws MsgException {
		
		this.myMailbox = args[0];
		this.wsName = args[1];
		
		this.meanResponseTimeDegradation = Double.valueOf(args[2]);
		this.exponentialDistribution = new ExponentialDistribution(this.meanResponseTimeDegradation);//with a mean of 10s
		
		this.methods = new HashMap<String, WsMethod>();
		
		this.myServiceMailbox= args[args.length-1];
		// host = getHost();

		createWebMethods(args);
		

		if (ControlVariables.DEBUG || ControlVariables.PRINT_MAILBOXES)
			Msg.info("Receiving on '" + myMailbox + "'");

		workerThreadExecution();

	}

	/*
	 * 
	 */
	private void createWebMethods(String[] args) {
		int initialIndex=3;//method position
		
		for (int i = initialIndex; i < args.length; ) {
			System.out.println("Arg["+i+"]= "+args[i]);
			if( !args[i].equals("method") ){
				Msg.info("WorkerThread: Error parsing at method creating, not found method value tag, i="+i);
				break;
			}
			//The Response size is default, for customized response size, will be defined into dependency specification (TODO)
			WsMethod currentMethod= createMethod(this.wsName, args[i+1], args[i + 2], "0", args[i + 3]);//request Size is only defined in dependency
			this.methods.put(currentMethod.getServiceName()+"_"+currentMethod.getName(), currentMethod);
			i=i+4;
			//System.out.println("Arg["+i+"]= "+args[i]);
			
			if(args[i].equals("dependency")){//only a single dependency
				System.out.println("Have dependency");
				WsMethod dependentMethod= createMethod(args[i+1], args[i + 2], args[i + 3], args[i + 4], "0");//TODO would be customed response size 
				//currentMethod.addDependency(dependentMethod.getServiceName(), dependentMethod);
				currentMethod.addDependency(dependentMethod.getServiceName()+"_"+dependentMethod.getName(), dependentMethod);
				System.out.println("dependency: "+ dependentMethod.getName()+" of service "+ dependentMethod.getServiceName());
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
	
	/*
	 * 
	 */
	private void workerThreadExecution() throws TransferFailureException,
			HostFailureException, TimeoutException, MsgException {
		while (true) {
			//double startTime = Msg.getClock();
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

				//%%%%  adding a value according to a probability distribution?
				
				ChoreographyInstance chorInstance= ChoreographyMonitor.findChoreographyInstance(responseTask.requestServed.getCompositionId());
				//Msg.info("WorkerThread: chorInstance: "+chorInstance.getCompositionId());
				
				
				ManagerRequest managerRequest =chorInstance.getManagerRequest();
				WsRequest parentRequest= managerRequest.getParentDependency(responseTask.requestServed);
				//Msg.info("WorkerThread: parentrequest id: "+parentRequest.getId());

				System.out.println("Parent request: Service name= "+parentRequest.serviceName+" , Service Method = "+parentRequest.serviceMethod);
				ServiceOperation so = ChoreographyModel.findServiceOperation(parentRequest.serviceName.trim(), parentRequest.serviceMethod.trim());
				System.out.println("So= "+so.getName());
				//The Inclusive or join gateways is implicit for now for REQUEST_RESPONSE interactions
				switch(so.getGatewayTypeToDependencies()){
				//switch(parentRequest.){
					case SEQUENCE_FLOW:
						Msg.info("WorkerThread: Response from SEQUENCE_FLOW" );
						handleResponseParallelAndSequenceGateway(so, responseTask, parentRequest, managerRequest);
						break;
					case  EXCLUSIVE://data-based
						Msg.info("WorkerThread: Response for EXCLUSIVE " );
						 handleResponseExclusiveGateway(so, responseTask, parentRequest, managerRequest);
						 break;
					case PARALLEL:
						Msg.info("WorkerThread: Response for PARALLEL " );
						handleResponseParallelAndSequenceGateway(so, responseTask, parentRequest, managerRequest);
						break;
					//case INCLUSIVE:
						//Msg.info("WorkerThread: INCLUSIVE " );
						//TODO
						//break;
					default :
						Msg.info("WorkerThread: response for SEQUENCE (default)" );
						//handleSequenceFlow(so,currentMethod, request);
						handleResponseParallelAndSequenceGateway(so, responseTask, parentRequest, managerRequest);
				}
			}
			else if (task instanceof FinalizeTask){
				    //this.kill();
					break;
			}
		}//end of while
	}

	
	/*
	 * 
	 */
	private void handleResponseParallelAndSequenceGateway(ServiceOperation so,
			ResponseTask responseTask, WsRequest parentRequest, ManagerRequest managerRequest) throws HostFailureException, TaskCancelledException, TransferFailureException, TimeoutException {
		//Msg.info("WorkerThread:handling received response: 
		//response of a dependent method from a current request(responseTask.requestServed)?
		if(  parentRequest!=null &&  responseTask.requestServed.done ){ //

			Msg.info("WorkerThread: It was completed the dependency: "+responseTask.serviceName+":"+responseTask.serviceMethod);
			//next, execute or waiting to complete the another request dependencies according to gateway
			managerRequest.notifyDependentTaskConclusion(responseTask.requestServed);//
			
			if(managerRequest.areCompletedRequestDependencies(parentRequest)){ //complete and ready to finally be executed
				Msg.info("WorkerThread: "+parentRequest.serviceName+":"+parentRequest.serviceMethod+" ready to execute, finally");
				WsMethod pendingMethod = this.outstandingExecutionMethods.get(parentRequest.id);
				if(pendingMethod==null)
					Msg.info("WorkerThread: There is no pending method of request "+parentRequest.id);

				// >>> QoS measurements // Event #3 (receiving response of dependent service)
				//getting time response of first response
				QoSInfo qosInfoDependency = managerRequest.getQoSInfoOf(responseTask.requestServed);
				//TIME RESPONSE of QoSInfo dependent
				Double dependentResponseTime = Msg.getClock() - responseTask.requestServed.startTime; //Double endTime = Msg.getClock();
				QoSParameter qosResponseTime = new QoSParameter(QoSAttribute.RESPONSE_TIME);
				qosResponseTime.setValue(dependentResponseTime);
				qosInfoDependency.setQoSParamInResponseOf(QoSAttribute.RESPONSE_TIME, qosResponseTime);//internally knows if is composed
				Msg.info("Event 3: Dependent Response time: <"+dependentResponseTime+">");
				
				// >>> QoS measurements // Event #4 (executing and sending response to original consumer)
				QoSInfo qosInfoParent = managerRequest.getQoSInfoOf(parentRequest);
				Double partialExecutionTime	= directExecuteMethod(parentRequest, pendingMethod);//executing
				Double currentExecutionTime = partialExecutionTime+dependentResponseTime;
				QoSParameter qosCurrentExecutionTime = new QoSParameter(QoSAttribute.EXECUTION_TIME);
				qosResponseTime.setValue(currentExecutionTime);
				qosInfoParent.setQoSParamInRequestOf(QoSAttribute.EXECUTION_TIME, qosCurrentExecutionTime);//current execution time
				managerRequest.notifyTaskConclusion(parentRequest);//notifying
				Msg.info("Event 4: partial Execution time: <"+partialExecutionTime+">");
				Msg.info("Event 4: Current Execution time: <"+currentExecutionTime+">");
				
			}
			else{
				//waiting completion of remainder dependencies 
			}
			
		}
		else{// request don't completed
			
		}
		
	}

	
	
	/*
	 * 
	 */
	private void handleResponseExclusiveGateway(ServiceOperation so,
			ResponseTask responseTask, WsRequest parentRequest, ManagerRequest managerRequest) throws HostFailureException, TaskCancelledException, TransferFailureException, TimeoutException {
		//Msg.info("WorkerThread:handling received response: 
		//response of a dependent method from a current request(responseTask.requestServed)?
		if(  parentRequest!=null &&  responseTask.requestServed.done ){ //

			Msg.info("WorkerThread: It was completed the dependency: "+responseTask.serviceName+":"+responseTask.serviceMethod);
			//next, execute or waiting to complete the another request dependencies according to gateway
			managerRequest.notifyDependentTaskConclusion(responseTask.requestServed);//
			
			Msg.info("WorkerThread-EXCLUSIVE: "+parentRequest.serviceName+":"+parentRequest.serviceMethod+" ready to execute, finally");
			WsMethod pendingMethod = this.outstandingExecutionMethods.get(parentRequest.id);
			
			if(pendingMethod==null)
				Msg.info("WorkerThread: There is no pending method of request "+parentRequest.id);
			
			// >>> QoS measurements // Event #3 (receiving response of dependent service)
			//getting time response of first response
			QoSInfo qosInfoDependency = managerRequest.getQoSInfoOf(responseTask.requestServed);
			//TIME RESPONSE of QoSInfo dependent
			Double dependentResponseTime = Msg.getClock() - responseTask.requestServed.startTime; //Double endTime = Msg.getClock();
			QoSParameter qosResponseTime = new QoSParameter(QoSAttribute.RESPONSE_TIME);
			qosResponseTime.setValue(dependentResponseTime);
			qosInfoDependency.setQoSParamInResponseOf(QoSAttribute.RESPONSE_TIME, qosResponseTime);//internally knows if is composed
			Msg.info("Event 4: Dependent Response time: <"+dependentResponseTime+">");
			
			// >>> QoS measurements // Event #4 (executing and sending response to original consumer)
			QoSInfo qosInfoParent = managerRequest.getQoSInfoOf(parentRequest);
			Double partialExecutionTime	= directExecuteMethod(parentRequest, pendingMethod);//executing
			Double currentExecutionTime = partialExecutionTime+dependentResponseTime;
			QoSParameter qosCurrentExecutionTime = new QoSParameter(QoSAttribute.EXECUTION_TIME);
			qosResponseTime.setValue(currentExecutionTime);
			qosInfoParent.setQoSParamInRequestOf(QoSAttribute.EXECUTION_TIME, qosCurrentExecutionTime);//current execution time
			managerRequest.notifyTaskConclusion(parentRequest);//notifying
			Msg.info("Event 4: partial Execution time: <"+partialExecutionTime+">");
			Msg.info("Event 4: Current Execution time: <"+currentExecutionTime+">");
			
		}
		else{// request don't completed
			
		}
		
	}

	
	/*
	 * 
	 */
	private Double directExecuteMethod(WsRequest request, WsMethod method) throws HostFailureException, TaskCancelledException, TransferFailureException, TimeoutException {

		Double startTime = Msg.getClock();
		method.execute();
		//method.getComputeDuration();
		//%%%%  adding a value according to a probability distribution?
		Double additionalExecutionTime = this.exponentialDistribution.sample();
		sleep(additionalExecutionTime.longValue());
		/*try {
			//wait(additionalExecutionTime.longValue());
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println(" ******* Exception wait a additional execuion Time!");
			e.printStackTrace();
		}*/
		
		
		Double endTime = Msg.getClock();
		Msg.info("Execution Time of "+request +" = "+(endTime-startTime));
		//Msg.info("Execution Time of "+request +" = "+(endTime-startTime)+additionalEsecutionTime);
		
		//currentMethod.setWasExecuted(true);
		if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS)
			Msg.info("Task completed");
		
		if(request.getMessageInteraction()==MessageInteractionType.Request)//don't need response
			return null;
		
		String responseMailbox = request.senderMailbox;
		Double outputFileSize=0.001;

		if(method.getServiceName().equals("WS1") && method.getName().equals("method1")){
			if(ChoreographyMonitor.getResponseSizeOf("WS1_method1")==null)
				outputFileSize = method.getOutputFileSizeInBytes();
			else
				outputFileSize = ChoreographyMonitor.getResponseSizeOf("WS1_method1");
		}
		else
			outputFileSize = method.getOutputFileSizeInBytes();
		
		sendResponseTask(request, responseMailbox, outputFileSize);//currently send directly to a Service Invoker, and a response size is default
		this.outstandingExecutionMethods.remove(request.getId());
		
		return (endTime-startTime);
		//return (endTime-startTime)+additionalEsecutionTime;
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
		//WsMethod currentMethod = requestWsMethodTask(request.serviceMethod);
		WsMethod currentMethod = requestWsMethodTask(request.serviceName+"_"+request.serviceMethod);
		
		ServiceOperation so = ChoreographyModel.findServiceOperation(currentMethod.getServiceName(), currentMethod.getName());
		
		//Event #1: receiving request, QoS Measurement Communication time 1, latency, bw of request; but for now not
		
		//if(currentMethod.hasDependency()){
		if(so.getDependencies() !=null && !so.getDependencies().isEmpty()){//have event #2 and event #3 
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
			//Service without dependencies have no event #2 nor #3, only #1 and #4; and it's event #4 (sending response)		
			// >>> QoS measurements // Event #4 (executing and sending response to original consumer)
			ManagerRequest managerRequest = ChoreographyMonitor.findChoreographyInstance(request.getCompositionId()).getManagerRequest();
			
			QoSInfo qosInfo = managerRequest.getQoSInfoOf(request);
			Double currentExecutionTime	= directExecuteMethod(request, currentMethod);//executing
			QoSParameter qosCurrentExecutionTime = new QoSParameter(QoSAttribute.EXECUTION_TIME);
			qosCurrentExecutionTime.setValue(currentExecutionTime);
			qosInfo.setQoSParamInRequestOf(QoSAttribute.EXECUTION_TIME, qosCurrentExecutionTime);//current execution time
			//managerRequest.notifyTaskConclusion(request);//notifying
			Msg.info("Event 4: Current Execution time: <"+currentExecutionTime+">");
		}
	}//end 


	
	private void handleSequenceFlow(ServiceOperation so, WsMethod currentMethod, WsRequest currentRequest) throws TransferFailureException, HostFailureException, TimeoutException, TaskCancelledException {

		System.out.println("Dependencies SO: "+so.getDependencies().size());
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
			//at add dependency the a dependency QoSInfo is creted by managerRequest 
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
		
		ChoreographyInstance chorInstance= ChoreographyMonitor.findChoreographyInstance(currentRequest.getCompositionId());
	
		for (WsMethod dependentMethod : currentMethod.getDependencies().values()){

				WsRequest requestDependentTask = new WsRequest(dependentMethod.getServiceName(),
						dependentMethod.getName(), dependentMethod.getInputFileSizeInBytes(),this.myMailbox);
				
				requestDependentTask.setCompositionId(currentRequest.getCompositionId());
				requestDependentTask.startTime= Msg.getClock();//initial time for communication time, and response time
				
				//System.out.println("ManagerRequest: current list: "+chorInstance.getManagerRequest().getRequests() );
				String serviceOperationKey= dependentMethod.getServiceName()+"_"+dependentMethod.getName();
				
				//if( so.getMiTypeDependencies().get(dependentMethod.getName())==MessageInteractionType.Request_Response){
				if( so.getMiTypeDependencies().get(serviceOperationKey)==MessageInteractionType.Request_Response){
	
					Msg.info("["+this.myMailbox+"]"+" Waiting a response of dependent service for "+currentRequest+", new request: "+requestDependentTask.toString());
	
					//adding dependency
					chorInstance.getManagerRequest().addDependency(currentRequest, requestDependentTask);//in order to waiting a response			
					if(this.outstandingExecutionMethods.get(currentRequest.getId()) ==null)
						this.outstandingExecutionMethods.put(currentRequest.getId(), currentMethod);
					
					redirectTask(requestDependentTask);//redirecting Task to Service
				}
				else{
					Msg.info("No Waiting, only was needed send and there is no response (async) ");
					redirectTask(requestDependentTask);//redirecting Task to Service
				}
		
		}//for
		
		if(this.outstandingExecutionMethods.get(currentRequest.getId()) ==null){
			//Only Requests with no response: then to execute currentRequest
			directExecuteMethod(currentRequest, currentMethod);
		}

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

	/*
	 * 
	 */
	public WsMethod requestWsMethodTask(String keyMethod) {
		if (ControlVariables.DEBUG)
			Msg.info("WorkerThread: requestWsMethodTask, retrieving the method: "+keyMethod + " of "+this.methods.size()+" methods");
		//WsMethod method = methods.get(wsMethodName);//critic point
		WsMethod method = methods.get(keyMethod);//critic point
		if(method== null && ControlVariables.DEBUG)
			Msg.info("WorkerThread: requestWsMethodTask - method null!");
		
		WsMethod cloneMethod = new WsMethod(method.getServiceName(),method.getName(),
				method.getComputeDuration(), method.getInputFileSizeInBytes(),
				method.getOutputFileSizeInBytes());
		//cloning dependencies
		for(WsMethod dependency: method.getDependencies().values()){
			WsMethod cloneMethodDependency=new WsMethod(dependency.getServiceName(), dependency.getName(),
					dependency.getComputeDuration(), dependency.getInputFileSizeInBytes(), dependency.getOutputFileSizeInBytes());
			cloneMethod.addDependency(cloneMethodDependency.getName(), cloneMethodDependency);
		}
		return cloneMethod;
	}

	/*
	 * 
	 */
	private void redirectTask(Task task)
			throws TransferFailureException, HostFailureException,
			TimeoutException {
		if (ControlVariables.DEBUG || ControlVariables.PRINT_TASK_TRANSMISSION)
			Msg.info("WorkerThread: Redirecting to service" + myServiceMailbox);
		//System.out.println("Trying to send to "+myServiceMailbox);
		//QoS measurements Event #2 : throughput, but not for now 
		task.send(myServiceMailbox);
	}
	
	/*
	 * 
	 */
	private WsMethod createMethod(String serviceName, String name, String computeSize, 
			String outputFileSize) {
		WsMethod method = new WsMethod(serviceName,name, Double.parseDouble(computeSize),
				0, Double.parseDouble(outputFileSize));//inputSize = 0
		
		//String serviceOperationKey= method.getServiceName()+"_"+method.getName();
		Msg.info("WorkerThread: createMethod : "+name);
		return method;
	}

	/*
	 * 
	 */
	private WsMethod createMethod(String serviceName, String name, String computeSize,
			String inputFileSize, String outputFileSize) {
		WsMethod method = new WsMethod(serviceName,name, Double.parseDouble(computeSize),
				Double.parseDouble(inputFileSize), Double.parseDouble(outputFileSize));//inputSize = 0
		
		//String serviceOperationKey= method.getServiceName()+"_"+method.getName();
		Msg.info("WorkerThread: createMethod : "+name);
		return method;
	}

	public Double getMeanResponseTimeDegradation() {
		return meanResponseTimeDegradation;
	}

	public void setMeanResponseTimeDegradation(
			Double meanResponseTimeDegradation) {
		this.meanResponseTimeDegradation = meanResponseTimeDegradation;
	}
	
}
