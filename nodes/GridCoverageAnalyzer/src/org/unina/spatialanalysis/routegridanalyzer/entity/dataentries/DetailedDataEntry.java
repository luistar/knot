package org.unina.spatialanalysis.routegridanalyzer.entity.dataentries;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.unina.spatialanalysis.routegridanalyzer.entity.TimeSlot;
import org.unina.spatialanalysis.routegridanalyzer.entity.supportstructures.HitsInDay;

public class DetailedDataEntry {
	
	private String gridId;
	
	private LocalDateTime day;
	
	private Integer nVisitsInDay;
		
	private Integer nVisitsEM;
		
	private Integer nVisitsMM;
		
	private Integer nVisistsA;
		
	private Integer nVisistsE;
		
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
	
	public DetailedDataEntry(LocalDate ld, int gridSlotNumber, String  gs, HitsInDay vholder) {
		this.gridId = "[" +gridSlotNumber+"]";
		this.day = ld.atStartOfDay();
		this.theGeom = gs;
		this.nVisitsEM = vholder.getTotalVisits(TimeSlot.EARLY_MORNING);
		this.nVisitsMM = vholder.getTotalVisits(TimeSlot.MID_MORNING);
		this.nVisistsA = vholder.getTotalVisits(TimeSlot.AFTERNOON);
		this.nVisistsE = vholder.getTotalVisits(TimeSlot.EVENING);
		this.nVisitsInDay = vholder.getTotalVisits(TimeSlot.WHOLE_DAY);
	}

	public String getGridId() {
		return gridId;
	}

}
