package br.usp.ime.simulation.shared;

import java.util.ArrayList;
import java.util.List;

public class ServiceRegistry {
	
	private static ServiceRegistry instanceRegistry = null;
	
	public static ServiceRegistry getInstance() {
	      if(instanceRegistry == null) {
	         instanceRegistry  = new ServiceRegistry();
	      }
	      return instanceRegistry;
	   }
	
	public List<String> serviceMailboxes = new ArrayList<String>();
	
	public void reset(){
		serviceMailboxes = new ArrayList<String>();
	}
	
	public void putServiceMailbox(String smb){
		serviceMailboxes.add(smb);
	}

	public List<String> getServiceMailboxes() {
		return serviceMailboxes;
	}

	
	
}
