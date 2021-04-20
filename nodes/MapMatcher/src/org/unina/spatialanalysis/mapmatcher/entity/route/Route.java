package org.unina.spatialanalysis.mapmatcher.entity.route;

import java.time.LocalDateTime;

public class Route {


    private String routeAsGPSLinestring;
    private LocalDateTime routeBeginsAt;
    private int ownerId;
    private String routeId;
    
	/**
	 * @return the routeAsGPSLinestring
	 */
	public String getRouteAsGPSLinestring() {
		return routeAsGPSLinestring;
	}
	/**
	 * @return the routeBeginsAt
	 */
	public LocalDateTime getRouteBeginsAt() {
		return routeBeginsAt;
	}
	/**
	 * @return the ownerId
	 */
	public int getOwnerId() {
		return ownerId;
	}
	public Route(String routeAsGPSLinestring, LocalDateTime routeBeginsAt, int ownerId, String routeId) {
		super();
		this.routeAsGPSLinestring = routeAsGPSLinestring;
		this.routeBeginsAt = routeBeginsAt;
		this.ownerId = ownerId;
		this.routeId = routeId;
	}
	/**
	 * @return the routeId
	 */
	public String getRouteId() {
		return routeId;
	}
    
    
    
}
