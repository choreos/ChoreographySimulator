package br.usp.ime.simulation.qos_model;

public class QoSParameter {

	public enum QoSType{
		ATOMIC, COMPOSED 
	};
	
	public enum QoSScope{
		SERVICE, COMMUNICATION, MESSAGE, COMPOSITION 
	};
	
	public enum QoSCategory{
		PERFORMANCE, SECURITY, COST, DEPENDABILITY  
	};
	
	/*public enum QoSide{
		CONSUMER, SUPPLIER  
	};*/
	
	public enum QoSAttribute{
		
		RESPONSE_TIME,
		EXECUTION_TIME,
		COMMUNICATION_TIME, 
		NETWORK_LATENCY, 
		NETWORK_BANDWIDTH, 
		NETWORK_TRANSFER_TIME

		//protected Map<QoSAttribute, QoSParam> components=new HashMap<QoSAttribute, QoSParam>();
		//protected abstract void setDependencies();
		//public Map<QoSAttribute, QoSParam> getComponents(){	return components;}
	}

	
	public enum QoSMetric{
		SECONDS, BITS_PER_SECOND, NRO_PER_SECONDS, BYTES, BITS,  KBYTES, MBYTES, MILLISECONDS 
	};
	

	//private QoSMetric metric;
	private String name;
	private QoSAttribute qosAttribute;
	//private QoSType type;
	private Double value;
	private QoSType qosType= QoSType.ATOMIC;

	public QoSParameter(QoSAttribute attr){
		this(attr, QoSType.ATOMIC.toString(), QoSType.ATOMIC);
	}
	
	public QoSParameter(QoSAttribute attr, String name){
		this(attr, name, QoSType.ATOMIC);
	}
	public QoSParameter(QoSAttribute attr, QoSType type){
		this(attr, type.toString(), type);
	}
	
	public QoSParameter(QoSAttribute attr, String name, QoSType type){
		this.qosAttribute=attr;
		this.qosType= type;
		this.name=name;
	}
	
//	public QoSParameter(QoSAttribute attr, Double value){
//		this.qosAttribute=attr;
//		this.value= value;
//	}
	
	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;;
	}

	
	public QoSAttribute getQoSAttribute(){
		return this.qosAttribute;
	}

	public QoSType getQosType() {
		return qosType;
	}

	public void setQosType(QoSType qosType) {
		this.qosType = qosType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
