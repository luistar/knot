package org.unina.spatialanalysis.routecalculator.entity.route;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.unina.spatialanalysis.routecalculator.entity.position.GPSPosition;
import org.unina.spatialanalysis.routecalculator.entity.position.Position;


/**
 * A class representing a Route.
 * @author sinog
 *
 */
public class Route {

    private GPSPosition start;
    private GPSPosition end;
    private double duration;
    private double distance;
    private String routeAsGPSLinestring;
    private Timestamp routeBeginsAt;
    private Timestamp routeEndsAt;
    private int id;


    /**
     * The constructor takes a JSON response of OSRM, with the geometry parameters set as geojson. It then extracts the
     * coordinates from the object and stores it internally as a String. The starting and ending locations are stored
     * in two points.
     * @param coordinates A JSON array of coordinates.
     */
    public Route(ArrayList<GPSPosition> coordinates, Double aDistance, Double aDuration, Position startingPosition,Position finalPosition) {
        start = startingPosition.getLocation();
        end =  finalPosition.getLocation();
        routeBeginsAt = startingPosition.getTimeOfRecord();
        routeEndsAt = finalPosition.getTimeOfRecord();
        distance = aDistance;
        duration = aDuration;
        id = startingPosition.getId();
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
    

    public Route() {
		// TODO Auto-generated constructor stub
	}

	public <T extends Route> void  appendRoute( T toAppend) {
        end = toAppend.getEnd();
        duration +=toAppend.getDuration();
        distance +=toAppend.getDistance();
        routeEndsAt = (Timestamp) toAppend.getRouteEndsAt();
        routeAsGPSLinestring = routeAsGPSLinestring.substring(0, routeAsGPSLinestring.length()-1);
        routeAsGPSLinestring += ", " + toAppend.getRouteAsGPSLinestring().replace("LINESTRING(", "");
    }

    public GPSPosition getStart() {
        return start;
    }

    public GPSPosition getEnd() {
        return end;
    }

    public double getDuration() {
        return duration;
    }

    public double getDistance() {
        return distance;
    }

    public String getRouteAsGPSLinestring() {
        return routeAsGPSLinestring;
    }

    public Timestamp getRouteBeginsAt() {
        return routeBeginsAt;
    }

    public Timestamp getRouteEndsAt() {
        return routeEndsAt;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString(){
        return  "ID: " + id + "\n"+
                "Punto iniziale: " + start.asWKT() + " a " + routeBeginsAt.toString() +  "\n"+
                "Punto finale: "  + end.asWKT()   + " a " + routeEndsAt.toString() + "\n"+
                "Durata: " + duration + " s\n"+
                "Distanza: " + distance + "m\n"+
                "Geometria: " + routeAsGPSLinestring + "\n";
    }
}
