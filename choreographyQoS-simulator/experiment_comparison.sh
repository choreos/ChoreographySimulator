#!/bin/bash
nro_request_initial=2
nro_request_increment=1
nro_request_times=2
nro_request_limit=49

#for i    in  {$nro_request_initial..$nro_request_times..$nro_request_increment}
#for i    in  {1..10}  
sh simulate_comparison.sh  1  8388608
i=$nro_request_initial
while [ $i -le $nro_request_limit ]
do
  	#sh simulate_comparison.sh  $i 8388608
	sh simulate_comparison.sh  10 8388608
#java -cp ./target/classes/:lib/jdom-2.0.3.jar:/opt/Apps/dev/simgrid-java/java/simgrid.jar br.usp.ime.simulation.Simulation smallplatform.xml  smallChoreographyDeployment2.xml i
	 i=$(( $i + $nro_request_increment ))
done

