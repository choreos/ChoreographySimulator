java -cp ./target/classes/:lib/jdom-2.0.3.jar:lib/commons-math3-3.0.jar:$SIMGRID_JAVA_ROOT/java/simgrid.jar br.usp.ime.simulation.Simulation choreography_platform.xml  choreography_deployment_prob_monitoring.xml choreography_specification.xml $1 $2 $3