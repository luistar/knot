package org.unina.spatialanalysis.routegridanalyzer.entity.grid;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Set;

import org.unina.spatialanalysis.routegridanalyzer.entity.supportstructures.HitsInDay;

public class GridSlot {
	
	private double minLat;
	private double maxLat;
	private double minLon;
	private double maxLon;
	
	private HashMap<LocalDate, HitsInDay> visits;
	
	private int totalVisits;
	private double totalTimeBetweenVisits;
	
	private LocalDateTime lastRecordedVisit;
	
	private String theGeom;

	/**
	 * @return the minLat
	 */
	public double getMinLat() {
		return minLat;
	}

	/**
	 * @return the maxLat
	 */
	public double getMaxLat() {
		return maxLat;
	}

	/**
	 * @return the minLon
	 */
	public double getMinLon() {
		return minLon;
	}

	/**
	 * @return the maxLon
	 */
	public double getMaxLon() {
		return maxLon;
	}

	/**
	 * @return the the_geom
	 */
	public String getTheGeom() {
		return theGeom;
	}
	
	public void addVisit(LocalDateTime visitTime) {
		if(lastRecordedVisit!=null) {
			totalTimeBetweenVisits = ChronoUnit.SECONDS.between(lastRecordedVisit, visitTime);
		}
		lastRecordedVisit = visitTime;
		totalVisits++;
		LocalDate ld = visitTime.toLocalDate();
		if(!this.visits.keySet().contains(ld)) {
			this.visits.put(ld, new HitsInDay());
		}
		this.visits.get(ld).addHit(visitTime);
	}
	

	public GridSlot(double minLat, double maxLat, double minLon, double maxLon) {
		super();
		this.minLat = minLat;
		this.maxLat = maxLat;
		this.minLon = minLon;
		this.maxLon = maxLon;
		theGeom = "POLYGON((" + minLon + " " + minLat +"," + 
								minLon + " " + maxLat +"," + 
								maxLon + " " + maxLat +"," + 
								maxLon + " " + minLat +"," +
								minLon + " " + minLat +"))";
		this.totalTimeBetweenVisits = 0;
		this.totalVisits = 0;
		this.lastRecordedVisit = null;
		this.visits = new HashMap<LocalDate,HitsInDay>();
	}

	/**
	 * @return the visits
	 */
	public HashMap<LocalDate, HitsInDay> getVisits() {
		return visits;
	}

	/**
	 * @return the totalVisits
	 */
	public int getTotalVisits() {
		return totalVisits;
	}

	/**
	 * @return the totalTimeBetweenVisits
	 */
	public double getTotalTimeBetweenVisits() {
		return totalTimeBetweenVisits;
	}

	@Override
	public String toString() {
		return "GridSlot [minLat=" + minLat + ", maxLat=" + maxLat + ", minLon=" + minLon + ", maxLon=" + maxLon
				+ ", visits=" + visits + ", totalVisits=" + totalVisits + ", totalTimeBetweenVisits="
				+ totalTimeBetweenVisits + ", lastRecordedVisit=" + lastRecordedVisit + ", theGeom=" + theGeom + "]";
	}

	public boolean containsPoint(double lon, double lat) {
		if(lon>=this.minLon && lon<this.maxLon && lat>=this.minLat && lat<this.maxLat) {
			return true;
		}else {
			return false;
		}
	}
	
	public void normalizeForDay(Set<LocalDate> dates) {
		for(LocalDate ld: dates) {
			if(!this.visits.containsKey(ld)) {
				this.visits.put(ld, new HitsInDay());
			}
		}
	}
}
