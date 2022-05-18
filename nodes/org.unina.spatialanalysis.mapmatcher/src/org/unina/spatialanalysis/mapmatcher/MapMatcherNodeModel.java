package org.unina.spatialanalysis.mapmatcher;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.osgi.framework.ServiceException;
import org.unina.spatialanalysis.mapmatcher.entity.osm.OsmDataManager;
import org.unina.spatialanalysis.mapmatcher.entity.output.NormalizingDataEntry;
import org.unina.spatialanalysis.mapmatcher.entity.output.RouteStepVisited;
import org.unina.spatialanalysis.mapmatcher.entity.route.Route;
import org.unina.spatialanalysis.mapmatcher.entity.route.RouteFactory;
import org.unina.spatialanalysis.mapmatcher.entity.route.RouteStep;
import org.unina.spatialanalysis.mapmatcher.entity.osm.Node;
import org.unina.spatialanalysis.mapmatcher.logging.LogStringMaker;
import org.unina.spatialanalysis.mapmatcher.osmfilereader.OsmFileReader;
import org.unina.spatialanalysis.mapmatcher.routedecoderservice.RouteDecoder;
import org.unina.spatialanalysis.mapmatcher.routedecoderservice.RouteDecoderFactory;




/**
 * This is an example implementation of the node model of the
 * "MapMatcher" node.
 * 
 * This example node performs simple number formatting
 * ({@link String#format(String, Object...)}) using a user defined format string
 * on all double columns of its input table.
 *
 * @author Sinogrante Principe
 */
@SuppressWarnings("deprecation")
public class MapMatcherNodeModel extends NodeModel {
	
	private static final NodeLogger LOGGER = NodeLogger.getLogger(MapMatcherNodeModel.class);
	
	//
	
	private static final String ID_COL = "m_col_id";

	private static final String BEGIN_AT_COL = "m_col_beginat";

	private static final String GEOMETRY_COL = "m_col_geometry";	
	
	//
	
	private static final String ROUTE_DECODER_SERVICE = "m_route_decoder_service";
	
	private static final String ROUTE_DECODER_SERVICE_DEFAULT = "OSRM";
	
	private static final String PAIR_TYPE_FORMAT = "m_pair_type";

	private static final String DEFAULT_PAIR_TYPE_FORMAT = "{lon,lat}";
	
	private static final String OSM_DATA_PATH = "osm_path";
	
	private static final String DEFAULT_PATH = "";
	
	private static final String ROUTE_DECODER_HOST = "m_route_decoder_host";
	
	private static final String DEFAULT_ROUTE_DECODER_HOST = "http://127.0.0.1:5000";
	
	private static final String INCLUDE_NEVER_VISITED = "m_include_never_visited";
	
	private static final boolean DEFAULT_INCLUDE_NEVER_VISITED = false;
	
	// 
	
	private final SettingsModelColumnName m_colIDSettings = createColIDSettings();

	private final SettingsModelColumnName m_colBeginAtSettings = createColBeginAtSettings();

	private final SettingsModelColumnName m_colGeometrySettings = createColGeometrySettings();

	/**
	 * The settings model to manage the type of coordinate pairs provided to the node,
	 * either {lon,lat} or {lat,lon}.
	 **/
	private final SettingsModelString m_pairTypeSettings = createCoordinatePairTypeSettings();
	
	private final SettingsModelString m_osmDataPath = createOsmDataPath();
	
	/**
	 * The settings model to manage the routing service, it contains the information required
	 * to access the routing service server.
	 */
	private final SettingsModelString m_routeDecoderHostSettings = createRouteDecoderHostSettings();
	
	private final SettingsModelBoolean m_includeNeverVisited = createIncludeNeverVisitedSetting();
	
	private final SettingsModelString m_routeDecoderService = createRouteDecoderServiceSettings();
	
	/**
	 * Constructor for the node model.
	 */
	protected MapMatcherNodeModel() {
		/**
		 * Here we specify how many data input and output tables the node should have.
		 * In this case its one input and one output table.
		 */
		super(1, 1);
	}

	/**
	 * A convenience method to create a new settings model used for the number
	 * format String. This method will also be used in the {@link RouteStepAnalyzerNodeDialog}. 
	 * The settings model will sync via the above defined key.
	 * 
	 * @return a new SettingsModelString with the key for the number format String
	 */
	static SettingsModelString createOsmDataPath() {
		return new SettingsModelString(OSM_DATA_PATH, DEFAULT_PATH);
	}
	
	
	static SettingsModelColumnName createColIDSettings()  {
		SettingsModelColumnName idColSettingModel = new SettingsModelColumnName(ID_COL, null);
		idColSettingModel.setEnabled(true);
		return idColSettingModel;
	}
	
