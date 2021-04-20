package org.unina.spatialanalysis.trajectoryassigner.assigner.defaultassigner;

import org.unina.spatialanalysis.trajectoryassigner.assigner.AbstractRouteAssignerFactory;
import org.unina.spatialanalysis.trajectoryassigner.assigner.RouteAssigner;
import org.unina.spatialanalysis.trajectoryassigner.entity.position.Position;

public class DefaultRouteAssignerFactory extends AbstractRouteAssignerFactory {
	
	private final int maxTime;
	private final boolean stretchOverMultipleDays;
	private final int minRecordings;
	
	public<T extends Position> RouteAssigner<T> getRouteAssigner(){
		return new DefaultRouteAssigner<T>(stretchOverMultipleDays, maxTime, minRecordings);
    }
	
	public DefaultRouteAssignerFactory(boolean stretchOverMultipleDays, int maxTime, int minRecordings) {
		this.maxTime = maxTime;
		this.stretchOverMultipleDays = stretchOverMultipleDays;	
		this.minRecordings = minRecordings;
	}
}
