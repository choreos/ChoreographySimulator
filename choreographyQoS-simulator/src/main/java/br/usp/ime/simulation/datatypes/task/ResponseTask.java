package br.usp.ime.simulation.datatypes.task;

import org.simgrid.msg.Task;


public class ResponseTask extends Task {

	public int instanceId;
	//private Long compositionId;
	public String serviceName;
	public String serviceMethod;
	public WsRequest requestServed;
	
	public ResponseTask(double outputFileSize) {
		super("", 0, outputFileSize);
	}
	
	public String getInitialSender(){
		return requestServed.senderMailbox;
	}

	
}

