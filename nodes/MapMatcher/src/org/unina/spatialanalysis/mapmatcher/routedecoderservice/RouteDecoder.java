package org.unina.spatialanalysis.mapmatcher.routedecoderservice;

import java.io.IOException;
import java.util.TreeSet;

import org.unina.spatialanalysis.mapmatcher.entity.route.RouteStep;
import org.osgi.framework.ServiceException;
import org.unina.spatialanalysis.mapmatcher.entity.route.Route;

public interface RouteDecoder<T extends RouteStep, S extends Route> {
	
	public TreeSet<T> decodeRoute(S s) throws ServiceException, IOException;
}
