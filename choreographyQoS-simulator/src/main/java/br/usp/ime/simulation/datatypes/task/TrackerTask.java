package br.usp.ime.simulation.datatypes.task;
import java.util.ArrayList;

import org.simgrid.msg.Task;

import br.usp.ime.simulation.common.Common;


public class TrackerTask extends Task {
	/**
	 * Type of the tasks
	 */
	public enum Type {
		REQUEST,
		ANSWER
	};
	public Type type;
	public String hostname;
	public String mailbox;
	public int serviceId;
	public int tracked;
	public double interval;
	public ArrayList<Integer> services;
	
	public TrackerTask(String hostname, String mailbox, int serviceId) {
		this(hostname, mailbox, serviceId, 0, Common.FILE_SIZE);
	}	
	public TrackerTask(String hostname, String mailbox, int serviceId, int tracked, int left) {
		super("", 0, Common.TRACKER_COMM_SIZE);
		this.type = Type.REQUEST;
		this.hostname = hostname;
		this.mailbox = mailbox;
		this.serviceId = serviceId;
		this.tracked = tracked;
		this.services = new ArrayList<Integer>();
	}
	
}
