package br.usp.ime.simulation.monitoring;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//import com.numericalmethod.suanshu.misc.ArrayUtils;
import com.numericalmethod.suanshu.stats.test.distribution.kolmogorov.KolmogorovSmirnov;
import com.numericalmethod.suanshu.stats.test.distribution.kolmogorov.KolmogorovSmirnov2Samples;
//import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;


public class ViolationDetection {
	private  static double [] contractData;
	private static int sampleMonitoringSize;// delta
	
	
	//public void test(double [] sampleMonitoring){
	public static void kolmogorovSmirnov2Samples( double[] sample1, double[] sample2, KolmogorovSmirnov.Side side){
		
		KolmogorovSmirnov2Samples instance = new KolmogorovSmirnov2Samples(sample1, sample2, side);
		   System.out.printf("p-value = %f; test stats = %f; null: %s%n",
				   instance.pValue(), instance.statistics(), instance.getNullHypothesis());
		
	}
	
	/*
	 * 
	 */
	public static DetectionStatistics testViolation(double[] currentSample){
		KolmogorovSmirnov2Samples instance1 = new KolmogorovSmirnov2Samples(contractData, currentSample, KolmogorovSmirnov.Side.GREATER);
		KolmogorovSmirnov2Samples instance2 = new KolmogorovSmirnov2Samples(contractData, currentSample, KolmogorovSmirnov.Side.LESS);
		
		
		DetectionStatistics stat= new DetectionStatistics();
		double distance = Math.max( Math.abs(instance1.statistics()), Math.abs(instance2.statistics()) );
		stat.setDistance(distance);
		stat.setDistance_plus(instance1.statistics());
		stat.setDistance_minus(instance2.statistics());
		stat.setpValue_plus(instance1.pValue());
		stat.setpValue_minus(instance2.pValue());
		
		//Rule detection
		boolean isGuaranteed= instance1.pValue()>=0.05 && //p+>0.05
			     distance <0.15; //D=max{D+,D+} <0.15
		
	    stat.setGuaranteed(isGuaranteed);
	    
	    return stat;
	}
	
	public static boolean testViolation2(double[] currentSample){
		return testViolation(currentSample).isGuaranteed();
	}


	public static void main(final String[] args){
		
		//double[] contractData;
		ViolationDetection.loadContractData("resources/chorsim_RT_contract.txt");
		System.out.println("contract:\n"+ViolationDetection.getContractData().length);
		double []sample = ViolationDetection.getSamplesFromFile("chorsim_RT_monitoring-15_17_14.txt");
		//System.out.println("sample:\n"+sample.length);
		//ViolationDetection.kolmogorovSmirnov2Samples(ViolationDetection.getContractData(), sample, KolmogorovSmirnov.Side.GREATER);
		//ViolationDetection.kolmogorovSmirnov2Samples(ViolationDetection.getContractData(), sample, KolmogorovSmirnov.Side.LESS);

		
		System.out.println("test1");
		//ViolationDetection.kolmogorovSmirnov2Samples(ViolationDetection.getContractData(), sample, KolmogorovSmirnov.Side.GREATER);
		//ViolationDetection.kolmogorovSmirnov2Samples(ViolationDetection.getContractData(), sample, KolmogorovSmirnov.Side.LESS);
		System.out.println("violation detection: "+testViolation(sample));
		
		System.out.println("test2");
		double []sample2 = ViolationDetection.getSamplesFromFile("chorsim_RT_monitoring-14_13_14.txt");
		//ViolationDetection.kolmogorovSmirnov2Samples(ViolationDetection.getContractData(), sample2, KolmogorovSmirnov.Side.GREATER);
		//ViolationDetection.kolmogorovSmirnov2Samples(ViolationDetection.getContractData(), sample2, KolmogorovSmirnov.Side.LESS);
		System.out.println("violation detection: "+testViolation(sample2));
		
		System.out.println("test3");
		double []sample3 = ViolationDetection.getSamplesFromFile("chorsim_RT_monitoring-12_13_11.txt");
		//ViolationDetection.kolmogorovSmirnov2Samples(ViolationDetection.getContractData(), sample3, KolmogorovSmirnov.Side.GREATER);
		//ViolationDetection.kolmogorovSmirnov2Samples(ViolationDetection.getContractData(), sample3, KolmogorovSmirnov.Side.LESS);
		System.out.println("violation detection: "+testViolation(sample3));
		
		System.out.println("test4");
		double []sample4 = ViolationDetection.getSamplesFromFile("chorsim_RT_monitoring-11_10_11.txt");
		//ViolationDetection.kolmogorovSmirnov2Samples(ViolationDetection.getContractData(), sample4, KolmogorovSmirnov.Side.GREATER);
		//ViolationDetection.kolmogorovSmirnov2Samples(ViolationDetection.getContractData(), sample4, KolmogorovSmirnov.Side.LESS);
		System.out.println("violation detection: "+testViolation(sample4));
		
		System.out.println("test5");
		double []sample5 = ViolationDetection.getSamplesFromFile("chorsim_RT_monitoring-10_10_11.txt");
		//ViolationDetection.kolmogorovSmirnov2Samples(ViolationDetection.getContractData(), sample5, KolmogorovSmirnov.Side.GREATER);
		//ViolationDetection.kolmogorovSmirnov2Samples(ViolationDetection.getContractData(), sample5, KolmogorovSmirnov.Side.LESS);
		System.out.println("violation detection: "+testViolation(sample5));
		
	}
	

	
	public static double [] getContractData() {
		return contractData;
	}

	
	public static void setContractData(double[] contract) {
		contractData = contract.clone();
	}
	
	public static void loadContractData(String fileName) {
		contractData = getSamplesFromFile(fileName);
	}
	
	public static double[] getSamplesFromFile(String fileName) {
		 
		List<Double> contract=new ArrayList<Double>();
	    //String NL = System.getProperty("line.separator");
		Scanner scanner=null;
	    try {
		    scanner = new Scanner(new FileInputStream(fileName));
	    	if(scanner.hasNextLine())
	    		scanner.nextLine();//header
	    	
	    	Double rt;
	    	String strRT;
	    	
	    	while (scanner.hasNextLine() && scanner.hasNext()){
	    		//String entry = scanner.nextLine();
	    		scanner.nextInt();//pivot
	    		//System.out.println(scanner.nextInt());//pivot
	    		strRT= scanner.next();
	    		
	    		//rt=(scanner.nextBigDecimal()).doubleValue();
	    		rt=Double.valueOf(strRT);
	    		//System.out.println("rt:"+strRT);
	    		contract.add(rt);
	        }
	    } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    finally{
	      scanner.close();
	    }
	    
	    double[] contractArray= new double[contract.size()];
	    int i=0;
	    for(Double x: contract)
	    	contractArray[i++]=x;
	    
	    return contractArray;
	    
	}
	

}
