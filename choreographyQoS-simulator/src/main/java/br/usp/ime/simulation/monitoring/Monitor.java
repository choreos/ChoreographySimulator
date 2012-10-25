package br.usp.ime.simulation.monitoring;

import org.simgrid.msg.Host;
import org.simgrid.msg.MsgException;
import org.simgrid.msg.Process;

public class Monitor extends Process{

	public Monitor(Host host, String name, String[]args) {
		super(host,name,args);
	} 
	@Override
	public void main(String[] arg0) throws MsgException {
		// TODO Auto-generated method stub
		
	}
	
}
