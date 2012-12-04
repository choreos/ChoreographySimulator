package br.usp.ime.simulation.choreography;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.simgrid.msg.Msg;

import br.usp.ime.simulation.datatypes.task.WsRequest;
import br.usp.ime.simulation.monitoring.QoSInfo;
import br.usp.ime.simulation.qos_model.QoSParameter;
import br.usp.ime.simulation.qos_model.QoSParameter.QoSAttribute;

public class ManagerRequest {
	private Map<Integer, WsRequest> requests;
	private Map<WsRequest, Map<Integer, WsRequest>>  dependsOn;//depende de
	//private Map<WsRequest, Map<Integer, WsRequest>> isDependencyOf;//é dependencia de, many parents
	//private Map<WsRequest, WsRequest> isDependencyOf;//only one parent
	private Map<Integer, WsRequest> isDependencyOf;//only one parent

	private Map<Integer, QoSInfo> qosInformations;//key : requestId
	private Map<QoSInfo, Map<Integer, QoSInfo> >  qosInformationsDependencies;//depende de
	

	public ManagerRequest() {
		this.dependsOn = new HashMap<WsRequest, Map<Integer,WsRequest>>();
		this.isDependencyOf = new HashMap<Integer, WsRequest>();//new HashMap<WsRequest, WsRequest>();
		this.requests = new HashMap<Integer, WsRequest>();
		
		this.qosInformations = new HashMap<Integer, QoSInfo>();
		this.qosInformationsDependencies = new HashMap<QoSInfo, Map<Integer,QoSInfo>>();
		
	}

	public void addRequest(WsRequest request) {
		requests.put(request.getId(),request);
		dependsOn.put(request, new HashMap<Integer, WsRequest>() );
		//isDependencyOf.put(request, new HashMap<Integer, WsRequest>() );
		//this.isDependencyOf.put(request.getId(), null);
		
		QoSInfo qosInfo= new QoSInfo();
		qosInfo.setRequest(request);
		this.qosInformations.put(request.getId(), qosInfo);
		this.qosInformationsDependencies.put(qosInfo, new HashMap<Integer, QoSInfo>());
	}

	public Map<Integer, WsRequest> getRequests() {
		return requests;
	}

	public void setRequests(Map<Integer, WsRequest> requests) {
		this.requests = requests;
	}

	public void addDependency(WsRequest request, WsRequest dependency) {
		if (requests.get(request.getId())==null) {
			Msg.info("Error adding dependency to non-existing request: "+request.getId());
			return;
		}

		this.dependsOn.get(request).put(dependency.getId(),dependency);
		
		//if(this.isDependencyOf.get(dependency)==null){
		//if (requests.get(dependency.getId())==null) {
			//Msg.info("Error adding dependency, non-existing dependencyOf "+dependency.getId());
		//}	
		//isDependencyOf.get(dependency).put(request.getId(),request);
		//this.isDependencyOf.put(dependency, request);
		this.isDependencyOf.put(dependency.getId(), request);
		
		if (qosInformations.get(request.getId())==null) {
			Msg.info("Error adding dependency QoSInfo to non-existing request: "+request.getId());
			return;
		}
		QoSInfo infoDependency = new QoSInfo();
		infoDependency.setComposed(true);//that is, your attributes will be composed, accordingly
		infoDependency.setRequest(request);
		this.qosInformationsDependencies.get(  this.qosInformations.get(request.getId())  ).put(dependency.getId(),  infoDependency);
	}

	
	public boolean findRequest(WsRequest request) {
		if (this.requests.get( request.getId() ) == null)
			return false;
		
		return true;
	}
	/**
	 * Taks ready to execute because don't have dependencies
	 * @return
	 */
	public List<WsRequest> getReadyTasks() {
		final List<WsRequest> readyTasks = new ArrayList<WsRequest>();

		for (WsRequest request : this.requests.values()) {
			if (isReadyTask(request)) {
				readyTasks.add(request);
			}
		}
		return readyTasks;
	}

