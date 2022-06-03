package org.unina.spatialanalysis.routestepanalyzer.entity.routesteps;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import org.unina.spatialanalysis.routestepanalyzer.entity.visit.TimeSlot;

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
	
	public double getMedianTimeBetweenVisits(TimeSlot timeSlot) {
		switch(timeSlot) {
		case EARLY_MORNING: 
			return earlyMorningTimeData.getMedianTimeBetweenVisits();
		case MID_MORNING:
			return midMorningTimeData.getMedianTimeBetweenVisits();
		case AFTERNOON:
			return afternoonTimeData.getMedianTimeBetweenVisits();
		case EVENING:
			return eveningTimeData.getMedianTimeBetweenVisits();
		case WHOLE_DAY:
			return wholeDayTimeData.getMedianTimeBetweenVisits();
		}
	return 0;
	}

	private class TimeDataHolder{
		
		private int totalHits = 0;
		private double timeBetweenHits = 0;
		private LocalDateTime lastVisited = null;
		private ArrayList<Double> timegaps = new ArrayList<>();
		private ArrayList<LocalDateTime> recovery = new ArrayList<>();
			
		/**
		 * @return the totalHits
		 */
		public int getTotalHits() {
			return totalHits;
		}

		public double getMedianTimeBetweenVisits() {
			double medianTime;
			timegaps.sort((d1, d2)->{
				if(d1>d2) {
					return 1;
				}else if(d2>d1) {
					return -1;
				}else {
					return 0;
				}
			});
			int size = timegaps.size();
			if(size>1) {
				if(size%2==0) {
					medianTime = (timegaps.get((size/2)-1) + timegaps.get(size/2))/2;
				}else {
					medianTime = timegaps.get(((size+1)/2)-1);
				}
				return medianTime;
			}else {
				return 0;
			}
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
			totalHits++;
			if(lastVisited==null) {
				this.lastVisited = h.getEnd();
				timegaps.add(0.0);
				recovery.add(lastVisited);
			}else {
				double timeBetween = Math.ceil(ChronoUnit.SECONDS.between(lastVisited, h.getBegin()));
					if(timeBetween>0) {
						double timeInSeconds = Math.ceil(ChronoUnit.SECONDS.between(lastVisited, h.getBegin()));
				 		timeBetweenHits+= timeInSeconds;
				 		lastVisited = h.getEnd();
				 		timegaps.add(timeBetween);
				 		recovery.add(lastVisited);
					}else {
						int j = 0;
						while(Math.ceil(ChronoUnit.SECONDS.between(recovery.get(j), h.getBegin()))>0) {
							j++;
						}
						timeBetween = Math.ceil(ChronoUnit.SECONDS.between(recovery.get(j), h.getBegin()));
						timegaps.add(timeBetween);
						if(lastVisited.isBefore(h.getEnd())) {
							lastVisited = h.getEnd();
							recovery.add(lastVisited);
						}
					}
				}
			}
	}
}

