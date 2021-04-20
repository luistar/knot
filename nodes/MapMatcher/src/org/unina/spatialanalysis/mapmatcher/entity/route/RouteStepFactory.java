package org.unina.spatialanalysis.mapmatcher.entity.route;

import java.time.LocalDateTime;

/**
 * The class RouteStepFactory creates a new RouteStep object. It offers methods for creating different kind of RouteSteps objects.
 * @author Sinogrante Principe
 *
 * @param <T> The sub-class of RouteStep this factory generates.
 */
public class RouteStepFactory<T extends RouteStep> {

	@SuppressWarnings("unchecked")
	public T createDefaultRouteStep(String routeId,int ownerId,long origin,long destination, double time, double distance, LocalDateTime beginTime, LocalDateTime endTime) {
		RouteStep res = new RouteStep(ownerId, origin, destination ,time, distance, beginTime, endTime, routeId);
		return (T) res;
	}
}
