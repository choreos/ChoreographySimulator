package br.usp.ime.simulation.choreography.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.usp.ime.simulation.datatypes.task.ResponseTask;
import br.usp.ime.simulation.datatypes.task.WsRequest;

public class ChoreographyModel {

	public enum GatewayType{ 
		PARALLEL, EXCLUSIVE, EVENT_BASED, INCLUSIVE, COMPLEX, JOIN, SEQUENCE_FLOW;
	}
	
	public enum MessageInteractionType{ 
		Request, Request_Response, Response;
	}
	
	public enum EventType{ 
		Request, Request_Response, Response;
	}
	
	public enum ChoreographyElementType{
		EVENT, SERVICE_INTERACTION, GATEWAY 
	}
	
	
	
	//private ServiceInteraction firstInteraction;

	private static ChoreographyModel instance;
	private static Map<String, Service> roleServices = new HashMap<String, Service>();//key=serviceId
	
	
//	public ChoreographyModel ChoreographyModel(){
//		if(instance ==null )
//			this.instance= new ChoreographyModel();
//		return instance;
//	}
	
	
	
	public static void addService(Service s){
		roleServices.put(s.getId(), s);
	}

	
	public static ServiceOperation findServiceOperation(String servicename, String operationName){
		if(roleServices.get(servicename)==null)
			return null;
		return roleServices.get(servicename).findServiceOperationByName(operationName);
	}
	
	
	public static Map<String, Service> getRoleServices() {
		return roleServices;
	}
	
	


	public static  void setRoleServices(Map<String, Service> roleServices) {
		roleServices = roleServices;
	}

}
