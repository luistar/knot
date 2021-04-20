package org.unina.spatialanalysis.RouteStepAnalyzer.entity.visit;

import java.time.LocalDateTime;

public enum TimeSlot {
	EARLY_MORNING("[00:00-06:00]"),
	MID_MORNING("[00:06-12:00]"),
	AFTERNOON("[12:00-18:00]"),
	EVENING("[18:00-24:00]"),
	WHOLE_DAY("");
	
	TimeSlot(String string) {
		timeInterval =string;
	}

	private String timeInterval;
	
	public String toString() {
		return timeInterval;
	}
	
	public static TimeSlot getTimeSlot(LocalDateTime ldt) {
		TimeSlot timeSlot = null;
		int hour = ldt.getHour();
		if(hour>=0 && hour<6) {
			timeSlot = TimeSlot.EARLY_MORNING;
		}else if(hour>=6 && hour<12) {
			timeSlot = TimeSlot.MID_MORNING;
		}else if(hour>=12 && hour<18) {
			timeSlot = TimeSlot.AFTERNOON;
		}else if(hour>=18 && hour<=23) {
			timeSlot = TimeSlot.EVENING;
		}
		return timeSlot;
	}
}
