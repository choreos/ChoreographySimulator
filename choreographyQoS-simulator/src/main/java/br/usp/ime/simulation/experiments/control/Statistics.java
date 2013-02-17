package br.usp.ime.simulation.experiments.control;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import br.usp.ime.simulation.choreography.Choreographer;
import br.usp.ime.simulation.log.Log;

public class Statistics {
	//plotting
	private static Log dataSet = new Log();
	private static String datasetFile="sim_chor_data.txt";
	public static DescriptiveStatistics statsResponseTime = new DescriptiveStatistics();
	
	
	public static void openDataset(String fileName){
		datasetFile =fileName ;
		dataSet.open(datasetFile, true);
	}
	
	public static void openDataset(){
		dataSet.open(datasetFile, true);
	}


	public static void recordDescriptiveStatistics(Integer nro_requests) {
		//writing into dataSet 
		Double variance = statsResponseTime.getVariance();
		dataSet.record(nro_requests.toString(), String.valueOf(statsResponseTime.getMean()), 
				String.valueOf(statsResponseTime.getMax()), String.valueOf(statsResponseTime.getMin()), variance.toString() );
	}


	public static void recordDescriptiveStatistics( String line ) {
		//writing into dataSet 
		dataSet.record(line );
	}
	
	public static void record( String... extraCols ) {
		dataSet.record(extraCols);
	}

	public static void recordDescriptiveStatistics( String... extraCols ) {
		dataSet.record(extraCols);
	}

	public static void closeDataset() {
		dataSet.close();
		
	}
	
	
	
}
