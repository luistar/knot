package org.unina.spatialanalysis.mapmatcher.entity.position;

import java.sql.Timestamp;



public class Position implements Comparable<Position>{
	
	private int id;
    private Timestamp timeOfRecord;
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

    public Position(int id, Timestamp timeOfRecord, GPSPosition location) {
        this.id = id;
        this.timeOfRecord = timeOfRecord;
        this.location = location;
    }

    public Position() {
    }

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
	    
    public Position(int anId, String aTimestamp, String aPosition, boolean isLatLong) {
    	this.id= anId;
    	aTimestamp = aTimestamp.replace("+01", "");
    	this.timeOfRecord = Timestamp.valueOf(aTimestamp);
    	double lat;
    	double lon;
    	if(isLatLong) {
    		lat = Double.parseDouble(aPosition.substring(aPosition.indexOf('(')+1, aPosition.indexOf(' ')-1));
    		lon = Double.parseDouble(aPosition.substring(aPosition.indexOf(' ')+1, aPosition.indexOf(')')-1));
    	}else{
    		lon = Double.parseDouble(aPosition.substring(aPosition.indexOf('(')+1, aPosition.indexOf(' ')-1));
    		lat = Double.parseDouble(aPosition.substring(aPosition.indexOf(' ')+1, aPosition.indexOf(')')-1));
    	}
    	this.location = new GPSPosition(lat, lon);
    	return;
    }
}