	/**
	 * Task ready to execute because don't have dependencies
	 * @param wsRequest
	 * @return
	 */
	private boolean isReadyTask(WsRequest wsRequest) {
		if (dependsOn.get(wsRequest) != null
				&& dependsOn.get(wsRequest).isEmpty() && !wsRequest.done)
			return true;

		return false;
	}

	/**
	 * 
	 * @param request
	 * @param setRequest
	 */
	//private void doneWsRequest(WsRequest request,Set<WsRequest> setRequest){
	@Deprecated
	private void doneWsRequest(WsRequest request, Set<WsRequest> requests){
		for(WsRequest i: requests){
			if(i.equals(request)){
				i.done = true;
				break;
			}
		}
		
	}

	public void notifyTaskConclusion(WsRequest request) {
		this.dependsOn.remove(request);//removing all dependencies
		//this.isDependencyOf
		this.requests.remove(request.getId());
	}
	
	public void notifyDependentTaskConclusion(WsRequest dependency) {
		
		WsRequest parent = this.isDependencyOf.get(dependency);
		
		if (parent != null) {//it's a dependency 
			//doneWsRequest(request,isDependencyOf.keySet());
			
			//for (WsRequest parent : isDependencyOf.get(request).values() ) {
			//for (WsRequest parent : isDependencyOf.get(request) ) {
			//removeThisRequestsDependencyOn(this.isDependencyOf.get(dependency), dependency);
			
			markDependencyAsDone(parent, dependency);
			this.requests.remove(dependency);
			this.isDependencyOf.remove(dependency).getId();
			
		}
		else{
			Msg.info("Could not find inputted request (" + dependency.toString() + ")");
			//for(WsRequest req : isDependencyOf.keySet()){
			for(Integer reqId : isDependencyOf.keySet()){
				//Msg.info("key: " + req.toString());
				Msg.info("key: " + reqId.toString());
			}
		}
	}
	
	private void markDependencyAsDone(WsRequest parent, WsRequest dependency){
		dependsOn.get(parent).get(dependency.getId()).done=true;
		
	}


		
	private void removeThisRequestsDependencyOn(WsRequest parent, WsRequest dependency) {
		dependsOn.get(parent).remove(dependency.getId());
	}

	public WsRequest getParentDependency(WsRequest request) {
		return this.isDependencyOf.get(request.getId());
	}
	
	public Map<Integer, WsRequest> getRequestDependencies(WsRequest request){
		return this.dependsOn.get(request.getId());
	}
	
	public boolean areCompletedRequestDependencies(WsRequest req){
		for ( WsRequest dependency : this.dependsOn.get(req).values()){
			if (dependency.done=false)
				return false;
		}
				
		return true;
	}

	public Map<Integer, WsRequest> getIsDependencyOf() {
		return isDependencyOf;
	}

	public void setIsDependencyOf(Map<Integer, WsRequest> isDependencyOf) {
		this.isDependencyOf = isDependencyOf;
	}

	/*
	 * 
	
	public void setValueOfQoSParam(WsRequest request, QoSAttribute executionTime, Double value) {
		
		for (QoSInfo qos: this.qosInformations.keySet()){
			if(qos.getRequest().getId()== request.getId())
				qos.setQoSParameterValueOf(executionTime, value);
		}
	}*/

	public QoSInfo getQoSInfoOf(WsRequest request){
//		for (QoSInfo qos: this.qosInformationsDependencies.keySet()){
//			if(qos.getRequest().getId()== request.getId())
//				return qos;
//		}
		
		return this.qosInformations.get(request.getId());
	}
	
	public Map<Integer, QoSInfo> QoSInfoDependenciesOf(WsRequest request){
		QoSInfo qosInfo= this.qosInformations.get(request.getId());
		if(qosInfo==null)
			return null;
		return this.qosInformationsDependencies.get(qosInfo);
	}
	
	
//	
//public void setValueOfQoSParam(WsRequest request, QoSParameter param) {
//		
//		for (QoSInfo qos: this.qosInformations.keySet()){
//			if(qos.getRequest().getId()== request.getId())
//				qos.setQoSParameterValueOf(param.getQoSAttribute(), param);
//		}
//	}

	
}
