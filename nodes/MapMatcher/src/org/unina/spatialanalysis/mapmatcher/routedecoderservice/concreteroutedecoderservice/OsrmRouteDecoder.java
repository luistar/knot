package org.unina.spatialanalysis.mapmatcher.routedecoderservice.concreteroutedecoderservice;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.TreeSet;

import org.osgi.framework.ServiceException;
import org.unina.spatialanalysis.mapmatcher.entity.route.Route;
import org.unina.spatialanalysis.mapmatcher.entity.route.RouteStep;
import org.unina.spatialanalysis.mapmatcher.entity.route.RouteStepFactory;
import org.unina.spatialanalysis.mapmatcher.routedecoderservice.RouteDecoder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;



public class OsrmRouteDecoder<T extends RouteStep, S extends Route> implements RouteDecoder<T, S>{

    /**
     * An http request for the match service of project-osrm using a car.
     */
    public static String REQUEST_FOR_MATCHING_ROUTE = "/match/v1/driving/";

    public final String host;
    public OsrmRouteDecoder(String routingServiceHost) {
		this.host = routingServiceHost;
    	
	}

	/**
     * The methods invokes the match service of project-osrm on a set of coordinates and returns the most likely matching route
     * for the coordinates given.
     * @param positions A set of Positions ordered accordingly to the time of their recording.
     * @return the JSON object associated with the request.
	 * @throws ServiceException In case the routing service failed.
     * @throws IOException In case of errors when sending the request.
     * @throws InterruptedException In case of errors when handling the request.
     */
    
    public TreeSet<T> decodeRoute(S route) throws ServiceException, IOException {
    	TreeSet<T> result = new TreeSet<T>();
    	String uri = host + REQUEST_FOR_MATCHING_ROUTE;
    	String tmp;
    	tmp = decodeLinestring(route);
    	tmp += "?geometries=geojson";
    	tmp += "&annotations=true";
    	tmp +="&steps=true";
    	uri= uri+tmp;
    	JSONObject res = runRequest(uri);
        result = decodeJSONAnswer(res, route);
        return result;
    }
    
    private String decodeLinestring(S route) {
    	String tmp = route.getRouteAsGPSLinestring();
    	String res ="";
    	tmp  = tmp.replace("LINESTRING(", "");
    	tmp = tmp.replace(")", "");
    	for(String x: tmp.split(",")) {
    		res+=(x.trim().replace(' ', ','));
    		res+=";";
    	}
    	res = res.substring(0, res.lastIndexOf(';'));
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
				throw new ServiceException(requestCode);
			}
		}
	}
	
	public TreeSet<T> decodeJSONAnswer(JSONObject o, S route){
		RouteStepHolder res = new RouteStepHolder();
		LocalDateTime startTime = route.getRouteBeginsAt();
		String routeId = route.getRouteId();
		int ownerId = route.getOwnerId();
		long origin = 0;
		long destination = 0;
		RouteStepFactory<T> factory = new RouteStepFactory<T>();
		LocalDateTime nextTime;
		JSONArray m = o.getJSONArray("matchings");
		
		for(int i=0; i<m.length(); i++) {
			JSONArray legs = m.getJSONObject(i).getJSONArray("legs");
			for(int j = 0; j<legs.length(); j++) {
				JSONObject annotation = legs.getJSONObject(j);
				annotation = annotation.getJSONObject("annotation");
				JSONArray nodes = annotation.getJSONArray("nodes");
				JSONArray times = annotation.getJSONArray("duration");
				JSONArray distance = annotation.getJSONArray("distance");
				for(int k = 0; k<nodes.length()-1; k++) {
						origin =nodes.getLong(k);
						destination =nodes.getLong(k+1);
						nextTime = startTime.plusSeconds((long) Math.ceil(times.getDouble(k)));
						T rs = factory.createDefaultRouteStep(routeId, ownerId, origin, destination, times.getDouble(k), distance.getDouble(k), startTime, nextTime);
						res.add(rs);
						startTime = nextTime;
				}
			}
		}
		return res;
	}
	
	private class RouteStepHolder extends TreeSet<T>{
		
		T lastInserted;
		
		private RouteStepHolder() {
			super();
			lastInserted = null;
		}
		
		
		public boolean add(T toAdd) {
			if(lastInserted==null) {
				lastInserted = toAdd;
				return super.add(toAdd);
			}else {
				if(lastInserted.getOriginId()== toAdd.getOriginId() && lastInserted.getDestinationId()==toAdd.getDestinationId()) {
					lastInserted.setEndVisitTime(toAdd.getEndAt());
					return false;
				}else {
					lastInserted=toAdd;
					return super.add(toAdd);
				}
			}			
		}
	}
	
}