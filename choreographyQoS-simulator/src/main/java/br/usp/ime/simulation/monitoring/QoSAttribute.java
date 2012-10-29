package br.usp.ime.simulation.monitoring;

public class QoSAttribute {

	public enum QoSType{
		SERVICE, COMMUNICATION, MESSAGE, COMPOSITION 
	};
	
	public enum QoSMetric{
		RESPONSE_TIME, EXECUTION_TIME, NET_LATENCY, NET_BANDWIDTH, NET_TRANSFER_TIME 
	};
	
	private QoSMetric metric;
	private QoSType type;
	private String value;

	public QoSAttribute(QoSType type, QoSMetric metric, String value){
		this.metric = metric;
		this.type = type;
		this.setValue(value);
	}
	
	public QoSAttribute(QoSType type, QoSMetric metric){
		this.metric = metric;
		this.type = type;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
