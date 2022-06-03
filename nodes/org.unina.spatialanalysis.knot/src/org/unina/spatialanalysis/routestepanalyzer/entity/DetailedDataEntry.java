package org.unina.spatialanalysis.routestepanalyzer.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.unina.spatialanalysis.routestepanalyzer.entity.routesteps.HitHolder;
import org.unina.spatialanalysis.routestepanalyzer.entity.routesteps.Segment;
import org.unina.spatialanalysis.routestepanalyzer.entity.visit.TimeSlot;

public class DetailedDataEntry {
	
	private long originId;
	
	private long destinationId;
	
	private LocalDateTime day;
	
	private Integer nVisitsInDay;
	
	private Double avgForDay;
	
	private double medianForDay;
	
	private Integer nVisitsEM;
	
	private Double avgForEM;
	
	private double medianForEM;
	
	private Integer nVisitsMM;
	
	private Double avgForMM;
	
	private double medianForMM;
	
	private Integer nVisistsA;
	
	private Double avgForA;
	
	private double medianForA;
	
	private Integer nVisistsE;
	
	private Double avgForE;
	
	private Double medianForE;
	
	private String tags;
	
	private String theGeom;

	
	/**
	 * @return the day
	 */
	public LocalDateTime getDay() {
		return day;
	}

	/**
	 * @param day the day to set
	 */
	public void setDay(LocalDate day) {
		this.day = day.atStartOfDay();
	}

	/**
	 * @return the nVisitsInDay
	 */
	public Integer getnVisitsInDay() {
		return nVisitsInDay;
	}

	/**
	 * @param nVisitsInDay the nVisitsInDay to set
	 */
	public void setnVisitsInDay(Integer nVisitsInDay) {
		this.nVisitsInDay = nVisitsInDay;
	}

	/**
	 * @return the avgForDay
	 */
	public Double getAvgForDay() {
		return avgForDay;
	}

	/**
	 * @param avgForDay the avgForDay to set
	 */
	public void setAvgForDay(Double avgForDay) {
		this.avgForDay = avgForDay;
	}

	/**
	 * @return the nVisitsEM
	 */
	public Integer getnVisitsEM() {
		return nVisitsEM;
	}

	/**
	 * @param nVisitsEM the nVisitsEM to set
	 */
	public void setnVisitsEM(Integer nVisitsEM) {
		this.nVisitsEM = nVisitsEM;
	}

	/**
	 * @return the avgForEM
	 */
	public Double getAvgForEM() {
		return avgForEM;
	}

	/**
	 * @param avgForEM the avgForEM to set
	 */
	public void setAvgForEM(Double avgForEM) {
		this.avgForEM = avgForEM;
	}

	/**
	 * @return the nVisitsMM
	 */
	public Integer getnVisitsMM() {
		return nVisitsMM;
	}

	/**
	 * @param nVisitsMM the nVisitsMM to set
	 */
	public void setnVisitsMM(Integer nVisitsMM) {
		this.nVisitsMM = nVisitsMM;
	}

	/**
	 * @return the avgForMM
	 */
	public Double getAvgForMM() {
		return avgForMM;
	}

	/**
	 * @param avgForMM the avgForMM to set
	 */
	public void setAvgForMM(Double avgForMM) {
		this.avgForMM = avgForMM;
	}

	/**
	 * @return the nVisistsA
	 */
	public Integer getnVisistsA() {
		return nVisistsA;
	}

	/**
	 * @param nVisistsA the nVisistsA to set
	 */
	public void setnVisistsA(Integer nVisistsA) {
		this.nVisistsA = nVisistsA;
	}

	/**
	 * @return the avgForA
	 */
	public Double getAvgForA() {
		return avgForA;
	}

	/**
	 * @param avgForA the avgForA to set
	 */
	public void setAvgForA(Double avgForA) {
		this.avgForA = avgForA;
	}

	/**
	 * @return the nVisistsE
	 */
	public Integer getnVisistsE() {
		return nVisistsE;
	}

	/**
	 * @param nVisistsE the nVisistsE to set
	 */
	public void setnVisistsE(Integer nVisistsE) {
		this.nVisistsE = nVisistsE;
	}

	/**
	 * @return the avgForE
	 */
	public Double getAvgForE() {
		return avgForE;
	}

	/**
	 * @param avgForE the avgForE to set
	 */
	public void setAvgForE(Double avgForE) {
		this.avgForE = avgForE;
	}

	
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
	
	
	
	public double getMedianForDay() {
		return medianForDay;
	}

	public double getMedianForEM() {
		return medianForEM;
	}

	public double getMedianForMM() {
		return medianForMM;
	}

	public double getMedianForA() {
		return medianForA;
	}

	public Double getMedianForE() {
		return medianForE;
	}

	public DetailedDataEntry(LocalDate ld, Segment s, HitHolder vholder) {
		this.originId = s.getOriginId();
		this.destinationId = s.getDestinationId();
		this.day = ld.atStartOfDay();
		this.theGeom = s.getTheGeom();
		this.tags = s.getTags();
		
		this.avgForEM = vholder.getAverageTimeBetweenVisits(TimeSlot.EARLY_MORNING);
		this.nVisitsEM = vholder.getTotalVisits(TimeSlot.EARLY_MORNING);
		this.medianForEM = vholder.getMedianTimeBetweenVisits(TimeSlot.EARLY_MORNING);
		
		this.avgForMM = vholder.getAverageTimeBetweenVisits(TimeSlot.MID_MORNING);
		this.nVisitsMM = vholder.getTotalVisits(TimeSlot.MID_MORNING);
		this.medianForMM = vholder.getMedianTimeBetweenVisits(TimeSlot.MID_MORNING);
		
		this.avgForA= vholder.getAverageTimeBetweenVisits(TimeSlot.AFTERNOON);
		this.nVisistsA = vholder.getTotalVisits(TimeSlot.AFTERNOON);
		this.medianForA = vholder.getMedianTimeBetweenVisits(TimeSlot.AFTERNOON);
		
		this.avgForE= vholder.getAverageTimeBetweenVisits(TimeSlot.EVENING);
		this.nVisistsE = vholder.getTotalVisits(TimeSlot.EVENING);
		this.medianForE = vholder.getMedianTimeBetweenVisits(TimeSlot.EVENING);
		
		this.avgForDay= vholder.getAverageTimeBetweenVisits(TimeSlot.WHOLE_DAY);
		this.nVisitsInDay = vholder.getTotalVisits(TimeSlot.WHOLE_DAY);
		this.medianForDay = vholder.getMedianTimeBetweenVisits(TimeSlot.WHOLE_DAY);
	}

	public long getOriginId() {
		return originId;
	}

	public long getDestinationId() {
		return destinationId;
	}
}
