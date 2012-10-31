package br.usp.ime.simulation.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceRegistry {
	
	private static ServiceRegistry instanceRegistry = null;
	
	public static ServiceRegistry getInstance() {
	      if(instanceRegistry == null) {
	         instanceRegistry  = new ServiceRegistry();
	      }
	      return instanceRegistry;
	   }
	
	public List<String> serviceMailboxes = new ArrayList<String>();
	public Map<String, String> serviceMailboxesMap = new HashMap<String, String>();//<serviceName,Mailboxes>
	
	public void reset(){
		serviceMailboxes = new ArrayList<String>();
	}
	
	public void putServiceMailbox(String smb){
		serviceMailboxes.add(smb);
	}
	
	public void putServiceAndServiceMailbox(String serviceName, String smb){
		this.serviceMailboxesMap.put(serviceName, smb);
	}
	
	public String findServiceMailBoxByServiceName(String serviceName){
		return this.serviceMailboxesMap.get(serviceName);
	}

	public List<String> getServiceMailboxes() {
		return serviceMailboxes;
	}

	
	
}
