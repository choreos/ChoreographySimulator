package br.usp.ime.simulation.monitoring;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import br.usp.ime.simulation.choreography.ChoreographyInstance;
import br.usp.ime.simulation.log.Log;


public class ChoreographyMonitor {//extends Process{

	private static Map<Long, ChoreographyInstance> choreographyInstances = new HashMap<Long, ChoreographyInstance>();
	private static Long nextChoreographyId=0L;
	
	//for experiments
	private static int numberRequests=1;
	private static int numberEnacts=1;//number of simulations
	private static Map<String, Double> responseSizes = new HashMap<String, Double>();
	private static String datasetFileName="data_default.txt";
	
	//for Monitoring
	private  static int shift; //p
	private static int currentShift=0;
	private static int pivot=0;
	private static int monitoringSampleSize=0;
	private static Queue currentMonitoringSample;
	private static Log monitoringLog; 

	public static void initialize() {
		ViolationDetection.loadContractData("resources/chorsim_RT_contract.txt");
		monitoringLog= new Log();
		monitoringLog.open("monitoring.log");
		monitoringLog.record("pivot", " test  ", " distance ", "    p_plus   ", "    p_minus "	);
		
		currentMonitoringSample = new ArrayDeque<Double>();
		monitoringSampleSize=100;
		shift=1;
		currentShift=0;
		pivot=0;
		
	}
	
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


	/*
	 * Violation Detection
	 */
	public static void testViolation(double[] samples) {
		
		//Boolean test = ViolationDetection.testViolation2(samples);
		//monitoringLog.record(String.valueOf(pivot), test.toString());
		
		DetectionStatistics stat = ViolationDetection.testViolation(samples);
		monitoringLog.record(String.valueOf(pivot), stat.isGuaranteed().toString(), String.valueOf(stat.getDistance()), 
				String.valueOf( stat.getpValue_plus()), String.valueOf(stat.getpValue_minus())  );
		
	}

	/*
	 * recordSample
	 */
	public static void recordSample(Double responseTime) {
		currentMonitoringSample.add(responseTime);
		
		if(pivot== monitoringSampleSize){//tem data suficiente para realizar a
			testViolation( toPrimitiveDoubles(currentMonitoringSample)  );
			currentShift++;
		}
		else if(pivot > monitoringSampleSize){
			if(currentShift==shift){
				for(int i=0;i<shift;i++)
					currentMonitoringSample.remove();
					
				testViolation( toPrimitiveDoubles( currentMonitoringSample) );
				currentShift=0;
			}
			currentShift++;
				
		}
		else{
			//nothing
		}
		
		pivot++;
		
	}

	
	private static double[] toPrimitiveDoubles(Collection<Double> samples){
		double []currentSample=new double[samples.size()];
		
		int i=0;
		for(Double s: samples)
			currentSample[i++]=s;
			
		return currentSample;
	}

	public static void close() {
		monitoringLog.close();
	}
	
}
