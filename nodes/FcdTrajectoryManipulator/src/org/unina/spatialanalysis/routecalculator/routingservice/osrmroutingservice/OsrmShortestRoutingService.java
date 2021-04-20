package org.unina.spatialanalysis.routecalculator.routingservice.osrmroutingservice;

import org.unina.spatialanalysis.routecalculator.entity.route.Route;
import org.unina.spatialanalysis.routecalculator.entity.route.RouteFactory;
import org.unina.spatialanalysis.routecalculator.routingservice.RoutingService;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.ServiceException;
import org.unina.spatialanalysis.routecalculator.entity.position.GPSPosition;
import org.unina.spatialanalysis.routecalculator.entity.position.Position;

/**
 * The OsrmShortestRoutingService uses the ProjectOSRM routing service to calculate the shortest path
 * between the starting position and the final position of a route.
 * @author sinog
 *
 * @param <T> The specific kind of position this routing service must operate on.
 * @param <S> The specific kinf of route this routing service must return.
 */
public class OsrmShortestRoutingService<T extends Position, S extends Route> implements RoutingService<T, S> {
	

    /**
     * An http request for the route service of project-osrm using a car.
     */
    public static String REQUEST_FOR_SHORTEST_ROUTE = "/route/v1/driving/";

    public final String host;
    
    public OsrmShortestRoutingService(String routingServiceHost) {
		this.host = routingServiceHost;
    	
	}
    
    @Override
    public S findRoute(TreeSet<T> pos) throws IOException, InterruptedException {
        String uri = host + REQUEST_FOR_SHORTEST_ROUTE;
        T startingPosition = pos.first();
        T finalPosition = pos.last();
        uri += (startingPosition.getLocation().getLon() + "," + startingPosition.getLocation().getLat() + ";");
        uri += (finalPosition.getLocation().getLon() + "," + finalPosition.getLocation().getLat());
        uri +=("?geometries=geojson");
        uri +=("&overview=full");
        uri +=("&steps=true");
        try {
        	JSONObject jsonObject = runRequest(uri);
        	return routingJsonDecoder(jsonObject, startingPosition, finalPosition);
        }catch(ServiceException e) {
        	System.out.println(e.getMessage());
        	return null;
        }
    }
    
    @SuppressWarnings("unchecked")
	private S routingJsonDecoder(JSONObject jsonObject,T start, T end) {
        S res = null;
        JSONArray routes = jsonObject.getJSONArray("routes");
        Object o = routes.get(0);
        if(o instanceof JSONObject) {
        	JSONObject routeObject = (JSONObject) o ;
	    	JSONArray coordinates = routeObject.getJSONObject("geometry").getJSONArray("coordinates");
	    	res = (S) RouteFactory.createRoute(decodeCoordinatesArray(coordinates), routeObject.getDouble("distance"), routeObject.getDouble("duration"), start, end); 
        }
        return res;
    }
    

	public JSONObject runRequest(String url) throws IOException, ServiceException {
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(url).build();
		try(Response response = client.newCall(request).execute()){
			JSONObject res = new JSONObject(response.body().string());
			String requestCode = res.getString("code");
			if(requestCode.equals("Ok")) {
				return res;
			}else {
				throw new ServiceException(res.getString("message"));
			}
		}
	}
	
	private ArrayList<GPSPosition> decodeCoordinatesArray(JSONArray coordinates) {
        GPSPosition tmp;
        ArrayList<GPSPosition> res = new ArrayList<GPSPosition>();
        for(int i = 0; i< coordinates.length(); i++){
            JSONArray internalArray = (JSONArray) coordinates.get(i);
            tmp = new GPSPosition(internalArray.getDouble(1), internalArray.getDouble(0));
            res.add(tmp);
        }
        return res;
    }
}
