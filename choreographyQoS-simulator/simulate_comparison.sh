java -cp ./target/classes/:lib/jdom-2.0.3.jar:lib/commons-math3-3.0.jar:$SIMGRID_JAVA_ROOT/java/simgrid.jar  -Djava.library.path=$SIMGRID_JAVA_ROOT/java  br.usp.ime.simulation.Simulation choreography_platformV2.xml  choreography_deploymentV2.xml choreography_specificationV2.xml  $1 $2 chorsim_comparison_chor_TR_byRequests.txt

java -cp ./target/classes/:lib/jdom-2.0.3.jar:lib/commons-math3-3.0.jar:$SIMGRID_JAVA_ROOT/java/simgrid.jar  -Djava.library.path=$SIMGRID_JAVA_ROOT/java  br.usp.ime.simulation.Simulation orchestration_platformV2.xml  orchestration_deploymentV2.xml orchestration_specificationV2.xml $1 $2  chorsim_comparison_orch_TR_byRequests.txt


