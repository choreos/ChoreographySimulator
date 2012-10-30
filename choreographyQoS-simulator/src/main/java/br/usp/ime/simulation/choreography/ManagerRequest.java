package br.usp.ime.simulation.choreography;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.simgrid.msg.Msg;

import br.usp.ime.simulation.datatypes.task.WsRequest;

public class ManagerRequest {
	private Map<Integer, WsRequest> requests;
	private Map<WsRequest, Map<Integer, WsRequest>>  dependsOn;//depende de
	//private Map<WsRequest, Map<Integer, WsRequest>> isDependencyOf;//é dependencia de, many parents
	private Map<WsRequest, WsRequest> isDependencyOf;//only one parent

	//private static ManagerRequest instance=null;
	
//	public static ManagerRequest getInstance(){
//		if(instance==null)
//			instance= new ManagerRequest();
//		return instance;
//	}
	
	public ManagerRequest() {
		dependsOn = new HashMap<WsRequest, Map<Integer,WsRequest>>();
		//isDependencyOf = new HashMap<WsRequest, Map<Integer,WsRequest>>();
		this.isDependencyOf = new HashMap<WsRequest, WsRequest>();
		requests = new HashMap<Integer, WsRequest>();
	}

	public void addRequest(WsRequest request) {
		requests.put(request.getId(),request);
		dependsOn.put(request, new HashMap<Integer, WsRequest>() );
		//isDependencyOf.put(request, new HashMap<Integer, WsRequest>() );
		this.isDependencyOf.put(request, null);
	}

	public void addDependency(WsRequest request, WsRequest dependency) {
		if (requests.get(request.getId())==null) {
			Msg.info("Error adding dependency to non-existing request");
			return;
		}

		this.dependsOn.get(request).put(dependency.getId(),dependency);
		
		if(this.isDependencyOf.get(dependency)==null){
			Msg.info("Error adding dependency, non-existing dependencyOf");
		}	
		//isDependencyOf.get(dependency).put(request.getId(),request);
		this.isDependencyOf.put(dependency, request);
	}

	
	public boolean findRequest(WsRequest request) {
		return this.requests.get(request)!=null;
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
	
	public void notifyDependentTaskConclusion(WsRequest dependency) {
		
		WsRequest parent = this.isDependencyOf.get(dependency);
		
		if (parent != null) {//it's a dependency 
			//doneWsRequest(request,isDependencyOf.keySet());
			
			//for (WsRequest parent : isDependencyOf.get(request).values() ) {
			//for (WsRequest parent : isDependencyOf.get(request) ) {
			//removeThisRequestsDependencyOn(this.isDependencyOf.get(dependency), dependency);
			
			markDependencyAsDone(parent, dependency);
			this.requests.remove(dependency);
			this.isDependencyOf.remove(dependency);
			
			//}
		}
		else{
			Msg.info("Could not find inputted request (" + dependency.toString() + ")");
			for(WsRequest req : isDependencyOf.keySet()){
				Msg.info("key: " + req.toString());
			}
		}
	}
	
	private void markDependencyAsDone(WsRequest parent, WsRequest dependency){
		dependsOn.get(parent).get(dependency.getId()).done=true;
		
	}

		
	private void removeThisRequestsDependencyOn(WsRequest parent, WsRequest dependency) {
		dependsOn.get(parent).remove(dependency.getId());
	}

	public WsRequest getParentDependency(WsRequest requestServed) {
		return this.isDependencyOf.get(requestServed);
	}
	
	public Map<Integer, WsRequest> getRequestDependencies(WsRequest request){
		return this.dependsOn.get(request.id);
	}
	
	public boolean areCompletedRequestDependencies(WsRequest req){
		for ( WsRequest dependency : this.dependsOn.get(req).values()){
			if (dependency.done=false)
				return false;
		}
				
		return true;
	}

	
}
