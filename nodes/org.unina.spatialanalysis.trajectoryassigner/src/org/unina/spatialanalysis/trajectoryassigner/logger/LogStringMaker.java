package org.unina.spatialanalysis.trajectoryassigner.logger;

import java.sql.Timestamp;

/**
 * A convenience class to generate Log messages.
 * @author sinog
 *
 */
public class LogStringMaker {
	
	private static final String LOG_BEGIN_EXECUTION_WITH_SETTINGS = " :\n Starting execution with Settings \n";
	
	private static final String LOG_END_EXECUTION = " :\n Finished Execution \n";
	
	private static final String ERROR_STUB = " :\n \tERROR :\n ";
	
	public static String logCurrentExecutionSetting(String pairType, boolean multipleDays, String assigner, int maxTimeBetweenRecords) {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		String res = now.toString() + LOG_BEGIN_EXECUTION_WITH_SETTINGS + 
				"{Coordinate Format: "  + pairType +
				"\nAssigner : " + assigner +
				"\nMultiple Days for Route: " + multipleDays +
				"\nMax Time Between Records: " + maxTimeBetweenRecords + " minutes" +
				"\n}";
		return res;
	}

	public static String logError(String error) {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		String res = now.toString() + ERROR_STUB + 
				"\t\t " + error;
		return res;
	}

	public static String logRoutesBeingCalculated(int i, int routeNumber) {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		String res = now.toString() + " : FCD Trajectory Restorator \n\t Calculating " +routeNumber + " routes for vehicle " + i +"\n";
				
		return res;
	}

	public static Object logExecutionEnd(long currentRowCounter, long size, int numberOfVehicles, int numberOfFoundRoutes,
			int numberOfMissedRoutes, int numberOfDiscardedRoutes, String mode) {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		String res = now.toString() + LOG_END_EXECUTION +
				"Read " + currentRowCounter + " of " + size +" input rows.\n" +
				"Found " + numberOfVehicles + " disinct vehicles , whose positions have been assigned to " + (numberOfFoundRoutes + numberOfMissedRoutes) +" routes.\n"+
				"Of those the Routing Service managed to find " + numberOfFoundRoutes + " routes and missed " + numberOfMissedRoutes  + " using " + mode +".\n"
				+ numberOfMissedRoutes + " routes were discarded as they did not last enough or were too short.";
		return res;
	}
}
