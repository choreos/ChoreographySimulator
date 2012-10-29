package br.usp.ime.simulation.monitoring;

import org.simgrid.msg.Host;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.Process;

public class ChoreographyMonitor extends Process{

	public ChoreographyMonitor(Host host, String name, String[]args) {
		super(host,name,args);
	} 
	@Override
	public void main(String[] arg0) throws MsgException {
		// TODO Auto-generated method stub
		
	}
	
}