	static SettingsModelColumnName createColBeginAtSettings()  {
		SettingsModelColumnName beginAtColSettingModel = new SettingsModelColumnName(BEGIN_AT_COL, null);
		beginAtColSettingModel.setEnabled(true);
		return beginAtColSettingModel;
	}
	
	static SettingsModelColumnName createColGeometrySettings()  {
		SettingsModelColumnName geometryColSettingModel = new SettingsModelColumnName(GEOMETRY_COL, null);
		geometryColSettingModel.setEnabled(true);
		return geometryColSettingModel;
	}
	
	static SettingsModelString createRouteDecoderServiceSettings(){
		return new SettingsModelString(ROUTE_DECODER_SERVICE, ROUTE_DECODER_SERVICE_DEFAULT);
	}
	
	static SettingsModelString createRouteDecoderHostSettings() {
		SettingsModelString routeDecoderHost = new SettingsModelString(ROUTE_DECODER_HOST, DEFAULT_ROUTE_DECODER_HOST);
		routeDecoderHost.setEnabled(true);
		return routeDecoderHost;
	}
	
	static SettingsModelString createCoordinatePairTypeSettings() {
		SettingsModelString pairType = new SettingsModelString(PAIR_TYPE_FORMAT, DEFAULT_PAIR_TYPE_FORMAT);
		pairType.setEnabled(true);
		return pairType;
	}
	
