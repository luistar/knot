package org.unina.spatialanalysis.trajectoryassigner.entity.position;

import java.sql.Timestamp;


/**
 * A representation of a vehicle position in the scope of the program.
 * It implements the interface Comparable<Position> to introduce an order over the time of recording
 * of the position.
 * @author sinog
 *
 */
public class Position implements Comparable<Position>{
	
	/**
	 * The id identifies the vehicle.
	 */
	private int id;
	
	/**
	 * The point in time this position was recorded.
	 */
    private Timestamp timeOfRecord;
    
    /**
     * The geographical information of this position.
     */
    private GPSPosition location;
    
    public int getId() {
        return id;
    }

    public Timestamp getTimeOfRecord() {
        return timeOfRecord;
    }

    public GPSPosition getLocation() {
        return location;
    }
    
    /**
     * A convenience constructor.
     * @param id 
     * @param timeOfRecord
     * @param location
     */
    public Position(int id, Timestamp timeOfRecord, GPSPosition location) {
        this.id = id;
        this.timeOfRecord = timeOfRecord;
        this.location = location;
    }

    public Position() {
    }
    
    /**
     * The method compareTo confronts two positions using the time in which they were recorded.
     * The order is ascending.
     */
    @Override
    public int compareTo(Position position) {
        if(this.getTimeOfRecord().after(position.getTimeOfRecord())){
            return 1;
        }else if(this.getTimeOfRecord().before(position.getTimeOfRecord())){
            return -1;
        }else {
            return 0;
        }
    }

    @Override
    public String toString() {
	        return "Position{" +
	                "\t id = " + id +
	                ",\t timeOfRecord = " + timeOfRecord +
	                ",\t location = " + location +
	                '}';
	    }
	/**
	 * A constructor to be called when reading data from the input table.
	 * It transforms the input Strings aTimestamp and aPosition in, respectively,
	 * a Timestamp object  and a pair of doubles. If the coordinates contained in aPosition are either more than two
	 * or could not be parsed as Double the constructor assigns NaN to lon and lat, meaning this position
	 * will be ignored when calculating routes.
	 * @param anId the id of the owner of the position.
	 * @param aTimestamp a String containing information on the time of record, in the format yyyy-[m]m-[d]d hh:mm:ss[.f...], with 
	 * 					 or without a TimeZone
	 * @param aPosition a WKT representation of a Point
	 * @param isLatLong it informs the constructor if aPosition is like {lat,lon} or {lon,lat}
	 */
    public Position(int anId, String aTimestamp, String aPosition, boolean isLatLong) {
    	this.id= anId;
    	if(aTimestamp.indexOf('+')!=-1) {
    		aTimestamp = aTimestamp.substring(0, aTimestamp.indexOf('+'));
    	}else if(aTimestamp.indexOf('-')!= -1) {
    		aTimestamp = aTimestamp.substring(0, aTimestamp.indexOf('-'));
    	}else if(aTimestamp.indexOf('Z')!= -1) {
    		aTimestamp = aTimestamp.substring(0, aTimestamp.indexOf('Z'));
    	}else if(aTimestamp.indexOf('z')!= -1) {
    		aTimestamp = aTimestamp.substring(0, aTimestamp.indexOf('z'));
    	}
    	this.timeOfRecord = Timestamp.valueOf(aTimestamp);
    	double lat;
    	double lon;
   		aPosition = aPosition.replace("POINT(", "");
		aPosition = aPosition.replace(")","");
		String coordinates[] = aPosition.split(" ");
		if(coordinates.length!=2) {
			lat = Double.NaN;
			lon = Double.NaN;
		}else {
	    	if(isLatLong) {
	    		try {
	    			lat = Double.parseDouble(coordinates[0]);
	    		}catch(NumberFormatException e) {
	    			lat = Double.NaN;
	    		}
	    		try {
	    			lon = Double.parseDouble(coordinates[1]);
	    		}catch(NumberFormatException e) {
	    			lon = Double.NaN;
	    		}
	    	}else{
	    		try {
	    			lat = Double.parseDouble(coordinates[1]);
	    		}catch(NumberFormatException e) {
	    			lat = Double.NaN;
	    		}
	    		try {
	    			lon = Double.parseDouble(coordinates[0]);
	    		}catch(NumberFormatException e) {
	    			lon = Double.NaN;
	    		}
	    	}
		}
    	this.location = new GPSPosition(lat, lon);
    	return;
    }
    
    /**
     * The method checks if the coordinates associated with this position are valid.
     * @return if one (or both) of lon and lat is NaN returns false, true otherwise
     */
	public boolean isValid() {
		return this.getLocation().getLat()!=Double.NaN && this.getLocation().getLon()!=Double.NaN;
	}
}
