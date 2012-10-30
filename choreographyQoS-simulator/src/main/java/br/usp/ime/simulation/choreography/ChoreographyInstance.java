package br.usp.ime.simulation.choreography;

import java.util.Map;

import br.usp.ime.simulation.choreography.model.Service;
import br.usp.ime.simulation.choreography.model.ServiceOperation;

public class ChoreographyInstance {
	
	private Long compositionId;
	private ServiceOperation initialServiceOperation;//initials?
	private Service initialService;//initials?
	private ManagerRequest managerRequest;
	
	//List of participant services

	public ChoreographyInstance() {
		this.managerRequest = new ManagerRequest();
	}
	
	public Long getCompositionId() {
		return compositionId;
	}
	
	public void setCompositionId(Long compositionId) {
		this.compositionId = compositionId;
	}
	
	public ServiceOperation getInitialServiceOperation() {
		return initialServiceOperation;
	}
	public void setInitialServiceOperation(ServiceOperation initialServiceOperation) {
		this.initialServiceOperation = initialServiceOperation;
	}

	public ManagerRequest getManagerRequest() {
		return managerRequest;
	}

	public void setManagerRequest(ManagerRequest managerRequest) {
		this.managerRequest = managerRequest;
	}

	public Service getInitialService() {
		return initialService;
	}

	public void setInitialService(Service initialService) {
		this.initialService = initialService;
	}
	
	
}
