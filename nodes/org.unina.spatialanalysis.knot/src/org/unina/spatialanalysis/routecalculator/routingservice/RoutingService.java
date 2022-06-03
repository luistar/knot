package org.unina.spatialanalysis.routecalculator.routingservice;


import java.util.TreeSet;

import org.unina.spatialanalysis.routecalculator.entity.position.Position;
import org.unina.spatialanalysis.routecalculator.entity.route.Route;


/**
 * The interface defines a RoutingService.
 * @author sinog
 *
 * @param <T> The specific kind of Position we want to use to calculate Routes.
 * @param <S> The specific king of Route we want to calculate.
 */
public interface RoutingService<T extends Position, S extends Route> {
   
	/**
	 * The method findRoute takes in input a TreeSet of T and calculates
	 * an S that traverse them.
	 * @param positions the Set of Position the result Route S must have traversed.
	 * @return an S that traverses the input Ts.
	 * @throws Exception
	 */
	public S findRoute(TreeSet<T> positions) throws Exception;
}
