package org.unina.spatialanalysis.mapmatcher.routedecoderservice;

import org.unina.spatialanalysis.mapmatcher.entity.route.Route;
import org.unina.spatialanalysis.mapmatcher.entity.route.RouteStep;
import org.unina.spatialanalysis.mapmatcher.routedecoderservice.concreteroutedecoderservice.OsrmRouteDecoder;

public class RouteDecoderFactory {
	public static <T extends RouteStep, S extends Route> RouteDecoder<T, S> getRouteDecoder(String type, String host){
		switch(type) {
			case "osrm": 
				return new OsrmRouteDecoder<T,S>(host);
			default:
				return null;
		}
	}
}
