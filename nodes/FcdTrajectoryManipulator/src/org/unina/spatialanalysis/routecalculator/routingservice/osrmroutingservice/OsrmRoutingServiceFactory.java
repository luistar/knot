package org.unina.spatialanalysis.routecalculator.routingservice.osrmroutingservice;

import org.unina.spatialanalysis.routecalculator.entity.position.Position;
import org.unina.spatialanalysis.routecalculator.entity.route.Route;
import org.unina.spatialanalysis.routecalculator.routingservice.AbstractRoutingServiceFactory;
import org.unina.spatialanalysis.routecalculator.routingservice.RoutingService;

public class OsrmRoutingServiceFactory extends AbstractRoutingServiceFactory {
	private final String host;
	
	public OsrmRoutingServiceFactory(String host) {
		this.host= host;
	}
	
	/**
	 * The method returns either an instance of OsrmMatchedRoutingService, if the mode is 'match', or
	 * OsrmShortestRoutingService, if the mode is 'shortest'.
	 */
	@Override
	public <T extends Position, S extends Route> RoutingService<T, S> getRoutingService(String mode) {
		 switch (mode){
            /* The match strategy has been eliminated from the possibilities as 
             * the addition of the node Trajectory Assigner makes it useless in a 
             * pipeline of the Spatial Analysis node. It is left here ONLY as an
             * example for providing multiple strategies for an eventual new routing service.
             * 
             * case "match":
             *   return new OsrmMatchedRoutingService<T,S>(host);
             */
            case "shortest":
            	return new OsrmShortestRoutingService<T,S>(host);
            default:
                System.out.println("No such mode!");
                return null;
        }
	}
}
