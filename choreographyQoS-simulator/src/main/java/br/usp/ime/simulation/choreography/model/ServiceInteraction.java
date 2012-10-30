package br.usp.ime.simulation.choreography.model;

import br.usp.ime.simulation.choreography.model.ChoreographyModel.ChoreographyElementType;
import br.usp.ime.simulation.datatypes.task.ResponseTask;
import br.usp.ime.simulation.datatypes.task.WsRequest;

public class ServiceInteraction{
	private String id;
	private String name;
	//private ServiceOperation serviceOp1;
	//private ServiceOperation serviceOperation;
	private WsRequest messageRequest;
	private ResponseTask messageResponse;//can be null
	private ChoreographyElementType nextElementType;  
}

