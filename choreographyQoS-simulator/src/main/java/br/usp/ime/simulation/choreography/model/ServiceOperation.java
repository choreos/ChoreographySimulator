package br.usp.ime.simulation.choreography.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.usp.ime.simulation.choreography.model.ChoreographyModel.GatewayType;
import br.usp.ime.simulation.choreography.model.ChoreographyModel.MessageInteractionType;
import br.usp.ime.simulation.choreography.model.Service;
import br.usp.ime.simulation.choreography.model.ServiceOperation;

public class ServiceOperation{
	//private String id;
	private Service service;
	private String name;
	
	private GatewayType gatewayTypeToDependencies=null;
	//private List<ServiceOperation> dependencies =new ArrayList<ServiceOperation>();//K=servicedependencyId+"_"+serviceOperationName
	private Map<String, ServiceOperation> dependencies =new HashMap<String, ServiceOperation>();
	//private Map<MessageInteractionType, Map<String, ServiceOperation> > dependenciesGroupsByMIType;//
	private Map<String, MessageInteractionType> miTypeDependencies =new HashMap<String, ChoreographyModel.MessageInteractionType>();
	 
	
	public ServiceOperation(Service service, String name){
		this.service= service;
		this.name = name;
		//this.serviceOperationsDependencies = new HashMap<ChoreographyModel.MessageInteractionType, ServiceOperation>();
		//this.dependenciesGroupsByMIType = new HashMap<ChoreographyModel.MessageInteractionType, Map<String,ServiceOperation>>();
		
	}
	
	
	public GatewayType getGatewayTypeToDependencies() {
		return gatewayTypeToDependencies;
	}


	public boolean isSequential(){
		return (this.gatewayTypeToDependencies ==null || this.gatewayTypeToDependencies==GatewayType.SEQUENCE_FLOW );
	}
	
	public void addDependencies(ServiceOperation so, MessageInteractionType miType){
		
		//if(this.dependenciesGroupsByMIType.get(miType)==null)
			//this.dependenciesGroupsByMIType.put(miType, new HashMap<String, ServiceOperation>());
			
		//this.dependencies.put(so.name, so);
		//this.miTypeDependencies.put(so.name, miType);
		this.dependencies.put(so.getKey(), so);
		this.miTypeDependencies.put(so.getKey(), miType);
		
		//this.dependenciesGroupsByMIType.get(miType).put(so.getName(), so);
	}
	
	
	/*
	 * return a Key to be used as key in the dependencies map
	 */
	public String getKey() {
		return this.getService().getName()+"_"+this.getName();
	}


	public Map<String, ServiceOperation> getDependencies() {
		return dependencies;
	}


	public void setDependencies(Map<String, ServiceOperation> dependencies) {
		this.dependencies = dependencies;
	}


	public Map<String, MessageInteractionType> getMiTypeDependencies() {
		return miTypeDependencies;
	}


	public void setMiTypeDependencies(
			Map<String, MessageInteractionType> miTypeDependencies) {
		this.miTypeDependencies = miTypeDependencies;
	}


	public String getId() {
		return this.service.getId()+"_"+this.getService().getName()+"_"+this.name;
	}
	
	public Service getService() {
		return service;
	}
	public void setService(Service service) {
		this.service = service;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setGatewayTypeToDependencies(GatewayType type){
		this.gatewayTypeToDependencies= type;
	}
	
}
