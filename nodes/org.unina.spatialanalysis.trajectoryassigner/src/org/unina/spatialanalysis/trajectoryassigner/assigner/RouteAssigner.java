package org.unina.spatialanalysis.trajectoryassigner.assigner;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.unina.spatialanalysis.trajectoryassigner.entity.position.Position;


public interface RouteAssigner<T extends Position> {
	/**
	 * The method identifyRoutes separates the Sorted Set of Positions into different routes.
	 * @param positions The positions to separate into different routes.
	 * @return a mapping between Integers and Tree Sets of Positions, each identifying a route.
     */
	public Map<Integer, TreeSet<T>> identifyRoutes(SortedSet<T> positions);

}
