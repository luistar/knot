package org.unina.spatialanalysis.RouteStepAnalyzer.entity;

import org.unina.spatialanalysis.RouteStepAnalyzer.entity.routesteps.Segment;

public class SimpleDataEntry {
	private long originId;
	private long destinationId;
	private String theGeom;
	private Integer totalVisit;
	private String tags;
	private double avgTime;
	private double medianTime;
	
	/**
	 * @return the tags
	 */
	public String getTags() {
		return tags;
	}

	/**
	 * @return the theGeom
	 */
	public String getTheGeom() {
		return theGeom;
	}
	/**
	 * @param theGeom the theGeom to set
	 */
	public void setTheGeom(String theGeom) {
		this.theGeom = theGeom;
	}
	/**
	 * @return the totalVisit
	 */
	public int getTotalVisit() {
		return totalVisit;
	}

	public SimpleDataEntry(Segment s) {
		super();
		this.originId = s.getOriginId();
		this.destinationId = s.getDestinationId();
		this.theGeom = s.getTheGeom();
		this.tags = s.getTags();
		this.totalVisit = s.getNumberOfHits();
		if(totalVisit-1>0) {
			this.avgTime = s.getTotalTimeBetweenHits()/(totalVisit-1);
		}else {
			this.avgTime = 0;
		}
		this.medianTime = s.getMedianTime();
	}
	
	public double getMedianTime() {
		return medianTime;
	}
	
	public double getAvgTime() {
		return avgTime;
	}
	public void setAvgTime(double avgTime) {
		this.avgTime = avgTime;
	}
	public long getDestinationId() {
		return destinationId;
	}
	public long getOriginId() {
		return originId;
	}
	
	public String formatAverage() {
		String res = String.format(  "%d:%02d:%02d.%04d",
		        (int)(avgTime / 3600),
		        (int)((avgTime % 3600) / 60),
		        (int)(avgTime % 60),(int) ((avgTime % 1)*10000));
		return res;
	}
	
}
