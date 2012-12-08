package br.usp.ime.simulation.monitoring;

import java.util.HashMap;
import java.util.Map;

import br.usp.ime.simulation.choreography.ChoreographyInstance;


public class ChoreographyMonitor {//extends Process{

	private static Map<Long, ChoreographyInstance> choreographyInstances = new HashMap<Long, ChoreographyInstance>();
	private static Long nextChoreographyId=0L;
	
	//for experiments
	private static int numberRequests=1;
	private static Map<String, Double> responseSizes = new HashMap<String, Double>();
	private static String datasetFileName="data_default.txt";
	
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

	public static int getNumberRequests() {
		return numberRequests;
	}

	public static void setNumberRequests(int numberRequest) {
		ChoreographyMonitor.numberRequests = numberRequest;
	}

	/**
	 * 
	 * @param key:  servicename_operationname
	 * @param responseSize : in Bytes
	 */
	public static void setResponseSizeOf(String key, Double responseSize) {
		responseSizes.put(key, responseSize);
	}
	
	public static Double getResponseSizeOf(String key){
		return responseSizes.get(key);
	}

	
	public static String getDatasetFileName() {
		return datasetFileName;
	}
	
	public static void setDatasetFileName(String filename) {
		datasetFileName = filename;
	}
}
