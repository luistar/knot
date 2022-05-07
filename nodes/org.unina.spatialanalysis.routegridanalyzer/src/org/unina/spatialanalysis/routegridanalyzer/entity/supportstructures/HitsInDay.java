package org.unina.spatialanalysis.routegridanalyzer.entity.supportstructures;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.unina.spatialanalysis.routegridanalyzer.entity.TimeSlot;

public class HitsInDay {
	
	private HitsInTimeSlots earlyMorningHits;
	private HitsInTimeSlots midMorningHits;
	private HitsInTimeSlots afternoonHits;
	private HitsInTimeSlots eveningHits;
	
	public int getTotalVisits(TimeSlot slot) {
		switch(slot) {
		case EARLY_MORNING:
			return this.earlyMorningHits.totalVisits;
		case MID_MORNING:
			return this.midMorningHits.totalVisits;
		case AFTERNOON:
			return this.afternoonHits.totalVisits;
		case EVENING:
			return this.eveningHits.totalVisits;
		default:
			return this.earlyMorningHits.totalVisits + this.midMorningHits.totalVisits + this.afternoonHits.totalVisits+this.eveningHits.totalVisits;
		}	
	}
	
	public double getTotalTimeBetweenVisits(TimeSlot slot) {
		switch(slot) {
		case EARLY_MORNING:
			return this.earlyMorningHits.totalTimeBetweenVisits;
		case MID_MORNING:
			return this.midMorningHits.totalTimeBetweenVisits;
		case AFTERNOON:
			return this.afternoonHits.totalTimeBetweenVisits;
		case EVENING:
			return this.eveningHits.totalTimeBetweenVisits;
		default:
			return this.earlyMorningHits.totalTimeBetweenVisits + this.midMorningHits.totalTimeBetweenVisits + this.afternoonHits.totalTimeBetweenVisits+this.eveningHits.totalTimeBetweenVisits;
		}	
	}
	
	public double getAverageTimeBetweenVisits(TimeSlot slot) {
	switch(slot) {
		case EARLY_MORNING:
			return this.earlyMorningHits.computeAvgTime();
		case MID_MORNING:
			return this.midMorningHits.computeAvgTime();
		case AFTERNOON:
			return this.afternoonHits.computeAvgTime();
		case EVENING:
			return this.eveningHits.computeAvgTime();
		default:{
				double totalVisits = this.getTotalVisits(TimeSlot.WHOLE_DAY);
				if(totalVisits>0) {
					return this.getTotalTimeBetweenVisits(TimeSlot.WHOLE_DAY)/totalVisits;
				}else {
					return 0;
				}
			}
		}
	}
	
	public void addHit(LocalDateTime ldt) {
		switch(TimeSlot.getTimeSlot(ldt)) {
			case EARLY_MORNING:
				this.earlyMorningHits.addVisit(ldt);
				return;
			case MID_MORNING:
				this.midMorningHits.addVisit(ldt);
				return;
			case AFTERNOON:
				this.afternoonHits.addVisit(ldt);
				return;
			case EVENING:
				this.eveningHits.addVisit(ldt);
				return;
			default:
				break;
		}
	}
	
	public HitsInDay() {
		super();
		this.earlyMorningHits = new HitsInTimeSlots();
		this.midMorningHits = new HitsInTimeSlots();
		this.afternoonHits = new HitsInTimeSlots();
		this.eveningHits = new HitsInTimeSlots();
	}

	private class HitsInTimeSlots {
		private int totalVisits;
		private double totalTimeBetweenVisits;
		
		private LocalDateTime lastRecordedVisit;
		
		public void addVisit(LocalDateTime visitTime) {
			totalVisits++;
			if(lastRecordedVisit!=null) {
				totalTimeBetweenVisits = ChronoUnit.SECONDS.between(lastRecordedVisit, visitTime);
			}
			lastRecordedVisit = visitTime;
		}
		
		public double computeAvgTime() {
			if(totalVisits==0) {
				return 0;
			}else {
				return totalTimeBetweenVisits/totalVisits;
			}
		}
	}
 	
}
