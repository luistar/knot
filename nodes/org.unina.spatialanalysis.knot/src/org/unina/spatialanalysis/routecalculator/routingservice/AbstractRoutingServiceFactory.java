package org.unina.spatialanalysis.routecalculator.routingservice;

import org.unina.spatialanalysis.routecalculator.entity.position.Position;
import org.unina.spatialanalysis.routecalculator.entity.route.Route;

/**
 * The class AbstractRoutingServiceFactory provides the RoutingServiceFactory specified in the current configuration.
 */
public abstract class  AbstractRoutingServiceFactory{
    
	/**
     * Return the RoutingService specified by the setting routing mode.
     * @param <T> Some kind of Position
     * @param <S> Some kind of Route
     * @param mode The mode of the routing service.
     * @return An instance of RoutingService wrapped in the implemented interface.
     */
    public abstract <T extends Position, S extends Route> RoutingService<T, S> getRoutingService(String mode);
    
}