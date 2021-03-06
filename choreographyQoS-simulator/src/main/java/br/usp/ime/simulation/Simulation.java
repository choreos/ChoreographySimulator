/*
 * Copyright 2006,2007,2010 The SimGrid Team. All right reserved. 
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the license (GNU LGPL) which comes with this package. 
 */

package br.usp.ime.simulation;
import org.simgrid.msg.Msg;
import org.simgrid.msg.NativeException;

import br.usp.ime.simulation.choreography.model.ChoreographyModel;
import br.usp.ime.simulation.monitoring.ChoreographyMonitor;

public class Simulation {
	
   /* This only contains the launcher. If you do nothing more than than you can run 
    *   java simgrid.msg.Msg
    * which also contains such a launcher
    */
   
    public static void main(final String[] args) throws NativeException {
    String platform = "smallplatform.xml";
    //String deploymentFile = "smallOrchestrationDeployment.xml";
    String deploymentFile = "smallChoreographyDeployment2.xml";
    String specificationFile = "smallChoreographySpacification.xml";
    int nro_request=1;
    Double responseSize=0.0;
    String dataSetFileName="_";
    Integer nro_enacts= 1;
	/* initialize the MSG simulation. Must be done before anything else (even logging). */
	Msg.init(args);

    if(args.length >= 6 && args.length <= 7) {
    	platform = args[0];
    	deploymentFile = args[1];
    	specificationFile = args[2];
    	nro_request=Integer.parseInt(args[3]);
    	responseSize=Double.parseDouble(args[4]);
    	dataSetFileName=args[5].trim();
    	
    	if(args.length == 7) 
    		nro_enacts = Integer.parseInt(args[6]);
    	else 
    		nro_enacts=1;
    }
    else{
    	Msg.info("Usage   : Simulation platform_file deployment_file");
        Msg.info("example : Simulation comm_time_platform.xml comm_time_deployment.xml");
    }
    
  //Generating the Choreography model from a specification xml 
  	//ChoreographyModel.generateChoreographyMode("smallChoreographySpecification.xml");
  	ChoreographyModel.generateChoreographyModel(specificationFile);
  	ChoreographyMonitor.setNumberRequests(nro_request);
  	
  	if(responseSize>0)
  		ChoreographyMonitor.setResponseSizeOf("WS1_method1", responseSize) ;
  	
  	ChoreographyMonitor.setDatasetFileName(dataSetFileName);
  	ChoreographyMonitor.setNumberOfEnacts(nro_enacts);
  	
  	ChoreographyMonitor.initialize();
  	
	/* construct the platform and deploy the application */
	Msg.createEnvironment(platform);
	Msg.deployApplication(deploymentFile);
	
		
	/*  execute the simulation. */
	Msg.run();
    }
}
