package org.unina.spatialanalysis.mapmatcher.entity.route;

import java.time.LocalDateTime;

public class RouteFactory {
	private final String MODE;
	
	
	public Route createRoute(String rowId, int ownerId, String theGeom, LocalDateTime beginAt) {
		switch(MODE) {
		case "{lon,lat}":
			return new Route(theGeom, beginAt, ownerId, rowId);
		case "{lat, lon}":{
			System.out.println(theGeom);
			String tmp = "LINESTRING(";
			theGeom = theGeom.replaceFirst("LINESTRING(", "");
			theGeom = theGeom.replaceFirst(")", "");
			for(String s : theGeom.split(",")) {
				String[] coords = s.split(" ");
				tmp +=(coords[1] + " " + coords[0]);
				tmp += ",";
			}
			tmp = tmp.substring(0, tmp.lastIndexOf(','));
			tmp += ")";
			System.out.println(tmp);
			return new Route(tmp, beginAt, ownerId, rowId);
		}
		default:
			return null;
		}
	}
	
	
	public RouteFactory (String mode) {
		MODE = mode;
		return;
	}
}
