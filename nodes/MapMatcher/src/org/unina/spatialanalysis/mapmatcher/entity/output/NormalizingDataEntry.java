package org.unina.spatialanalysis.mapmatcher.entity.output;

import java.util.Map;

import org.unina.spatialanalysis.mapmatcher.entity.osm.Node;

public class NormalizingDataEntry {
	private long originId;
	private long destinationId;
	private String tags;
	private String theGeom;
	/**
	 * @return the originId
	 */
	public long getOriginId() {
		return originId;
	}
	/**
	 * @return the destinationId
	 */
	public long getDestinationId() {
		return destinationId;
	}
	/**
	 * @return the tags
	 */
	public String getTags() {
		return tags;
	}
	/**
	 * @return the theGeom
	 */
	public String getTheGeom() {
		return theGeom;
	}
	
	public NormalizingDataEntry(Node origin, Node destination, Map<String,String> tagsMap) {
		this.tags = "{";
		for(String s: tagsMap.keySet()) {
			tags += (" " + s + " = " + tagsMap.get(s) +";");
		}
		if(this.tags.lastIndexOf(";")!=-1) {
			this.tags = this.tags.substring(0, this.tags.length()-1);
		}
		this.tags += " }";
		this.destinationId=destination.getNodeId();
		this.originId = origin.getNodeId();
		this.theGeom = "LINESTRING(" + origin.getLon() + " " +origin.getLat() + ", " + destination.getLon() + " " + destination.getLat() + ")";
	}
}
