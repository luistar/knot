package org.unina.spatialanalysis.mapmatcher.entity.osm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OsmDataManager {
	
	private final Map<Long,Node> nodes;
	
	private final Map<Long,Way> ways;
	
	private final Map<Long, Set<Long>> recovery;
	
	private final Map<String, Segment> normalize;
	
	public OsmDataManager() {
		super();
		this.nodes = new HashMap<Long, Node>();
		this.ways = new HashMap<Long, Way>();
		this.recovery = new HashMap<Long, Set<Long>>();
		this.normalize = new HashMap<String, Segment>(); 
	}
	
	public void addNode(Node n) {
		/*
		 * Check if the node id is present in the recovery map.
		 * If yes add the ways ids saved there to the map and
		 * remove the node id. 
		 */
		if(recovery.containsKey(n.getNodeId())) {
			for(Long wayId: recovery.get(n.getNodeId())) {
				if(ways.containsKey(wayId)) {
					n.addWay(ways.get(wayId));
				}
			}
			recovery.remove(n.getNodeId());
		}
		this.nodes.put(n.getNodeId(), n);
	}
	
	public void addWay(Way w, List<Long> composingNodes) {
		/*
		 * Check if the nodes of the way are present, 
		 * if yes add the way to the nodes, if not add the ids
		 * to the recovery map.
		 */
		
		Long prev = null;
		for(Long l: composingNodes) {
			if(prev==null) {
				prev = l;
			}else {
				this.normalize.put(prev+"_"+l, new Segment(prev, l));
				prev = l;
			}
			if(nodes.get(l) != null) {
				Node n = nodes.get(l);
				n.addWay(w);
			}else {
				if(recovery.containsKey(l)) {
					Set<Long> waysIds = recovery.get(l);
					waysIds.add(w.getWayId());
				}else {
					Set<Long> waysIds = new HashSet<Long>();
					waysIds.add(w.getWayId());
					recovery.put(l, waysIds);
				}
			}
		}
		
		
		this.ways.put(w.getWayId(), w);
	}
	
	public String printStats() {
		return "This manager contains "  + this.nodes.size() + " nodes and " + this.ways.size() + " ways\n"; 
	}
	
	public Map<String, String> getAllTagsOfSegment(Node origin, Node destination){
		Set<Long> originWays = origin.getWays();
		Set<Long> destinationWays = destination.getWays();
		Set<Long> tmp = new HashSet<Long>();
		tmp.addAll(originWays);
		tmp.retainAll(destinationWays);
		Map<String, String> res = new HashMap<>();
		for(Long l: tmp) {
			res.putAll(this.ways.get(l).getTags());
		}
		return res;
 	}
	
	public boolean checkNodePresence(long nodeId) {
		return this.nodes.containsKey(nodeId);
	}

	public Node getNode(long nodeId) {
		return this.nodes.get(nodeId);
	}
	
	public void tagSegmentAsVisited(long origin, long destination) {
		if(this.normalize.containsKey(origin+"_"+destination)) {
			this.normalize.get(origin+"_"+destination).setAsVisited();
		}
	}
	
	public boolean checkIfSegmentWasVisited(String segmentId) {
		if(this.normalize.containsKey(segmentId)) {
			return this.normalize.get(segmentId).visited;
		}else {
			/**
			 * If the map does not contain the segment but we have visited it
			 * it must mean that the map data we have does not associate it to
			 * a way, but the routing service does. 
			 */
			return true;
		}
	}
	
	public class Segment{
		private long origin;
		private long destination;
		private boolean visited;
		
		
		private Segment(long origin, long destination) {
			this.origin = origin;
			this.destination = destination;
			this.visited = false;
		}
		
		public void setAsVisited() {
			this.visited = true;
		}

		/**
		 * @return the origin
		 */
		public long getOrigin() {
			return origin;
		}

		/**
		 * @return the destination
		 */
		public long getDestination() {
			return destination;
		}
		
		
	}
	
	public Map<String, Segment> getSegments(){
		return this.normalize;
	}
}
