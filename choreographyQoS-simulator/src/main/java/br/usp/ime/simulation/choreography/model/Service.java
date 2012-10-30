package br.usp.ime.simulation.choreography.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Service{

	private Integer id;
	private static int nextId=0;
	private String name;
	//private List<ServiceOperation> serviceOperations;
	private Map<String, ServiceOperation> serviceOperations;
	
	public Service(String name){
		this.name=name;
		this.id= Service.nextId();
		//this.serviceOperations = new ArrayList<ServiceOperation>();
		this.serviceOperations = new HashMap<String, ServiceOperation>();//name, SO
	}
	
	private static int nextId(){
		return nextId++;
	}
	public String getId() {
		return id.toString()+"_"+this.name;
	}
	
	public void addServiceOperation(ServiceOperation so){
		//this.serviceOperations.add(so);
		this.serviceOperations.put(so.getName(), so);
	}


	public ServiceOperation findServiceOperationByName(String operationName) {
		return this.serviceOperations.get(operationName);
	}
	public void removeServiceOperation(ServiceOperation so){
		this.serviceOperations.remove(so.getName());
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public boolean equals(Object s){
		if (s == null)
            return false;
		return this.getId().equals( ((Service)s).getId()  );
	}
	

	public Map<String, ServiceOperation> getServiceOperations() {
		return serviceOperations;
	}

	public void setServiceOperations(Map<String, ServiceOperation> serviceOperations) {
		this.serviceOperations = serviceOperations;
	}

}
