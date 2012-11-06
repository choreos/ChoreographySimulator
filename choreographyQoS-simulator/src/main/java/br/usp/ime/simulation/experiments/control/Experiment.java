package br.usp.ime.simulation.experiments.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Properties;

import org.simgrid.msg.MsgException;
import org.simgrid.msg.NativeException;

public class Experiment {
	
	private Properties experimentsProperties = new Properties();

	
	public Experiment(){
		
	}
	
	public static void main(final String[] args) throws MsgException {
		Experiment experiment = new Experiment();
		experiment.loadExperimentsProperties();
		
		experiment.runExperiments();
	}
	
	public void runExperiments() throws MsgException{
		int nro_request_initial = Integer.parseInt(this.experimentsProperties.getProperty("nro_request_initial"));
		int nro_request_increment = Integer.parseInt(this.experimentsProperties.getProperty("nro_request_increment"));
		int nro_request_times= Integer.parseInt(this.experimentsProperties.getProperty("nro_request_times"));
		System.out.println("****************************************************");
		System.out.println("Nro_request_initial = "+ nro_request_initial);
		System.out.println("Nro_request_increment = "+ nro_request_increment);
		System.out.println("Nro_request_times = "+ nro_request_times);
		System.out.println("****************************************************");
		
		int current_nro_requests= nro_request_initial;
		
		for ( int i=0; i< nro_request_times; i++){
			//log.record("NRO_REQUESTS",String.valueOf(current_nro_requests));
			System.out.println("Executing: "+current_nro_requests+" requests");
			try {
				String run= "java -cp ./target/classes/:lib/jdom-2.0.3.jar:$SIMGRID_JAVA_ROOT/java/simgrid.jar "+
			     " br.usp.ime.simulation.Simulation smallplatform.xml smallChoreographyDeployment2.xml "+ current_nro_requests;
				
				String run2="sh simulate.sh "+ current_nro_requests; 

				//Process process = Runtime.getRuntime().exec("run",null,new File("."));
				Process process = Runtime.getRuntime().exec(run);
				InputStream is = process.getInputStream();
			       InputStreamReader isr = new InputStreamReader(is);
			       BufferedReader br = new BufferedReader(isr);
			       String line;

			       while ((line = br.readLine()) != null) {
			         System.out.println(line);
			       }
				
			} catch (IOException e) {
				System.out.println("Error executing");
				e.printStackTrace();
			} 
			
			current_nro_requests+=nro_request_increment; 
		}
		
		
	}
	
	private void loadExperimentsProperties() {
		//Load experiments properties from a properties file
		try {
			this.experimentsProperties.load(new FileInputStream("experiments.properties"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
