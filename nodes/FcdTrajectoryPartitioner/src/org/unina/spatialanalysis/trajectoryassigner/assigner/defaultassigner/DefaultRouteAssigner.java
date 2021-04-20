package org.unina.spatialanalysis.trajectoryassigner.assigner.defaultassigner;



import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.unina.spatialanalysis.trajectoryassigner.assigner.RouteAssigner;
import org.unina.spatialanalysis.trajectoryassigner.entity.position.GPSPosition;
import org.unina.spatialanalysis.trajectoryassigner.entity.position.Position;




/**
 * The Default Route Assigner. Given a Set of Positions ordered according to their time of record
 * it assigns them to specific subsets, each identifying a route.
 * @author sinog
 *
 * @param <T> The specific kind of Position to assign to routes.
 */
public class DefaultRouteAssigner<T extends Position> implements RouteAssigner<T> {

    /**
     * A value in minutes that is used to specify whenever a route ends and another begins. If the recording
     * of positions are stationary for the value of the constant the program identifies it as a new route.
     */
    private final long LIMIT_FOR_NEW_ROUTE;
    /**
     * The minimum number of recordings to be present in a route to be considered significant.
     */
    private final int MINIMUM_RECORDINGS_FOR_ROUTE;


    private final boolean OVER_MULTIPLE_DAYS;

    /**
     * The methods takes a Set of positions ordered by the time of record in ascending order and returns a mapping
     * of routes, separating them accordingly to the constant value LIMIT_FOR_NEW_ROUTE. If a position was either to close
     * to its predecessor (less than MINIMUM_DISTANCE_AWAY) or if the velocity needed to reach it from its predecessor was implausible
     * (more than MAXIMUM_VELOCITY) the position is ignored.
     * Each route containing less than the MINIMU_RECORDINGS_FOR_ROUTE gets discarded.
     * The boolean flag OVER_MULTIPLE_DAYS informs the method if two positions recorded in different days can belong to the same route.
     * @param positions a Set of positions ordered by time of record.
     * @return a mapping of Integers, which serve as routes identifiers, and TreeSets of taxi positions, which represents routes.
     */
    @SuppressWarnings("deprecation")
	public Map<Integer, TreeSet<T>> identifyRoutes(SortedSet<T> positions){
        Map<Integer, TreeSet<T>> routes = new HashMap<>();
        int i = 0;
        TreeSet<T> aRoute = new TreeSet<>();
        T prevPosition = null;
        double distance;
        double time;
        double velocity;
        for(T tp: positions){

            if(aRoute.isEmpty()){
                aRoute.add(tp);
                prevPosition = tp;
            }else if(tp.getTimeOfRecord().getTime()-prevPosition.getTimeOfRecord().getTime()<LIMIT_FOR_NEW_ROUTE ){
            	if(OVER_MULTIPLE_DAYS) {
                    GPSPosition currentPosition = tp.getLocation();
                    GPSPosition precedentPosition = prevPosition.getLocation();
                    distance = precedentPosition.getDistanceInMeters(currentPosition);
                    if(distance!=0) {
                    	aRoute.add(tp);
                        prevPosition = tp;
                    }
            	}else{
            		if(tp.getTimeOfRecord().getDate()==prevPosition.getTimeOfRecord().getDate() &&
            				tp.getTimeOfRecord().getMonth()==prevPosition.getTimeOfRecord().getMonth() &&
            					tp.getTimeOfRecord().getYear()==prevPosition.getTimeOfRecord().getYear()) {
            			 GPSPosition currentPosition = tp.getLocation();
                         GPSPosition precedentPosition = prevPosition.getLocation();
                         distance = precedentPosition.getDistanceInMeters(currentPosition);
                         if(distance!=0) {
                        	 aRoute.add(tp);
                             prevPosition = tp;
                         }
            		}else {
            			 if(aRoute.size()>MINIMUM_RECORDINGS_FOR_ROUTE) {
                             routes.put(i, aRoute);
                             i++;
                             aRoute = new TreeSet<>();
                             aRoute.add(tp);
                         }
                         prevPosition = tp;
            		}		
            	}
            }else{
                if(aRoute.size()>MINIMUM_RECORDINGS_FOR_ROUTE) {
                    routes.put(i, aRoute);
                    i++;
                    aRoute = new TreeSet<>();
                    aRoute.add(tp);
                }
                prevPosition = tp;
            }
        }
        return routes;
    }
    
    /**
     * The constructor assigns the constants of the DefaultRouteAssigner accordingly to the given settings of the Node.
     * @param stretchOverMultipleDays
     * @param maxTime
     * @param maxSpeed
     * @param minDistance
     */
    public DefaultRouteAssigner(boolean stretchOverMultipleDays, int maxTime, int minRecordings) {
		this.OVER_MULTIPLE_DAYS = stretchOverMultipleDays;
		this.LIMIT_FOR_NEW_ROUTE = maxTime * 60 * 1000;
		this.MINIMUM_RECORDINGS_FOR_ROUTE = minRecordings;
    }
    
}
