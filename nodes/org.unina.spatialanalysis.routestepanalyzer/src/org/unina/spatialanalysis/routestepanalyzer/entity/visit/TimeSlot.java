package org.unina.spatialanalysis.routestepanalyzer.entity.visit;

import java.time.LocalDateTime;

public enum TimeSlot {
	EARLY_MORNING("[00:00-08:00]"),
	MID_MORNING("[00:08-14:00]"),
	AFTERNOON("[14:00-20:00]"),
	EVENING("[20:00-24:00]"),
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
		if(hour>=0 && hour<8) {
			timeSlot = TimeSlot.EARLY_MORNING;
		}else if(hour>=8 && hour<14) {
			timeSlot = TimeSlot.MID_MORNING;
		}else if(hour>=14 && hour<20) {
			timeSlot = TimeSlot.AFTERNOON;
		}else if(hour>=20 && hour<=23) {
			timeSlot = TimeSlot.EVENING;
		}
		return timeSlot;
	}
}
