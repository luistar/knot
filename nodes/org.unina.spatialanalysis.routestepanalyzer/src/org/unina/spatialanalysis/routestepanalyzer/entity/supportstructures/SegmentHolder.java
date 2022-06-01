package org.unina.spatialanalysis.routestepanalyzer.entity.supportstructures;

import java.util.Collection;
import java.util.HashMap;

import org.unina.spatialanalysis.routestepanalyzer.entity.routesteps.Segment;

public class SegmentHolder{
	
	private HashMap<String, Segment> segments;
	
	public void addSegment(Segment s) {
		if(segments.containsKey(s.getOriginId()+"_"+s.getDestinationId())) {
			return;
		}else {
			segments.put(s.getOriginId()+"_"+s.getDestinationId(), s);
			return;
		}
	}
	
	public boolean containsSegment(long origin, long destination) {
		return segments.containsKey(origin+"_"+destination);
	}
	
	public Segment getSegment(long origin, long destination) {
		return segments.get(origin+"_"+destination);
	}
	
	public Collection<Segment> getAllSegments(){
		return this.segments.values();	
	}
	
	public SegmentHolder() {
		super();
		this.segments = new HashMap<String, Segment>();
		return;
	}
}
