package br.usp.ime.simulation.log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class Log {
	public BufferedWriter log;
	public static final String LOGNAME = "sim.log";

	public void open(){
		log = openLog("sim.log", false);
	}
	
	public void open(String name, boolean append){
		log = openLog(name, append);
	}

	public void open(String name){
		log = openLog(name, false);
	}
	
	private BufferedWriter openLog(final String filename, boolean append) {
        FileWriter fstream = null;
        try {
            fstream = new FileWriter(filename, append);
            
        } catch (IOException e) {
            System.err.println("Error while opening " + filename);
            e.printStackTrace();
        }

        return new BufferedWriter(fstream);
    }

    
    public void recordToDataset( String... extraCols) {
        String line = "";
        for (String column : extraCols) {
            line = line + " " + column;
        }

        writeln(line);
    }

    public void record( final double start,  double end, String... extraCols) {
    	double tr= (end - start);
        String line = start +" "+end + " " + tr ;

        for (String column : extraCols) {
            line = line + " " + column;
        }

        writeln(line);
    }
    
    public void record( String... extraCols) {
        String line = "";

        for (String column : extraCols) {
            line = line + " " + column;
        }

        writeln(line);
    }

    private void writeln(String line) {
        try {
            synchronized (Log.class) {
                log.write(line + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error while writing to file");
            e.printStackTrace();
        }
    }
    
    public void close(){
    	try {
			log.close();
			
		} catch (IOException e) {
			System.err.println("Error while close file");
			e.printStackTrace();
		}
    }
    
    
}
