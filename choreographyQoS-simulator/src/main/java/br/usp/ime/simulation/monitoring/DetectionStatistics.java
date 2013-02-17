package br.usp.ime.simulation.monitoring;

public class DetectionStatistics {
	private double pValue_plus;
	private double pValue_minus;
	
	private double distance_plus;
	private double distance_minus;
	
	private double distance;
	
	private Boolean guaranteed; 

	public double getpValue_plus() {
		return pValue_plus;
	}

	
	public void setpValue_plus(double pValue_plus) {
		this.pValue_plus = pValue_plus;
	}

	public double getpValue_minus() {
		return pValue_minus;
	}

	public void setpValue_minus(double pValue_minus) {
		this.pValue_minus = pValue_minus;
	}

	public double getDistance_plus() {
		return distance_plus;
	}

	public void setDistance_plus(double distance_plus) {
		this.distance_plus = distance_plus;
	}

	public double getDistance_minus() {
		return distance_minus;
	}

	public void setDistance_minus(double distance_minus) {
		this.distance_minus = distance_minus;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}


	public Boolean isGuaranteed() {
		return guaranteed;
	}


	public void setGuaranteed(Boolean guaranteed) {
		this.guaranteed = guaranteed;
	}

	
	
	
}
