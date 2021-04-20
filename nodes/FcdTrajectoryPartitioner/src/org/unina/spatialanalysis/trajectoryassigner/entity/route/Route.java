package org.unina.spatialanalysis.trajectoryassigner.entity.route;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.unina.spatialanalysis.trajectoryassigner.entity.position.*;


/**
 * A class representing a Route.
 * @author sinog
 *
 */
public class Route {

    private String routeAsGPSLinestring;
    private LocalDateTime routeBeginsAt;
    private int ownerId;
    private long routeId;


    /**
     * The constructor takes a JSON response of OSRM, with the geometry parameters set as geojson. It then extracts the
     * coordinates from the object and stores it internally as a String. The starting and ending locations are stored
     * in two points.
     * @param coordinates A JSON array of coordinates.
     */
    public Route(int ownerId, long routeId, ArrayList<GPSPosition> coordinates, LocalDateTime routeBeginsAt) {
        this.routeBeginsAt = routeBeginsAt;
        this.ownerId = ownerId;
        this.routeId = routeId;
        double latitude;
        double longitude;
        routeAsGPSLinestring = "LINESTRING( ";
        for(int i = 0; i<coordinates.size(); i++){
            latitude = coordinates.get(i).getLat();
            longitude = coordinates.get(i).getLon();
            if(i==0){
                routeAsGPSLinestring += longitude + " " + latitude + ",";
            }else if(i==coordinates.size()-1){
                routeAsGPSLinestring += " "+ longitude + " " + latitude + ")";
            }else{
                routeAsGPSLinestring += " "+ longitude + " " + latitude + ",";
            }
        }
    }


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


	/**
	 * @return the routeId
	 */
	public long getRouteId() {
		return routeId;
	}
    
    

}
