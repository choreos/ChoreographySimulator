package br.usp.ime.simulation.monitoring;

import java.util.HashMap;
import java.util.Map;

import br.usp.ime.simulation.choreography.ChoreographyInstance;


public class ChoreographyMonitor {//extends Process{

	private static Map<Long, ChoreographyInstance> choreographyInstances = new HashMap<Long, ChoreographyInstance>();
	private static Long nextChoreographyId=0L;
	
	//for experiments
	private static int numberRequests=1;
	private static int numberEnacts=1;//number of simulations
	private static Map<String, Double> responseSizes = new HashMap<String, Double>();
	private static String datasetFileName="data_default.txt";
	
	//for Monitoring
	private static int pivot=0;
	
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
		if(responseSize!=null && responseSize>0)
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

	public static void setNumberOfEnacts(Integer nro_enacts) {
		numberEnacts=nro_enacts;
	}
	
	public static Integer getNumberOfEnacts() {
		return numberEnacts;
	}

	public static int getCurrentPivot() {
		return pivot;
	}

	public static int nextPivot() {
		return ++pivot;
	}

	public static void setPivot(int pivot) {
		ChoreographyMonitor.pivot = pivot;
	}
}
