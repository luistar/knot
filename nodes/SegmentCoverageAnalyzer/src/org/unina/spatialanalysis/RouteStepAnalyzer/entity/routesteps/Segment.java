package org.unina.spatialanalysis.RouteStepAnalyzer.entity.routesteps;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

public class Segment {
	
	private long originId;
	
	private long destinationId;
	
	private TreeSet<Hit> hits;
		
	private HashMap<LocalDate, HitHolder> data;

	private String tags;

	private String theGeom;
	
	private double totalTimeBetweenHits;
	
	private int numberOfHits;
	
	
	/**
	 * @return the originId
	 */
	public long getOriginId() {
		return originId;
	}

	
	
	/**
	 * @return the data
	 */
	public HashMap<LocalDate, HitHolder> getData() {
		return data;
	}

	/**
	 * @return the destinationId
	 */
	public long getDestinationId() {
		return destinationId ;
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

	


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (destinationId ^ (destinationId >>> 32));
		result = prime * result + (int) (originId ^ (originId >>> 32));
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Segment other = (Segment) obj;
		if (destinationId != other.destinationId)
			return false;
		if (originId != other.originId)
			return false;
		return true;
	}



	public Segment(long originId, long destinationId, String tags, String theGeom) {
		super();
		this.originId = originId;
		this.destinationId = destinationId;
		this.tags = tags;
		this.theGeom = theGeom;
		this.hits = new TreeSet<Hit>();
		this.totalTimeBetweenHits = 0;
		this.numberOfHits = 0;
	}

	/**
	 * @return the hits
	 */
	public TreeSet<Hit> getHits() {
		return hits;
	}

	
	public void generateResults() {
		this.data = new HashMap<LocalDate, HitHolder>();
		Iterator<Hit> i = this.hits.iterator();
		LocalDateTime lastVisited = null;
		while(i.hasNext()) {
			Hit  h = i.next();
			if(this.data.containsKey(h.getBegin().toLocalDate())) {
				HitHolder tmp = this.data.get(h.getBegin().toLocalDate());
				tmp.addHit(h);
			}else {
				HitHolder tmp = new HitHolder();
				tmp.addHit(h);
				this.data.put(h.getBegin().toLocalDate(), tmp);
			}
			numberOfHits++;
			if(lastVisited==null) {
				lastVisited = h.getEnd();
			}else {
				totalTimeBetweenHits = ChronoUnit.SECONDS.between(lastVisited, h.getBegin());
				lastVisited = h.getEnd();
			}
			
			
			i.remove();
		}
		
	}
	
	/**
	 * @return the totalTimeBetweenVisits
	 */
	public double getTotalTimeBetweenHits() {
		return totalTimeBetweenHits;
	}



	/**
	 * @return the numberOfHits
	 */
	public int getNumberOfHits() {
		return numberOfHits;
	}



	public boolean addHit(Hit h) {
		return this.hits.add(h);
	}
	
	
	
}
