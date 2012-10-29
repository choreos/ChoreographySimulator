package br.usp.ime.simulation.shared;

import java.io.IOException;

import org.simgrid.msg.Host;
import org.simgrid.msg.HostFailureException;
import org.simgrid.msg.HostNotFoundException;
import org.simgrid.msg.Msg;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.Process;
import org.simgrid.msg.Task;
import org.simgrid.msg.TransferFailureException;

import br.usp.ime.simulation.datatypes.task.CoordinationMessage;
import br.usp.ime.simulation.datatypes.task.ResponseTask;
import br.usp.ime.simulation.datatypes.task.WsRequest;
import br.usp.ime.simulation.experiments.control.ControlVariables;

public abstract class ServiceInvoker extends Process {

	public ServiceInvoker(Host host, String name, String[]args) {
		
		super(host,name,args);
	} 
	
	protected void invokeWsMethod(WsRequest request, String sender,
			String destination) throws MsgException {

		request.destination = destination;
		request.senderMailbox = sender;

		try {
			sendTask(request, sender, destination, WsRequest.toString(request));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	protected void sendCoordinationMessage(CoordinationMessage message,
			String sender, String destination) throws TransferFailureException, HostFailureException, MsgException {

		try {
			sendTask(message, sender, destination, CoordinationMessage.toString(message));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void sendTask(Task request, String sender, String destination,
			String serializedObject) throws TransferFailureException, HostFailureException, MsgException {

		String serialization;

		serialization = serializedObject;
		String[] args = new String[2];
		args[0] = serialization;
		args[1] = destination;
		if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS
				|| ControlVariables.PRINT_TASK_TRANSMISSION)
			Msg.info("Sending a WsRequestSender ");
	
		WsRequestSender rqs= new WsRequestSender(args, getHost());
		rqs.start();
		
	}

	public abstract void notifyCompletion(WsRequest request,
			ResponseTask response) throws MsgException;

	public static Task getResponse(String sender) {
		Task response = null;

		response = tryUntilAMessageIsGot(sender, response);

		if (response instanceof ResponseTask) {
			if (ControlVariables.DEBUG || ControlVariables.PRINT_ALERTS
					|| ControlVariables.PRINT_TASK_TRANSMISSION)
				Msg.info("Task " + ((ResponseTask) response).serviceMethod
						+ " for composition "
						+ ((ResponseTask) response).instanceId
						+ " was succesfully executed by "
						+ ((ResponseTask) response).requestServed.destination);
		} else {
			Msg.info("Something went wrong...");
			System.exit(1);
		}
		return response;
	}

	private static Task tryUntilAMessageIsGot(String sender, Task response) {
		try {
			if (ControlVariables.DEBUG || ControlVariables.PRINT_MAILBOXES)
				Msg.info(" Trying to get response at mailbox: " + sender);
			response = Task.receive(sender);

		} catch (MsgException e) {
			if (ControlVariables.DEBUG || ControlVariables.PRINT_MAILBOXES)
				Msg.info(" Could not get message! ");
			response = tryUntilAMessageIsGot(sender, response);
		}
		return response;
	}
}
