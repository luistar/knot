package org.unina.spatialanalysis.RouteStepAnalyzer.entity.routesteps;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.unina.spatialanalysis.RouteStepAnalyzer.entity.visit.TimeSlot;

public class HitHolder {
	
	private TimeDataHolder earlyMorningTimeData;
	private TimeDataHolder midMorningTimeData;
	private TimeDataHolder afternoonTimeData;
	private TimeDataHolder eveningTimeData;
	private TimeDataHolder wholeDayTimeData;
	
	public HitHolder(){
		super();
		earlyMorningTimeData = new TimeDataHolder();
		midMorningTimeData = new TimeDataHolder();
		afternoonTimeData = new TimeDataHolder();
		eveningTimeData = new TimeDataHolder();
		wholeDayTimeData = new TimeDataHolder();
	}
	
	public void addHit(Hit h) {
		TimeSlot timeSlot = TimeSlot.getTimeSlot(h.getBegin());
		switch(timeSlot) {
		case EARLY_MORNING:
			earlyMorningTimeData.addHit(h);
			wholeDayTimeData.addHit(h);
			return;
		case MID_MORNING:
			midMorningTimeData.addHit(h);
			wholeDayTimeData.addHit(h);
			return;
		case AFTERNOON:
			afternoonTimeData.addHit(h);
			wholeDayTimeData.addHit(h);
			return;
		case EVENING:
			eveningTimeData.addHit(h);
			wholeDayTimeData.addHit(h);
			return;
		case WHOLE_DAY:
			return;
		}
	}
	
	public int getTotalVisits(TimeSlot timeSlot) {
		switch(timeSlot) {
			case EARLY_MORNING: 
				return earlyMorningTimeData.getTotalHits();
			case MID_MORNING:
				return midMorningTimeData.getTotalHits();
			case AFTERNOON:
				return afternoonTimeData.getTotalHits();
			case EVENING:
				return eveningTimeData.getTotalHits();
			case WHOLE_DAY:
				return wholeDayTimeData.getTotalHits();
		}
		return 0;
	}
	
	public double getTotalTimeBetweenVisits(TimeSlot timeSlot) {
		switch(timeSlot) {
			case EARLY_MORNING: 
				return earlyMorningTimeData.getTimeBetweenHits();
			case MID_MORNING:
				return midMorningTimeData.getTimeBetweenHits();
			case AFTERNOON:
				return afternoonTimeData.getTimeBetweenHits();
			case EVENING:
				return eveningTimeData.getTimeBetweenHits();
			case WHOLE_DAY:
				return wholeDayTimeData.getTimeBetweenHits();
		}
		return 0;
	}
	
	public double getAverageTimeBetweenVisits(TimeSlot timeSlot) {
		switch(timeSlot) {
		case EARLY_MORNING: 
			return earlyMorningTimeData.getAverageTimeBetweenVisits();
		case MID_MORNING:
			return midMorningTimeData.getAverageTimeBetweenVisits();
		case AFTERNOON:
			return afternoonTimeData.getAverageTimeBetweenVisits();
		case EVENING:
			return eveningTimeData.getAverageTimeBetweenVisits();
		case WHOLE_DAY:
			return wholeDayTimeData.getAverageTimeBetweenVisits();
		}
	return 0;
	}
	

	private class TimeDataHolder{
		
		private int totalHits = 0;
		private double timeBetweenHits = 0;
		private LocalDateTime lastVisited = null;
			
		/**
		 * @return the totalHits
		 */
		public int getTotalHits() {
			return totalHits;
		}

		/**
		 * @return the timeBetweenHits
		 */
		public double getTimeBetweenHits() {
			return timeBetweenHits;
		}
		
		public double getAverageTimeBetweenVisits() {
			if(totalHits-1 <= 0) {
				return 0;
			}else {
				return timeBetweenHits/(totalHits-1);
			}
		}

		public void addHit(Hit h) {
			if(lastVisited==null) {
				this.lastVisited = h.getEnd();
				totalHits++;
			}else {
					//System.out.println(ChronoUnit.SECONDS.between(lastVisited, h.getBegin()));
					double timeInMicroSeconds = ChronoUnit.MICROS.between(lastVisited, h.getBegin());
				 	timeBetweenHits+=Math.ceil(timeInMicroSeconds/1000000);
				 	lastVisited = h.getEnd();
				 	totalHits++;
				}
			}
		}
}

