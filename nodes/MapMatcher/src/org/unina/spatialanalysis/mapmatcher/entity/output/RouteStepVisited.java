package org.unina.spatialanalysis.mapmatcher.entity.output;

import java.time.LocalDateTime;
import java.util.Map;

import org.unina.spatialanalysis.mapmatcher.entity.osm.Node;

public class RouteStepVisited {
	
	private int ownerId;
	private String routeId;
	private LocalDateTime beginAt;
	private LocalDateTime endAt;
	private Node origin;
	private Node destination;
	private String tags;
	
	/**
	 * @return the ownerId
	 */
	public int getOwnerId() {
		return ownerId;
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
	 * @return the origin
	 */
	public Node getOrigin() {
		return origin;
	}
	/**
	 * @return the destination
	 */
	public Node getDestination() {
		return destination;
	}
	
	public String getTags() {
		return tags;
	}
	
	public RouteStepVisited(int ownerId, String routeId, LocalDateTime beginAt, LocalDateTime endAt, Node origin, Node destination, Map<String, String> tagsMap) {
		super();
		this.ownerId = ownerId;
		this.routeId = routeId;
		this.beginAt = beginAt;
		this.endAt = endAt;
		this.origin = origin;
		this.destination = destination;
		this.tags = "{";
		for(String s: tagsMap.keySet()) {
			tags += (" " + s + " = " + tagsMap.get(s) +";");
		}
		if(this.tags.lastIndexOf(";")!=-1) {
			this.tags = this.tags.substring(0, this.tags.length()-1);
		}
		this.tags += " }";
	}
	
	public String getTheGeom() {
		return "LINESTRING(" + origin.getLon() + " " +origin.getLat() + ", " + destination.getLon() + " " + destination.getLat() + ")";
	}
	
	public long getOriginId() {
		return origin.getNodeId();
	}
	
	public long getDestinationId() {
		return destination.getNodeId();
	}

	/**
	 * @return the routeId
	 */
	public String getRouteId() {
		return routeId;
	}
	
	
}
