package org.unina.spatialanalysis.mapmatcher.entity.route;

import java.time.LocalDateTime;

public class RouteStep implements Comparable<RouteStep> {
	private int id;
	private long originId;
	private long destinationId;
	private double duration;
	private double distance;
	private LocalDateTime beginAt;
	private LocalDateTime endAt;
	private String routeId;
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @return the originId
	 */
	public long getOriginId() {
		return originId;
	}
	/**
	 * @return the destinationId
	 */
	public long getDestinationId() {
		return destinationId;
	}
	/**
	 * @return the beginAt
	 */
	public LocalDateTime getBeginAt() {
		return beginAt;
	}
	/**
	 * @return the endAt
	 */
	public LocalDateTime getEndAt() {
		return endAt;
	}
	/**
	 * @return the routeId
	 */
	public String getRouteId() {
		return routeId;
	}
	public RouteStep(int id, long originId, long destinationId,double duration, double distance, LocalDateTime beginAt, LocalDateTime endAt,
			String routeId) {
		super();
		this.id = id;
		this.originId = originId;
		this.destinationId = destinationId;
		this.duration = duration;
		this.distance = distance;
		this.beginAt = beginAt;
		this.endAt = endAt;
		this.routeId = routeId;
	}
	@Override
	public int compareTo(RouteStep o) {
		if(this.beginAt.isAfter(o.beginAt)) {
			return 1;
		}else if(this.beginAt.isBefore(o.beginAt)) {
			return -1;
		}else {
			return 0;
		}
	}
	@Override
	public String toString() {
		return "RouteStep [id=" + id + ", originId=" + originId + ", destinationId=" + destinationId + ", duration="
				+ duration + ", distance=" + distance + ", beginAt=" + beginAt + ", endAt=" + endAt + ", routeId="
				+ routeId + "]";
	}
	
	public void setEndVisitTime(LocalDateTime end) {
		this.endAt = end;
	}
	
}
