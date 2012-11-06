package br.usp.ime.simulation.experiments.control;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import br.usp.ime.simulation.choreography.Choreographer;
import br.usp.ime.simulation.log.Log;

public class Statistics {
	//plotting
	private static Log dataSet = new Log();
	private static String datasetFile="sim_chor_data.txt";
	public static DescriptiveStatistics statsResponseTime = new DescriptiveStatistics();
	
	
	public static void openDataset(){
		dataSet.open(datasetFile, true);
	}


	public static void recordDescriptiveStatistics(Integer nro_requests) {
		// TODO Auto-generated method stub
		//writing into dataSet 
		Double variance = statsResponseTime.getVariance();
		dataSet.record(nro_requests.toString(), String.valueOf(statsResponseTime.getMean()), 
				String.valueOf(statsResponseTime.getMax()), String.valueOf(statsResponseTime.getMin()), variance.toString() );
		
	}


	public static void closeDataset() {
		dataSet.close();
		
	}
	
	
	
}
