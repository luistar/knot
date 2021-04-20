package org.unina.spatialanalysis.RouteStepAnalyzer.entity.routesteps;

import java.time.LocalDateTime;

public class Hit  implements Comparable<Hit>{
	
	private LocalDateTime begin;
	
	private LocalDateTime end;
	
	private int ownerId;
	
	/**
	 * @return the begin
	 */
	public LocalDateTime getBegin() {
		return begin;
	}

	/**
	 * @return the end
	 */
	public LocalDateTime getEnd() {
		return end;
	}

	/**
	 * @return the ownerId
	 */
	public int getOwnerId() {
		return ownerId;
	}
	
	public Hit(LocalDateTime begin, LocalDateTime end, int ownerId) {
		super();
		this.begin = begin;
		this.end = end;
		this.ownerId = ownerId;
	}

	@Override
	public int compareTo(Hit o) {
		if(this.begin.isAfter(o.begin)) {
			return 1;
		}else if(this.begin.isBefore(o.begin)) {
			return -1;
		}else {
			if(this.ownerId>o.ownerId) {
				return 1;
			}else if(this.ownerId<o.ownerId) {
				return -1;
			}else {
				return 0;
			}
		}
	}

	@Override
	public String toString() {
		return "Hit [begin=" + begin.toString() + ", end=" + end.toString() + ", ownerId=" + ownerId + "]";
	}
	
	
}
