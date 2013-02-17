package br.usp.ime.simulation.choreography.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.usp.ime.simulation.choreography.ChoreographyParser;
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
		roleServices.put(s.getName(), s);
	}

	public static void generateChoreographyModel(String file){
		ChoreographyParser parser= new ChoreographyParser(file);
		parser.generateChoreographyModel();
	}
	
	public static ServiceOperation findServiceOperation(String servicename, String serviceOperationName){
		if(roleServices.get(servicename)==null){
			System.out.println("Service "+servicename+" not found");
			return null;
		}
		//String serviceOperationKey = servicename+"_"+serviceOperationName;
		//return roleServices.get(servicename).findServiceOperationByKey(serviceOperationKey);
		return roleServices.get(servicename).findServiceOperationByName(serviceOperationName);
	}
	
	
	public static Map<String, Service> getRoleServices() {
		return roleServices;
	}
	
	


	public static  void setRoleServices(Map<String, Service> roleServices) {
		roleServices = roleServices;
	}

}
