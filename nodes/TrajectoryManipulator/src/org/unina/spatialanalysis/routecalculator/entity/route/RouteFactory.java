package org.unina.spatialanalysis.routecalculator.entity.route;

import java.util.ArrayList;

import org.unina.spatialanalysis.routecalculator.entity.position.GPSPosition;
import org.unina.spatialanalysis.routecalculator.entity.position.Position;

/**
 * The RouteFactory creates objects of type Route. 
 * @author sinog
 *
 */
public class RouteFactory {

    @SuppressWarnings("unchecked")
	public static <T extends Position, S extends Route>  S createRoute(ArrayList<GPSPosition> coordinates, double aDistance, double aDuration, T initialPosition, T finalPosition){
       switch ("default"){
           case "default":
        	   	Route r =  new Route(coordinates, aDistance, aDuration,initialPosition , finalPosition);
                return (S) r;
           default:
                System.out.println("Invalid Configuration!");
                return null;
       }
    }
}