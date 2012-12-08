#!/bin/bash
#nro_request_initial=2
#nro_request_increment=1
#nro_request_times=9
#nro_request_limit=49
nro_request_current=5

# Bandwidth variables
BW_initial=1 #1Mbps
BW_increment=1000000  #+ 1Mbps
BW_counter=1
BW_counter_limit=3


# Response Size of WS1 method1 variables
response_size_counter=1
response_size_counter_limit=50
response_size_initial=0 #8388608 #  1MB
response_size_increment=  8388608 # + 1 MB


#sh simulate2.sh  1 
#i=$nro_request_initial
response_size_current=$response_size_initial #$response_size_initial
BW_current=$BW_initial #$BW_initial

FileName="default.txt"
platformFile="choreography_platform.xml"
while [ $BW_counter -le $BW_counter_limit ]
do
	echo ">> Current BW: "$BW_counter" Mbps"

  	#sh simulate.sh  $i
	response_size_counter=1
	platformFile="choreography_platform"$BW_counter".xml"
	echo "platform: "$platformFile
	FileName="chor_data_"$BW_counter"Mbps.dat"
	#echo "\t   Writing into "$FileName
	while [ $response_size_counter -le $response_size_counter_limit ]
	do
		#FileName="data_"$BW_counter"Mbps_"$response_size_counter"MB.txt"
		echo "\t   "$response_size_counter"MB  into "$FileName
		
		response_size_current= $(($response_size_current + $response_size_increment)) #$(($response_size_counter * $response_size_increment))
		sh simulate2.sh  $nro_request_current $response_size_current $FileName
		
		response_size_counter=$(($response_size_counter + 1))
	done
	 
	 BW_current=$(($BW_counter * $BW_increment))
	 BW_counter=$(($BW_counter + 1))	
	 #$(($BW_counter + 1))	
done
#java -cp ./target/classes/:lib/jdom-2.0.3.jar:/opt/Apps/dev/simgrid-java/java/simgrid.jar br.usp.ime.simulation.Simulation smallplatform.xml  smallChoreographyDeployment2.xml i
