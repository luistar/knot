package org.unina.spatialanalysis.routegridanalyzer.entity.grid;

import java.time.LocalDateTime;

public class Hit implements Comparable<Hit> {
	
	private double longitude;
	private double latitude;
	private LocalDateTime time;
	private int ownerId;
	
	@Override
	public int compareTo(Hit o) {
		if(this.time.isAfter(o.time)) {
			return 1;
		}else if(this.time.isBefore(o.time)) {
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
	
	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}


	/**
	 * @return the time
	 */
	public LocalDateTime getTime() {
		return time;
	}

	/**
	 * @return the ownerId
	 */
	public int getOwnerId() {
		return ownerId;
	}

	public Hit(double longitude,double latitude, LocalDateTime time, int ownerId) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.time = time;
		this.ownerId = ownerId;
	}

	@Override
	public String toString() {
		return "Hit [longitude=" + longitude + ", latitude=" + latitude + ", time=" + time + ", ownerId=" + ownerId
				+ "]";
	}

	
}
