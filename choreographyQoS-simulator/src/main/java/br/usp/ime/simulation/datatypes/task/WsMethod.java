package br.usp.ime.simulation.datatypes.task;

import java.util.HashMap;
import java.util.Map;

import org.simgrid.msg.Task;

import br.usp.ime.simulation.choreography.model.ChoreographyModel.MessageInteractionType;

public class WsMethod extends Task {

	//private int computingSizeInMI;
	private double inputFileSizeInBytes=0.001;
	private double outputFileSizeInBytes=0.001;
	private String wsMethodName;
	private String serviceName;
	private Map<String, WsMethod> dependencies;//key : servicename_methodName
	
	//private boolean isSynchronous;
	
	
	public WsMethod(String serviceName, String wsMethodName, double averageComputeDuration, double inputFileSize, double outputFileSize) {
		super(wsMethodName, averageComputeDuration, (inputFileSize + outputFileSize));
			this.inputFileSizeInBytes =  inputFileSize;
			this.outputFileSizeInBytes = outputFileSize;
			this.wsMethodName = wsMethodName;
			this.serviceName = serviceName;
			this.dependencies = new HashMap<String, WsMethod>();
		//this.miTypesDependencies = new HashMap<String, WsMethod.MessageInteractionType>();
	}

	
	public boolean hasDependency(){
		return (this.dependencies != null && !dependencies.isEmpty());
	}
	
	/*
	 * key=serviceName_serviceMethodName
	 */
	public void addDependency(String methodName, WsMethod Method){
		this.dependencies.put(methodName, Method);
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


	
	
}