	static SettingsModelBoolean createIncludeNeverVisitedSetting() {
		return new SettingsModelBoolean(INCLUDE_NEVER_VISITED, DEFAULT_INCLUDE_NEVER_VISITED);
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		
		LOGGER.info(LogStringMaker.logCurrentExecutionSetting(m_osmDataPath.getStringValue(), m_routeDecoderHostSettings.getStringValue(), m_pairTypeSettings.getStringValue(), m_includeNeverVisited.getBooleanValue()));
		BufferedDataTable inputTable = inData[0];
		
		/*
		 * Create the output specifications for the table containing RouteStep data.
		 */
		DataTableSpec routeStepsSpecs = createOutputForRouteSteps();
		BufferedDataContainer routeStepsContainer = exec.createDataContainer(routeStepsSpecs);
								
		OsmDataManager mapData = null;
		exec.setMessage("Parsing Map Data...");
		File osmFile = new File(m_osmDataPath.getStringValue());
		OsmFileReader myFileReader = new OsmFileReader(osmFile);
		try {
			mapData = myFileReader.readOsmFile();
		}catch(IOException e) {

		}
		if(mapData==null) {
			return null;
		}
		exec.setMessage("Parsing Map Data finished!");

		/*
		 * Getting the index of the relevant columns (their presence is ensured by the configure method).
		 */
		int idIndex = -1;
		int beginAtIndex = -1;
		int theGeomIndex =-1;
		
		String colIDName = m_colIDSettings.getColumnName();
		String colBeginAtName = m_colBeginAtSettings.getColumnName();
		String colGeometryName = m_colGeometrySettings.getColumnName();	
		
		DataTableSpec specs = inputTable.getDataTableSpec();
		for(int i = 0; i<specs.getNumColumns(); i++) {
			DataColumnSpec columnspec = specs.getColumnSpec(i);
			if(columnspec.getName().equals(colIDName)) {
				idIndex = i;
			}else if(columnspec.getName().equals(colBeginAtName)) {
				beginAtIndex = i;
			}else if(columnspec.getName().equals(colGeometryName)) {
				theGeomIndex = i;
			}
		}
		
		RouteDecoder<RouteStep, Route> decoder = RouteDecoderFactory.getRouteDecoder("osrm",m_routeDecoderHostSettings.getStringValue());
		RouteFactory routeFactory = new RouteFactory(m_pairTypeSettings.getStringValue());
		int routeStepsCounter = 0;
		
		exec.setProgress(0, "Processing rows...");
		
		int routesMissedForTooManyTraceCoordinates = 0;
		int routesMissedForMatchingTraceError = 0;
		int routesMissedForIOError = 0;
		
		CloseableRowIterator rowIterator = inputTable.iterator();

		int currentRowCounter = 0;

		while(rowIterator.hasNext()) {
			DataRow currentRow = rowIterator.next(); 
			currentRowCounter++;
			int ownerId = -1;
			String theGeom = null;
			LocalDateTime beginAt = null;
			
			DataCell cell = currentRow.getCell(idIndex);
			if (cell.getType().getCellClass().equals((IntCell.class))) {
				IntCell intCell = (IntCell) cell;
				ownerId = intCell.getIntValue();
			}
			cell = currentRow.getCell(beginAtIndex);
			if(cell.getType().getCellClass().equals(DateAndTimeCell.class)) {
				DateAndTimeCell dateAndTimeCell = (DateAndTimeCell) cell;
				beginAt = LocalDateTime.parse(dateAndTimeCell.getStringValue());
			}
			cell = currentRow.getCell(theGeomIndex);
			if(cell.getType().getCellClass().equals(StringCell.class)) {
				StringCell stringCell = (StringCell) cell;
				theGeom = stringCell.getStringValue();
			}
			
			if(ownerId == -1 || theGeom == null || beginAt == null) {
				LOGGER.error(LogStringMaker.logError("Row " + currentRow.getKey() + " has invalid values! It will be skipped.\n"));
			}else {
				Route r = routeFactory.createRoute(currentRow.getKey().getString(), ownerId, theGeom, beginAt);
				try {
					TreeSet<RouteStep> steps = decoder.decodeRoute(r);
					for(RouteStep rs: steps) {
						if(mapData.checkNodePresence(rs.getOriginId()) && mapData.checkNodePresence(rs.getDestinationId())) {
							Node origin = mapData.getNode(rs.getOriginId());
							Node destination = mapData.getNode(rs.getDestinationId());
							if(origin != null && destination != null) {
								mapData.tagSegmentAsVisited(origin.getNodeId(), destination.getNodeId());	
								addRouteStepDataEntry(new RouteStepVisited(rs.getId(), rs.getRouteId(), rs.getBeginAt(), rs.getEndAt(), origin, destination, mapData.getAllTagsOfSegment(origin, destination) ), routeStepsCounter++, routeStepsContainer);
							}
						}
					}
				}catch(ServiceException e) {
					switch(e.getMessage()) {
						case("NoMatch"):{
							routesMissedForMatchingTraceError++;
							LOGGER.error(LogStringMaker.logError("Could not match the trace of route " + currentRow.getKey()));
						}
						case("TooBig"):{
							routesMissedForTooManyTraceCoordinates++;
							LOGGER.error(LogStringMaker.logError("Too many trace coordinates for " + currentRow.getKey() +".\n Can you increase the number of supported trace coordinates?"));
						}	
					}
				}catch(IOException e) {
					routesMissedForIOError++;
					LOGGER.error(LogStringMaker.logError("An IO error occured for " + currentRow.getKey() +".\n" + e.getStackTrace()));
				}
			}
			exec.setProgress(currentRowCounter/(double)inputTable.size(), "Processing row " + currentRowCounter + " of " + inputTable.size());
		}
		
		if(m_includeNeverVisited.getBooleanValue()) {
			exec.setProgress(0.0,"Normalizing Result...");
			int n = 0;
			int counter = 1;
			int normalizingSize = mapData.getSegments().size();
			for(String s: mapData.getSegments().keySet()) {
				if(!mapData.checkIfSegmentWasVisited(s)) {
					Node origin = mapData.getNode(mapData.getSegments().get(s).getOrigin());
					Node destination = mapData.getNode(mapData.getSegments().get(s).getDestination());
					if(origin != null && destination != null) {
						NormalizingDataEntry nEntry = new NormalizingDataEntry(origin, destination, mapData.getAllTagsOfSegment(origin, destination));
						addNormalizingDataEntry(nEntry, n++, routeStepsContainer);
					}
				}
				exec.setProgress(counter++/(double) normalizingSize,"Normalizing Result...");
			}
		}
		
		
		
		routeStepsContainer.close();
		BufferedDataTable out1 = routeStepsContainer.getTable();
		LOGGER.info(LogStringMaker.logExecutionEnd(currentRowCounter, inputTable.size(), routesMissedForTooManyTraceCoordinates, routesMissedForMatchingTraceError, routesMissedForIOError));
		return new BufferedDataTable[] { out1 };
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		 
		
		String routeDecoderService = m_routeDecoderService.getStringValue();
		
		switch(routeDecoderService) {
			case("OSRM"):
				validateOSRMSettings();
				break;
			default:
				throw new InvalidSettingsException("The selected Route Decoder Strategy does not exist!");
		}
		
		
		String path = m_osmDataPath.getStringValue();
		File f = new File(path);
		
		if(!f.exists()){
			throw new InvalidSettingsException("The entered input file does not exist!");
		}
		
		if(!f.canRead()) {
			throw new InvalidSettingsException("Don't have the required permissions for reading the input file!");
		}
		
		String colIDName = m_colIDSettings.getColumnName();
		String colBeginAtName = m_colBeginAtSettings.getColumnName();
		String colGeometryName = m_colGeometrySettings.getColumnName();				
		
		if((colIDName == null || colBeginAtName == null || colGeometryName == null)) {
			LOGGER.info(LogStringMaker.logError("All columns must be selected in the configuration dialog"));
			throw new InvalidSettingsException("All columns must be selected in the configuration dialog");
		}
		
		if(!(inSpecs[0].containsName(colIDName) && inSpecs[0].containsName(colBeginAtName) && inSpecs[0].containsName(colGeometryName))) {
			if(!(inSpecs[0].getColumnSpec(colIDName).getType().getCellClass().equals(IntCell.TYPE) &&
					inSpecs[0].getColumnSpec(colGeometryName).getType().getCellClass().equals(StringCell.TYPE) && 
							inSpecs[0].getColumnSpec(colBeginAtName).getType().getCellClass().equals(DateAndTimeCell.TYPE))) {
				throw new InvalidSettingsException("The input columns must have the following type: id-Integer, begin_at-DateTime, the_geom-String");
			}
			throw new InvalidSettingsException("The input table must contain the following columns: id, begin_at, the_geom");
		}
		
		return new DataTableSpec[] { createOutputForRouteSteps() };
	}

