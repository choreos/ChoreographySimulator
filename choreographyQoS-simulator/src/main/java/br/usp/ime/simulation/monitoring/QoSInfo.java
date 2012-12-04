package br.usp.ime.simulation.monitoring;

import java.util.HashMap;
import java.util.Map;

import br.usp.ime.simulation.datatypes.task.ResponseTask;
import br.usp.ime.simulation.datatypes.task.WsRequest;
import br.usp.ime.simulation.qos_model.QoSParameter;
import br.usp.ime.simulation.qos_model.QoSParameter.QoSAttribute;
import br.usp.ime.simulation.qos_model.QoSParameter.QoSType;

public class QoSInfo {

	private WsRequest request;
	private ResponseTask response;
	
	private boolean isComposed=false;
	
	Map<QoSAttribute, QoSParameter> qosParamsInRequest;
	Map<QoSAttribute, QoSParameter> qosParamsInResponse;
	
	public QoSInfo(){
		//this.isComposed= false;
		this.qosParamsInRequest = new HashMap<QoSParameter.QoSAttribute, QoSParameter>();
		this.qosParamsInRequest.put(QoSAttribute.EXECUTION_TIME, null);
		this.qosParamsInRequest.put(QoSAttribute.RESPONSE_TIME, null);
		this.qosParamsInRequest.put(QoSAttribute.COMMUNICATION_TIME, null);
		//this.qosParameters.put(QoSAttribute.EXECUTION_TIME, null);
		

		this.qosParamsInResponse = new HashMap<QoSParameter.QoSAttribute, QoSParameter>();
		this.qosParamsInRequest.put(QoSAttribute.EXECUTION_TIME, null);
		this.qosParamsInResponse.put(QoSAttribute.RESPONSE_TIME, null);
		this.qosParamsInResponse.put(QoSAttribute.COMMUNICATION_TIME, null);
	}
	
	
	
	public QoSParameter getQoSParamInResponseOf(QoSAttribute attr){
		return this.qosParamsInResponse.get(attr);
	}
	
	public void setQoSParamInResponseOf(QoSAttribute attr, QoSParameter param){
		if (this.isComposed)
			 param.setQosType(QoSType.COMPOSED);
		
		this.qosParamsInResponse.put(attr, param);
	}
	
	
	public QoSParameter getQoSParamInRequestOf(QoSAttribute attr){
		return this.qosParamsInRequest.get(attr);
	}
	
	
	public void setQoSParamInRequestOf(QoSAttribute attr, QoSParameter param){
		if (this.isComposed)
			 param.setQosType(QoSType.COMPOSED);
		
		this.qosParamsInRequest.put(attr, param );
	}
	
	
	public void setQoSParamInResponseValueOf(QoSAttribute attr, Double qosValue, QoSType type){
		QoSParameter param = new QoSParameter(attr, type);
		this.qosParamsInResponse.put(attr, param);
	}

	
	public void setQoSParamInRequestValueOf(QoSAttribute attr, Double qosValue, QoSType type){
		QoSParameter param = new QoSParameter(attr, type);
		this.qosParamsInRequest.put(attr, param);
	}
	
	
	public WsRequest getRequest() {
		return request;
	}

	
	public void setRequest(WsRequest request) {
		this.request = request;
	}

	
	public ResponseTask getResponse() {
		return response;
	}

	
	public void setResponse(ResponseTask response) {
		this.response = response;
	}

	
	public boolean isComposed() {
		return isComposed;
	}


	public void setComposed(boolean isComposed) {
		this.isComposed = isComposed;
	}

}
