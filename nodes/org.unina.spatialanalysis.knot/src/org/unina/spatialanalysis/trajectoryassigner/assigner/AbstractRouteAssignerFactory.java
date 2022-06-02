package org.unina.spatialanalysis.trajectoryassigner.assigner;

import org.unina.spatialanalysis.trajectoryassigner.entity.position.Position;

public abstract class AbstractRouteAssignerFactory {

	public abstract <T extends Position> RouteAssigner<T> getRouteAssigner();
}
