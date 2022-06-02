package org.unina.spatialanalysis.routestepanalyzer.entity.routesteps;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

public class Segment {
	
	private final int MIN_TIME_BETWEEN_SETTING;
	
	private long originId;
	
	private long destinationId;
	
	private TreeSet<Hit> hits;
		
	private HashMap<LocalDate, HitHolder> data;

	private String tags;

	private String theGeom;
	
	private double totalTimeBetweenHits;
	
	private int numberOfHits;
	
	private double medianTime;
	
	/**
	 * @return the originId
	 */
	public long getOriginId() {
		return originId;
	}

	
	
	public double getMedianTime() {
		return medianTime;
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



	public Segment(long originId, long destinationId, String tags, String theGeom, int minTimeBetween) {
		super();
		this.originId = originId;
		this.destinationId = destinationId;
		this.tags = tags;
		this.theGeom = theGeom;
		this.hits = new TreeSet<Hit>();
		this.totalTimeBetweenHits = 0;
		this.numberOfHits = 0;
		this.MIN_TIME_BETWEEN_SETTING = minTimeBetween;
	}

	/**
	 * @return the hits
	 */
	public TreeSet<Hit> getHits() {
		return hits;
	}

	
	public void generateResults() throws IOException {
		
		this.data = new HashMap<LocalDate, HitHolder>();
		ArrayList<Double> timesInBetween = new ArrayList<>();
		Iterator<Hit> i = this.hits.iterator();
		
		LocalDateTime lastVisited = null;
		ArrayList<LocalDateTime> recovery = new ArrayList<>();;
		
		while(i.hasNext()) {
			Hit h = i.next();
			if(lastVisited==null) {
				if(this.data.containsKey(h.getBegin().toLocalDate())) {
					HitHolder tmp = this.data.get(h.getBegin().toLocalDate());
					tmp.addHit(h);
				}else {
					HitHolder tmp = new HitHolder();
					tmp.addHit(h);
					this.data.put(h.getBegin().toLocalDate(), tmp);
				}
				numberOfHits++;
				lastVisited = h.getEnd();
				recovery.add(h.getEnd());
				timesInBetween.add(0.0);
			}else {
				double timeBetween = Math.ceil(ChronoUnit.SECONDS.between(lastVisited, h.getBegin()));
				if(timeBetween>=MIN_TIME_BETWEEN_SETTING){
					timesInBetween.add(timeBetween);
					if(this.data.containsKey(h.getBegin().toLocalDate())) {
						HitHolder tmp = this.data.get(h.getBegin().toLocalDate());
						tmp.addHit(h);
					}else {
						HitHolder tmp = new HitHolder();
						tmp.addHit(h);
						this.data.put(h.getBegin().toLocalDate(), tmp);
					}
					totalTimeBetweenHits += timeBetween;
					numberOfHits++;
					lastVisited = h.getEnd();
					recovery.add(h.getEnd());
				}else if(timeBetween<0) {
					int j = 0;
					while(Math.ceil(ChronoUnit.SECONDS.between(recovery.get(j), h.getBegin()))>0) {
						j++;
					}
					timeBetween = Math.ceil(ChronoUnit.SECONDS.between(recovery.get(j), h.getBegin()));
	
					timesInBetween.add(timeBetween);
					if(this.data.containsKey(h.getBegin().toLocalDate())) {
						HitHolder tmp = this.data.get(h.getBegin().toLocalDate());
						tmp.addHit(h);
					}else {
						HitHolder tmp = new HitHolder();
						tmp.addHit(h);
						this.data.put(h.getBegin().toLocalDate(), tmp);
					}
					numberOfHits++;
					totalTimeBetweenHits += timeBetween;
				
				}
			}
			i.remove();
		}
		timesInBetween.sort((d1, d2)->{
			if(d1>d2) {
				return 1;
			}else if(d2>d1) {
				return -1;
			}else {
				return 0;
			}
		});
		int size = timesInBetween.size();
		if(size>1) {
			if(timesInBetween.size()%2==0) {
				medianTime = (timesInBetween.get((size/2)-1) + timesInBetween.get(size/2))/2;
			}else {
				medianTime = timesInBetween.get(((size+1)/2)-1);
			}
		}else {
			medianTime=0;
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
