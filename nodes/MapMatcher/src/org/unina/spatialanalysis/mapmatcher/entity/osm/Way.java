package org.unina.spatialanalysis.mapmatcher.entity.osm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Way {
	
	private long wayId;
	
	private Map<String, String> tags;

	/**
	 * @return the wayId
	 */
	public long getWayId() {
		return wayId;
	}

	/**
	 * @return the tags
	 */
	public Map<String, String> getTags() {
		return tags;
	}

	public Way(long wayId) {
		super();
		this.wayId = wayId;
		this.tags = new HashMap<String, String>();
	}
	
	public void addTags(String key, String value) throws IOException {
		this.tags.put(key, value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (wayId ^ (wayId >>> 32));
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
		Way other = (Way) obj;
		if (wayId != other.wayId)
			return false;
		return true;
	}
}
