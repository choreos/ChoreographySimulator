package br.usp.ime.simulation.monitoring;

import java.util.HashMap;
import java.util.Map;

import br.usp.ime.simulation.choreography.ChoreographyInstance;


public class ChoreographyMonitor {//extends Process{

	private static Map<Long, ChoreographyInstance> choreographyInstances = new HashMap<Long, ChoreographyInstance>();
	private static Long nextChoreographyId=0L;
	
	public  static void addChoreographyInstance(ChoreographyInstance instance){
		choreographyInstances.put(instance.getCompositionId(), instance);
	}
	
	public static  ChoreographyInstance nextChoreographyInstance(){
		ChoreographyInstance instance = new ChoreographyInstance();
		instance.setCompositionId(nextChoreographyId());
		choreographyInstances.put(instance.getCompositionId(), instance);
		return instance;
	}
	
	public static ChoreographyInstance findChoreographyInstance(Long id){
		return choreographyInstances.get(id);
	}
	
	private static Long nextChoreographyId(){
		return nextChoreographyId++;
	}
}
