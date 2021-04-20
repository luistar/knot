package org.unina.spatialanalysis.mapmatcher.logging;

import java.sql.Timestamp;

public class LogStringMaker {
	
	private static final String LOG_BEGIN_EXECUTION_WITH_SETTINGS = " :\n Starting execution with Settings \n";
	
	private static final String LOG_END_EXECUTION = " :\n Finished Execution \n";
	
	private static final String ERROR_STUB = " :\n \tERROR :\n ";
	
	public static String logCurrentExecutionSetting(String osmFile, String host, String pairType, boolean includeNeverVisited) {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		String res = now.toString() + LOG_BEGIN_EXECUTION_WITH_SETTINGS + 
				"{ Coordinate Format: "  + pairType +
				"\n Routing Service Host: " + host +
				"\n OSM Map File : " + osmFile +
				"\n Include Never Visited: " + includeNeverVisited + 
				"\n}";
		return res;
	}

	public static String logError(String error) {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		String res = now.toString() + ERROR_STUB + 
				"\t\t " + error;
		return res;
	}

	public static String logExecutionEnd(long currentRowCounter, long size, int missedForTooBig, int missedForNoMatch, int missedForIO) {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		String res = now.toString() + LOG_END_EXECUTION +
				"Read " + currentRowCounter + " of " + size +" input routes.\n" +
				"Total routes matched to segments: " + (currentRowCounter - missedForTooBig - missedForNoMatch - missedForIO) +".\n" +
				(missedForTooBig!=0?(missedForTooBig + " routes could not be found due to many trace coordinates! Can you increase the limit in the OSRM routing machine?\n"):"")+
				(missedForNoMatch!=0? (missedForNoMatch  + " routes could not be found at all!\n"):"") +
				(missedForIO!=0? (missedForIO + " routes could not be found due to IO errors.\n"):"");
		return res;
	}
}
