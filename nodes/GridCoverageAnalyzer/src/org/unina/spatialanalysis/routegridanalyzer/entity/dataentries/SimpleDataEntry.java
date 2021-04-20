package org.unina.spatialanalysis.routegridanalyzer.entity.dataentries;

import org.unina.spatialanalysis.routegridanalyzer.entity.grid.GridSlot;

public class SimpleDataEntry {
	private String gridId;
	private String theGeom;
	private Integer totalVisit;

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
	public Integer getTotalVisit() {
		return totalVisit;
	}
	/**
	 * @param totalVisit the totalVisit to set
	 */
	public void setTotalVisit(Integer totalVisit) {
		this.totalVisit = totalVisit;
	}
	
	public SimpleDataEntry(int gridNumber, GridSlot gs) {
		super();
		this.gridId = "[" + gridNumber +"]";
		this.theGeom = gs.getTheGeom();
		this.totalVisit = gs.getTotalVisits();
	}

	public String getGridId() {
		return gridId;
	}
	
	
	
}
