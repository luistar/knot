package org.unina.spatialanalysis.mapmatcher.entity.position;

public class GPSPosition {
	
	private double lat;
	private double lon;
	
	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	
	
	public GPSPosition(double lat, double lon) {
		super();
		this.lat = lat;
		this.lon = lon;
	}
	
	
	private static  class HaversineAlgorithm {

	    static final double _eQuatorialEarthRadius = 6378.1370D;
	    static final double _d2r = (Math.PI / 180D);

	    public static int HaversineInM(double lat1, double long1, double lat2, double long2) {
	        return (int) (1000D * HaversineInKM(lat1, long1, lat2, long2));
	    }

	    public static double HaversineInKM(double lat1, double long1, double lat2, double long2) {
	        double dlong = (long2 - long1) * _d2r;
	        double dlat = (lat2 - lat1) * _d2r;
	        double a = Math.pow(Math.sin(dlat / 2D), 2D) + Math.cos(lat1 * _d2r) * Math.cos(lat2 * _d2r)
	                * Math.pow(Math.sin(dlong / 2D), 2D);
	        double c = 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a));
	        double d = _eQuatorialEarthRadius * c;

	        return d;
	    }

	}
	
	
	/**
	 * The method returns the distance in Kilometers between this GPSPosition and another, considering
	 * this as the point of Origin and the other as the Destination. The calculation is done using the 
	 * Haversine Formula. The distance is calculated as the crow flies, and as such is no indication
	 * of the real travel distance between the two points.
	 * @param o the other GPSPosition.
	 * @return the distance in Meters between this GPSPosition and the other.
	 */
	public double getDistanceInMeters(GPSPosition o) {
		return HaversineAlgorithm.HaversineInM(this.lat, this.lon, o.getLat(), o.getLon());
	}

	public String toString() {
		return lon + " " + lat; 	
	}
	
	public String asWKT() {
		return "POINT( " + lon +" " + lat +")";
	}
}
