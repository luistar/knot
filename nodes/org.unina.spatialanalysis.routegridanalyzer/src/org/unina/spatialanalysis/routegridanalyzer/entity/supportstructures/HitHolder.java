package org.unina.spatialanalysis.routegridanalyzer.entity.supportstructures;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.TreeSet;

import org.unina.spatialanalysis.routegridanalyzer.entity.grid.Hit;

public class HitHolder extends HashMap<Integer, TreeSet<Hit>> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7420395103900010773L;
	
	private final int TIME_IN_MINUTES;
	
	private HashMap<Integer, int[]> lastRecordedPositions = new HashMap<Integer, int[]>();
		
	public void addHit(Hit h, int[] position) {
		if(this.containsKey(h.getOwnerId())) {
			/*
			 * There is already a set for this id. I take the last recorded (the one with maximum time)
			 */
			Hit lastRecorded = this.get(h.getOwnerId()).last();
			try {
				//Get the position associated with the parameter
				if(lastRecordedPositions.containsKey(h.getOwnerId())) {
					int [] prevGridPosition = lastRecordedPositions.get(h.getOwnerId());
					if(position[0]!= prevGridPosition[0] || position[1] != prevGridPosition[1]) {
						//Here the position in the grid is changed, we record it.
						this.get(h.getOwnerId()).add(h);
						lastRecordedPositions.put(h.getOwnerId(), position);
					}else {
						if(ChronoUnit.MINUTES.between(lastRecorded.getTime(), h.getTime())>TIME_IN_MINUTES) {
							this.get(h.getOwnerId()).add(h);
						}
					}
				}
				
				
			}catch(IllegalArgumentException e) {
				//Here the position is out of bounds.
				return;
			}
		}else {
			TreeSet<Hit> tmp = new TreeSet<Hit>();
			tmp.add(h);
			this.put(h.getOwnerId(), tmp);
			this.lastRecordedPositions.put(h.getOwnerId(), position);
		}
	}
	
	public HitHolder(int timeInMinutes) {
		this.TIME_IN_MINUTES = timeInMinutes;
		
	}
}
