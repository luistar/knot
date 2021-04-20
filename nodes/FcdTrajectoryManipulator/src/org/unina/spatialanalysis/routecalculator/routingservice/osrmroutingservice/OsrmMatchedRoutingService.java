package org.unina.spatialanalysis.routecalculator.routingservice.osrmroutingservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.osgi.framework.ServiceException;
import org.unina.spatialanalysis.routecalculator.entity.position.GPSPosition;
import org.unina.spatialanalysis.routecalculator.entity.position.Position;
import org.unina.spatialanalysis.routecalculator.entity.route.Route;
import org.unina.spatialanalysis.routecalculator.entity.route.RouteFactory;
import org.unina.spatialanalysis.routecalculator.routingservice.RoutingService;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The OsrmMatchedRoutingService uses the ProjectOSRM routing service to calculate the most likely path
 * among the set of positions passed as the input.
 * @author sinog
 *
 * @param <T> The specific kind of position this routing service must operate on.
 * @param <S> The specific kind of route this routing service must return.
 * @deprecated left as a an example of alternate implementation
 */

public class OsrmMatchedRoutingService<T extends Position, S extends Route> implements RoutingService<T, S>{

    /**
     * An http request for the match service of project-osrm using a car.
     */
    public static String REQUEST_FOR_MATCHING_ROUTE = "/match/v1/driving/";

    public final String host;
    
    public OsrmMatchedRoutingService(String routingServiceHost) {
		this.host = routingServiceHost;
    	
	}

	/**
     * The methods invokes the match service of project-osrm on a set of coordinates and returns the most likely matching route
     * for the coordinates given.
     * @param positions A set of Positions ordered accordingly to the time of their recording.
     * @return the JSON object associated with the request.
     * @throws IOException In case of errors when sending the request.
     * @throws InterruptedException In case of errors when handling the request.
     */
    
    @Override
    public S  findRoute(TreeSet<T> positions) throws Exception {
        	S result = null;
        	try {
        		result = findSubRoute(positions);
        	}catch(ServiceException e) {
        		System.out.println(e.getMessage());
        	}catch(IOException e) {
        		System.out.println(e.getMessage());
        	}
        return result;
    }

  
    /**
     * @deprecated
     * @param positions
     * @return
     */
    public Map<Integer, TreeSet<T>> divideRoute(TreeSet<T> positions){
        Map<Integer, TreeSet<T>> result = new HashMap<>();
        int i = 0, j = 0;
        TreeSet<T> currentSet = new TreeSet<T>((T t1, T t2)->{
            if(t1.getTimeOfRecord().after(t2.getTimeOfRecord())){
                return 1;
            }else if (t1.getTimeOfRecord().before(t2.getTimeOfRecord())){
                return -1;
            }else{
                return 0;
            }
        });
        TreeSet<T> buffer = new TreeSet<T>((T t1, T t2)->{
            if(t1.getTimeOfRecord().after(t2.getTimeOfRecord())){
                return 1;
            }else if (t1.getTimeOfRecord().before(t2.getTimeOfRecord())){
                return -1;
            }else{
                return 0;
            }
        });
        for(T position: positions){
            if(i<60){
                if(!buffer.isEmpty()){
                    for(T bufferized: buffer){
                        currentSet.add(bufferized);
                        i++;
                    }
                    buffer.clear();
                }
                currentSet.add(position);
                if(i>=50){
                    buffer.add(position);
                }
                i++; 
            }else{
                result.put(j++, currentSet);
                currentSet = new TreeSet<T>((T t1, T t2)->{
                    if(t1.getTimeOfRecord().after(t2.getTimeOfRecord())){
                        return 1;
                    }else if (t1.getTimeOfRecord().before(t2.getTimeOfRecord())){
                        return -1;
                    }else{
                        return 0;
                    }
                });
                currentSet.add(position);
                i=0;
            }
        }
        result.put(j++, currentSet);
        return result;
    }

    public S findSubRoute(TreeSet<T> positions) throws IOException, InterruptedException {
        String uri = host + REQUEST_FOR_MATCHING_ROUTE;
        S  res;
        for (T p : positions) {
            uri += (p.getLocation().getLon() + "," + p.getLocation().getLat() + ";");
        }
        uri = uri.substring(0, uri.length() - 1);
        uri += "?geometries=geojson";
        uri+= "&overview=full";
        uri+="&tidy=true";
        uri+="&gaps=ignore";
        JSONObject jsonObject = runRequest(uri);
        res = matchingJsonDecoder(jsonObject, positions.first(), positions.last());
        return res;
    }

    private S matchingJsonDecoder(JSONObject jsonObject,T start, T end) throws ServiceException{
        JSONArray matchings = jsonObject.getJSONArray("matchings");
        ArrayList<GPSPosition> positions = new ArrayList<GPSPosition>();
        double time = 0;
        double distance = 0;
		S res  = null;
    	JSONObject o = matchings.getJSONObject(0);
		JSONArray coordinates = o.getJSONObject("geometry").getJSONArray("coordinates");
		for(int j=0; j<coordinates.length(); j++ ) {
			JSONArray loc = coordinates.getJSONArray(j);
			positions.add(new GPSPosition(loc.getDouble(1), loc.getDouble(0)));
		}
		time += o.getDouble("duration");
		distance += o.getDouble("distance");
		res =  RouteFactory.createRoute(positions, distance, time, start, end);
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

}