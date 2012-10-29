package br.usp.ime.simulation.datatypes.task;

import java.util.HashMap;
import java.util.Map;

import org.simgrid.msg.Task;

public class WsMethod extends Task {

	private int computingSizeInMI;
	private double inputFileSizeInBytes;
	private double outputFileSizeInBytes;
	private String wsMethodName;
	private String serviceName;
	private Map<String, WsMethod> dependencies;
	
	//private Map<String, WsMethod> executedDependencies;
	private boolean isSynchronous;
	private boolean wasExecuted;
	
	public enum MessageInteractionType{ 
		Request, Request_Response, Response;
	}
	private Map<String, MessageInteractionType> MITypesDependencies;
	
	private MessageInteractionType messageInteractionType;
	
	public WsMethod(String wsMethodName, double averageComputeDuration, double inputFileSize, double outputFileSize) {
		super(wsMethodName, averageComputeDuration, (inputFileSize + outputFileSize));
		this.inputFileSizeInBytes =  inputFileSize;
		this.outputFileSizeInBytes = outputFileSize;
		this.wsMethodName = wsMethodName;
		this.setSynchronous(true);
		
		dependencies = new HashMap<String, WsMethod>();
	}

	public boolean hasDependency(){
		return (this.dependencies != null && !dependencies.isEmpty());
	}
	
	public void addDependency(String service, WsMethod Method){
		this.dependencies.put(service, Method);
	}
	
	public int getComputingSizeInMI() {
		return computingSizeInMI;
	}

	public double getInputFileSizeInBytes() {
		return inputFileSizeInBytes;
	}

	public double getOutputFileSizeInBytes() {
		return outputFileSizeInBytes;
	}

	public String getWsMethodName() {
		return wsMethodName;
	}

	public void setWsMethodName(String wsMethodName) {
		this.wsMethodName = wsMethodName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public Map<String, WsMethod> getDependencies() {
		return dependencies;
	}

	public boolean isSynchronous() {
		return isSynchronous;
	}

	public void setSynchronous(boolean isSynchronous) {
		this.isSynchronous = isSynchronous;
	}

	public MessageInteractionType getMessageInteractionType() {
		return messageInteractionType;
	}

	public void setMessageInteractionType(MessageInteractionType messageInteractionType) {
		this.messageInteractionType = messageInteractionType;
	}

	public boolean wasExecuted() {
		return wasExecuted;
	}

	public void setWasExecuted(boolean wasExecuted) {
		this.wasExecuted = wasExecuted;
	}	

}