	private void validateOSRMSettings() throws InvalidSettingsException {
		String format = m_pairTypeSettings.getStringValue();
		if(!format.equals("{lat,lon}") && !format.equals("{lon,lat}")) {
			throw new InvalidSettingsException("The entered format is not a valid coordinate pair type!");
		}
		
		/*
		 * Check if the OSRM server host inserted is a valid URL and if it available.
		 */
		String osrmHost = m_routeDecoderHostSettings.getStringValue();
		osrmHost = osrmHost.toLowerCase();
		try {
		    URL myURL = new URL(osrmHost);
		    URLConnection myURLConnection = myURL.openConnection();
		    myURLConnection.connect();
		} 
		catch (MalformedURLException e) {
			throw new InvalidSettingsException("The entered host is not a valid URL!");
		} 
		catch (IOException e) {   
			throw new InvalidSettingsException("The entered host could not be reached!");
		}
		
	}

	private DataTableSpec createOutputForRouteSteps() {
		List<DataColumnSpec> newColumnSpecs = new ArrayList<>();
		DataColumnSpecCreator specCreator = new DataColumnSpecCreator("owner_id", IntCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("route_id", StringCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("begin_at", DateAndTimeCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("end_at", DateAndTimeCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());	
		specCreator = new DataColumnSpecCreator("origin_id", LongCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());	
		specCreator = new DataColumnSpecCreator("destination_id", LongCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());	
		specCreator = new DataColumnSpecCreator("tags", StringCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());	
		HashMap<String, String> m = new HashMap<String, String>();
		m.put("crs code", "EPSG:4326");
		m.put("crs WKT", 
				"GEOGCS[\"GCS_WGS_1984\",\r\n"
				+ "	DATUM[\"D_WGS_1984\",\r\n"
				+ "		SPHEROID[\"WGS_1984\",6378137,298.257223563]],\r\n"
				+ "	PRIMEM[\"Greenwich\",0.0],\r\n"
				+ "   	UNIT[\"degree\",0.0174532925199433],\r\n"
				+ "		AXIS[\"Longitude\", EAST ],\r\n"
				+ "		AXIS[\"Latitude\", NORTH ]]");
		DataColumnProperties properties = new DataColumnProperties(m);
		specCreator = new DataColumnSpecCreator("the_geom", StringCell.TYPE);
		specCreator.setProperties(properties);
		newColumnSpecs.add(specCreator.createSpec());
		DataColumnSpec[]  newColumnSpecsArray = newColumnSpecs.toArray(new DataColumnSpec[newColumnSpecs.size()]);
		return new DataTableSpec(newColumnSpecsArray);
	}
	
	private void addRouteStepDataEntry(RouteStepVisited r, int currentRowCounter, BufferedDataContainer container) {
				
		List<DataCell> cells = new ArrayList<>();
		
		DataCell idCell = new IntCell(r.getOwnerId());
		cells.add(idCell);
		
		DataCell routeId = new StringCell(r.getRouteId());
		cells.add(routeId);
		
		
		DataCell beginAtCell = null;
		try {
			beginAtCell = DateAndTimeCell.fromString(r.getBeginAt().format(DateTimeFormatter.ISO_DATE_TIME));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		cells.add(beginAtCell);
		
		DataCell endAtCell = null;
		try {
			endAtCell = DateAndTimeCell.fromString(r.getEndAt().format(DateTimeFormatter.ISO_DATE_TIME));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		cells.add(endAtCell);
		
		DataCell originId = new LongCell(r.getOriginId());
		cells.add(originId);
		
		DataCell destinationId = new LongCell(r.getDestinationId());
		cells.add(destinationId);

		DataCell tags = new StringCell(r.getTags());
		cells.add(tags);
		
	
		DataCell theGeom = new StringCell(r.getTheGeom());
		cells.add(theGeom);
		DataRow row = new DefaultRow("N " + currentRowCounter,cells);
		try {
			container.addRowToTable(row);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}

	return;		
	}
	
	
	private void addNormalizingDataEntry(NormalizingDataEntry nEntry, int currentRowCounter, BufferedDataContainer container) {
				
		List<DataCell> cells = new ArrayList<>();
		
		DataCell idCell = DataType.getMissingCell();
		cells.add(idCell);
		
		DataCell routeId = DataType.getMissingCell();
		cells.add(routeId);
		
		
		DataCell beginAtCell = DataType.getMissingCell();
		cells.add(beginAtCell);
		
		DataCell endAtCell = DataType.getMissingCell();
		cells.add(endAtCell);
		
		DataCell originId = new LongCell(nEntry.getOriginId());
		cells.add(originId);
		
		DataCell destinationId = new LongCell(nEntry.getDestinationId());
		cells.add(destinationId);

		DataCell tags = new StringCell(nEntry.getTags());
		cells.add(tags);
		
	
		DataCell theGeom = new StringCell(nEntry.getTheGeom());
		cells.add(theGeom);
		DataRow row = new DefaultRow("NEVER_VISITED " + currentRowCounter,cells);
		try {
			container.addRowToTable(row);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}

	return;		
	}
	
	
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		/*
		 * Save user settings to the NodeSettings object. SettingsModels already know how to
		 * save them self to a NodeSettings object by calling the below method. In general,
		 * the NodeSettings object is just a key-value store and has methods to write
		 * all common data types. Hence, you can easily write your settings manually.
		 * See the methods of the NodeSettingsWO.
		 */
		m_colIDSettings.saveSettingsTo(settings);
		m_colBeginAtSettings.saveSettingsTo(settings);
		m_colGeometrySettings.saveSettingsTo(settings);
		
		m_osmDataPath.saveSettingsTo(settings);
		m_routeDecoderHostSettings.saveSettingsTo(settings);
		m_pairTypeSettings.saveSettingsTo(settings);
		m_includeNeverVisited.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		/*
		 * Load (valid) settings from the NodeSettings object. It can be safely assumed that
		 * the settings are validated by the method below.
		 * 
		 * The SettingsModel will handle the loading. After this call, the current value
		 * (from the view) can be retrieved from the settings model.
		 */
		m_colIDSettings.loadSettingsFrom(settings);
		m_colBeginAtSettings.loadSettingsFrom(settings);
		m_colGeometrySettings.loadSettingsFrom(settings);
		
		m_osmDataPath.loadSettingsFrom(settings);
		m_routeDecoderHostSettings.loadSettingsFrom(settings);
		m_pairTypeSettings.loadSettingsFrom(settings);
		m_includeNeverVisited.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		/*
		 * Check if the settings could be applied to our model e.g. if the user provided
		 * format String is empty. In this case we do not need to check as this is
		 * already handled in the dialog. Do not actually set any values of any member
		 * variables.
		 */
		m_colIDSettings.validateSettings(settings);
		m_colBeginAtSettings.validateSettings(settings);
		m_colGeometrySettings.validateSettings(settings);
				
		m_osmDataPath.validateSettings(settings);
		m_routeDecoderHostSettings.validateSettings(settings);
		m_pairTypeSettings.validateSettings(settings);
		m_includeNeverVisited.validateSettings(settings);
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		/*
		 * Advanced method, usually left empty. Everything that is
		 * handed to the output ports is loaded automatically (data returned by the execute
		 * method, models loaded in loadModelContent, and user settings set through
		 * loadSettingsFrom - is all taken care of). Only load the internals
		 * that need to be restored (e.g. data used by the views).
		 */
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		/*
		 * Advanced method, usually left empty. Everything
		 * written to the output ports is saved automatically (data returned by the execute
		 * method, models saved in the saveModelContent, and user settings saved through
		 * saveSettingsTo - is all taken care of). Save only the internals
		 * that need to be preserved (e.g. data used by the views).
		 */
	}

	@Override
	protected void reset() {
		/*
		 * Code executed on a reset of the node. Models built during execute are cleared
		 * and the data handled in loadInternals/saveInternals will be erased.
		 */
		}
}

