#!/bin/bash
nro_request_initial=2
nro_request_increment=1
nro_request_times=9
nro_request_limit=49

#for i    in  {$nro_request_initial..$nro_request_times..$nro_request_increment}
#for i    in  {1..10}  
sh simulate.sh  1
i=$nro_request_initial
while [ $i -le $nro_request_limit ]
do
  	sh simulate_orch.sh  $i
#java -cp ./target/classes/:lib/jdom-2.0.3.jar:/opt/Apps/dev/simgrid-java/java/simgrid.jar br.usp.ime.simulation.Simulation smallplatform.xml  smallChoreographyDeployment2.xml i
	 i=$(( $i + $nro_request_increment ))
done

