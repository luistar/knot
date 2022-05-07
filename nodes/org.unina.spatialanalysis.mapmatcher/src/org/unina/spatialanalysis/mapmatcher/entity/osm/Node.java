package org.unina.spatialanalysis.mapmatcher.entity.osm;

import java.util.HashSet;
import java.util.Set;

public class Node {
	
	private long nodeId;
	
	private double lon;
	private double lat;
	
	private Set<Long> ways;

	/**
	 * @return the nodeId
	 */
	public long getNodeId() {
		return nodeId;
	}

	/**
	 * @return the lon
	 */
	public double getLon() {
		return lon;
	}

	/**
	 * @return the lat
	 */
	public double getLat() {
		return lat;
	}

	/**
	 * @return the ways
	 */
	public Set<Long> getWays() {
		return ways;
	}

	public Node(long nodeId, double lon, double lat) {
		super();
		this.nodeId = nodeId;
		this.lon = lon;
		this.lat = lat;
		this.ways= new HashSet<Long>();
	}
	
	public boolean isInWay(Way w) {
		return this.ways.contains(w.getWayId());
	}
	
	public boolean addWay(Way w) {
		return this.ways.add(w.getWayId());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (nodeId ^ (nodeId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (nodeId != other.nodeId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Node [nodeId=" + nodeId + ", lon=" + lon + ", lat=" + lat + "]";
	}
	
	
	
}
