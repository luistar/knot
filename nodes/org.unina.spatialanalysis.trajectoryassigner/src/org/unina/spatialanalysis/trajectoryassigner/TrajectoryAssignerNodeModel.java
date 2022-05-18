package org.unina.spatialanalysis.trajectoryassigner;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.unina.spatialanalysis.trajectoryassigner.assigner.AbstractRouteAssignerFactory;
import org.unina.spatialanalysis.trajectoryassigner.assigner.RouteAssigner;
import org.unina.spatialanalysis.trajectoryassigner.assigner.defaultassigner.DefaultRouteAssigner;
import org.unina.spatialanalysis.trajectoryassigner.assigner.defaultassigner.DefaultRouteAssignerFactory;
import org.unina.spatialanalysis.trajectoryassigner.entity.position.GPSPosition;
import org.unina.spatialanalysis.trajectoryassigner.entity.position.Position;
import org.unina.spatialanalysis.trajectoryassigner.entity.position.PositionFactory;
import org.unina.spatialanalysis.trajectoryassigner.entity.route.Route;
import org.unina.spatialanalysis.trajectoryassigner.logger.LogStringMaker;

/**
 * This is an example implementation of the node model of the
 * "TrajectoryAssigner" node.
 * 
 * This example node performs simple number formatting
 * ({@link String#format(String, Object...)}) using a user defined format string
 * on all double columns of its input table.
 *
 * @author Sinogrante Principe
 */
public class TrajectoryAssignerNodeModel extends NodeModel {

	private static final NodeLogger LOGGER = NodeLogger.getLogger(TrajectoryAssignerNodeModel.class);

	private static final String ID_COL = "m_col_id";

	private static final String TIMESTAMP_COL = "m_col_timestamp";

	private static final String GEOMETRY_COL = "m_col_geometry";	
	
	private static final String ROUTE_ASSIGNER = "m_route_assigner";

	private static final String DEFAULT_ROUTE_ASSIGNER = "default";

	private static final String PAIR_TYPE_FORMAT = "m_pair_type";

	private static final String DEFAULT_PAIR_TYPE_FORMAT = "{lat,lon}";

	private static final String ALLOW_SPAWN_MULTIPLE_DAYS = "m_day_spawn_mode";

	private static final boolean DEFAULT_SPAWN_MULTIPLE_DAYS = true;

	private static final String MAXIMUM_TIME_BETWEEN_RECORDS = "m_max_time";

	private static final int DEFAULT_MAXIMUM_TIME_BETWEEN_RECORDS = 3;

	private static final String MINIMUM_ROUTE_RECORDINGS = "m_minimum_recordings";

	private static final int DEFAULT_MINIMUM_RECORDINGS =15;

	
	private final SettingsModelColumnName m_colIDSettings = createColIDSettings();

	private final SettingsModelColumnName m_colTimestampSettings = createColTimestampSettings();

	private final SettingsModelColumnName m_colGeometrySettings = createColGeometrySettings();

	
	/**
	 * The settings model to manage how positions are assigned to routes.
	 */
	private final SettingsModelString m_routeAssignerSettings = createRouteAssignerSettings();

	/**
	 * The settings model to manage the type of coordinate pairs provided to the node,
	 * either {lon,lat} or {lat,lon}.
	 **/
	private final SettingsModelString m_pairTypeSettings = createCoordinatePairTypeSettings();

	/**
	 * The settings model to inform the node if routes should be allowed to spawn over multiple days or not.
	 */
	private final SettingsModelBoolean m_allowSpawnMultipleDaysSettings = createSpawnMultipleDaysSettings();

	private final SettingsModelIntegerBounded m_maxTimeBetween = createMaxTimeBetweenSettings();

	private final SettingsModelIntegerBounded m_minRecordings = createMinimumRecordingsSettings();

	// Column select

	static SettingsModelColumnName createColIDSettings() {
		SettingsModelColumnName rowIDSettingModel = new SettingsModelColumnName(ID_COL, null);
		rowIDSettingModel.setEnabled(true);
		return rowIDSettingModel;
	}
	
	static SettingsModelColumnName createColTimestampSettings() {
		SettingsModelColumnName rowtimestampSettingModel = new SettingsModelColumnName(TIMESTAMP_COL, null);
		rowtimestampSettingModel.setEnabled(true);
		return rowtimestampSettingModel;
	}

	static SettingsModelColumnName createColGeometrySettings() {
		SettingsModelColumnName rowLocationSettingModel = new SettingsModelColumnName(GEOMETRY_COL, null);
		rowLocationSettingModel.setEnabled(true);
		return rowLocationSettingModel;
	}

	static SettingsModelString createRouteAssignerSettings() {
		SettingsModelString routeAssignerSetting = new SettingsModelString(ROUTE_ASSIGNER, DEFAULT_ROUTE_ASSIGNER);
		routeAssignerSetting.setEnabled(true);
		return routeAssignerSetting;
	}

	/**.
	 * The following method will also be used in the {@link RouteCalculatorNodeDialog}. 
	 * @return a new SettingsModelString with the key for the coordinate pair type String.
	 */
	static SettingsModelString createCoordinatePairTypeSettings() {
		SettingsModelString coordinatePairTypeSettings = new SettingsModelString(PAIR_TYPE_FORMAT, DEFAULT_PAIR_TYPE_FORMAT);
		coordinatePairTypeSettings.setEnabled(true);
		return coordinatePairTypeSettings;
	}


	/**.
	 * The following method will also be used in the {@link RouteCalculatorNodeDialog}. 
	 * @return a new SettingsModelBoolean with the key for the allow spawn multiple days setting, which
	 * controls if the assigner should subdivide routes by day. 
	 * @see RouteAssigner
	 * @see DefaultRouteAssigner
	 * @see DefaultRouteAssignerFactory
	 */
	static SettingsModelBoolean createSpawnMultipleDaysSettings() {
		SettingsModelBoolean spawnMultipleDaysSettings = new SettingsModelBoolean(ALLOW_SPAWN_MULTIPLE_DAYS, DEFAULT_SPAWN_MULTIPLE_DAYS);
		spawnMultipleDaysSettings.setEnabled(true);
		return spawnMultipleDaysSettings;
	}

	static SettingsModelIntegerBounded createMaxTimeBetweenSettings() {
		SettingsModelIntegerBounded maxTimeBetweenRecordsSettings =  new SettingsModelIntegerBounded(MAXIMUM_TIME_BETWEEN_RECORDS, DEFAULT_MAXIMUM_TIME_BETWEEN_RECORDS, 1, Integer.MAX_VALUE);
		maxTimeBetweenRecordsSettings.setEnabled(true);
		return maxTimeBetweenRecordsSettings; 
	}

	static SettingsModelIntegerBounded createMinimumRecordingsSettings() {
		SettingsModelIntegerBounded minRouteRecordingsSettings =  new SettingsModelIntegerBounded(MINIMUM_ROUTE_RECORDINGS, DEFAULT_MINIMUM_RECORDINGS, 2, Integer.MAX_VALUE);
		minRouteRecordingsSettings.setEnabled(true);
		return minRouteRecordingsSettings; 
	}


	/**
	 * Constructor for the node model.
	 */
	protected TrajectoryAssignerNodeModel() {
		/**
		 * Here we specify how many data input and output tables the node should have.
		 * In this case its one input and one output table.
		 */
		super(1, 2);
	}


	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {

		BufferedDataTable inputTable = inData[0];
		DataTableSpec outputSpecForMapMatching = createOutputForMapMatching();
		DataTableSpec outputSpecForRoutingNode = createOutputForRoutingNode();		

		BufferedDataContainer containerForMapMatching = exec.createDataContainer(outputSpecForMapMatching);
		BufferedDataContainer containerForRoutingNode = exec.createDataContainer(outputSpecForRoutingNode);

		CloseableRowIterator rowIterator = inputTable.iterator();
		String routeAssignerSetting = m_routeAssignerSettings.getStringValue();
		/*
		 * The String pairTypeFormat informs the PositionFactory if the coordinates are supplied as 
		 * {lat,lon}  or {lon,lat} pairs.
		 */
		String pairTypeFormat = m_pairTypeSettings.getStringValue();
		/*
		 * The boolean flag to be passed to the RouteAssigner. If true the route assigner can assign positions
		 * in different days to the same route, if false each position in a different day than the previous one will
		 * start a new route.
		 */
		boolean stretchOverMultipleDays = m_allowSpawnMultipleDaysSettings.getBooleanValue();
		/*
		 * This value informs the Route Assigner about the maximum time between two subsequent positions. If the time 
		 * between p1 and p2 is less than this value than they belong to the same route, else p2 becomes the first 
		 * position of a new route. The value is in minutes.
		 */
		int maxTimeBetweenRecords = m_maxTimeBetween.getIntValue();

		int minRecordings = m_minRecordings.getIntValue();
		int currentRowCounter = 0;
		long positionsEntryCounter = 0;
		LOGGER.info(LogStringMaker.logCurrentExecutionSetting(pairTypeFormat, stretchOverMultipleDays, routeAssignerSetting, maxTimeBetweenRecords));

		PositionFactory positionFactory = new PositionFactory(pairTypeFormat);
		TreeSet<Position> positions = new TreeSet<Position>(new Comparator<Position>() {
			@Override
			public int compare(Position o1, Position o2) {
				if(o1.getTimeOfRecord().after(o2.getTimeOfRecord())) {
					return 1;
				}else if(o1.getTimeOfRecord().before(o2.getTimeOfRecord())) {
					return -1;
				}else {
					return 0;
				}
			}
		});

		Map<Integer, TreeSet<Position>> taxiRoutes= new HashMap<Integer, TreeSet<Position>>();
		AbstractRouteAssignerFactory routeAssignerFactory;

		switch(routeAssignerSetting) {
		case("default"):
			routeAssignerFactory = new DefaultRouteAssignerFactory(stretchOverMultipleDays, maxTimeBetweenRecords, minRecordings);
		break;
		default:
			return null;
		}

		String colIDName = m_colIDSettings.getColumnName();
		String colTimestampName = m_colTimestampSettings.getColumnName();
		String colGeometryName = m_colGeometrySettings.getColumnName();	

		int idIndex = -1;
		int timeStampIndex = -1;
		int locationIndex = -1;

		DataTableSpec specs = inData[0].getDataTableSpec();
		for(int i = 0; i<specs.getNumColumns(); i++) {
			DataColumnSpec columnspec = specs.getColumnSpec(i);
			if(columnspec.getName().equals(colIDName)) {
				idIndex = i;
			}else if(columnspec.getName().equals(colTimestampName)) {
				timeStampIndex = i;
			}else if(columnspec.getName().equals(colGeometryName)) {
				locationIndex=i;
			}
		}


		if(idIndex==-1 || timeStampIndex == -1 || locationIndex == -1) {
			//unreachable (in theory), the configure method ensure the presence of the columns we searched for.
			LOGGER.info(LogStringMaker.logError("The input table must have the following columns: (id-Integer), (timestamp-String), (location-String)\n"));
			return null;
		}


		long routeId=0;
		int currentId = -1;
		int prevId= -1;

		RouteAssigner<Position> assigner = routeAssignerFactory.getRouteAssigner();

		while (rowIterator.hasNext()) {
			DataRow currentRow = rowIterator.next();
			int id = 0;
			String timestamp = null;
			String position = null;
			Position p;

			DataCell cell = currentRow.getCell(idIndex);
			if (cell.getType().getCellClass().equals((IntCell.class))) {
				IntCell intCell = (IntCell) cell;
				id = intCell.getIntValue();
				/*
				 * If the the prevId had value -1 initialize it to the current id.
				 */
				if(prevId==-1) {			
					prevId=id;
				}
				currentId=id;
			}
			//Assigning data
			cell = currentRow.getCell(timeStampIndex);
			if(cell.getType().getCellClass().equals(StringCell.class)) {
				StringCell stringCell = (StringCell) cell;
				timestamp = stringCell.getStringValue();
			}
			cell = currentRow.getCell(locationIndex);
			if(cell.getType().getCellClass().equals(StringCell.class)) {
				StringCell stringCell = (StringCell) cell;
				position = stringCell.getStringValue();
			}


			//Create the current position
			p= positionFactory.createPosition(id, timestamp, position);

			/*
			 * If either the current Id has changed from the previous Id, meaning we have read all
			 * the positions of said Id, OR the rowIterator does not have a nextValue, meaning we have
			 * finished reading the table, we begin the routing operations for the vehicle identified
			 * by the prevId.
			 */
			if(prevId!=currentId || !rowIterator.hasNext()) {
				/*
				 * In the case we have no next value the current position has to be added to the
				 * set holding the positions for id.
				 */


				if(!rowIterator.hasNext()) {
					positions.add(p);
				}

				/*
				 * Divide the positions into likely routes. 
				 */
				taxiRoutes = assigner.identifyRoutes(positions);

				int routeNumber = taxiRoutes.keySet().size();


				LOGGER.info(LogStringMaker.logRoutesBeingCalculated((rowIterator.hasNext()? prevId:currentId ), routeNumber));

				exec.setMessage("Found " + routeNumber + " routes for vehicle " + prevId);

				for(Integer k: taxiRoutes.keySet()) {
					LocalDateTime beginAt=null;
					ArrayList<GPSPosition> positionsOfRoute = new ArrayList<GPSPosition>();
					for(Position posInRoute: taxiRoutes.get(k)) {
						positionsOfRoute.add(posInRoute.getLocation());
						if(beginAt==null) {
							beginAt = LocalDateTime.parse(posInRoute.getTimeOfRecord().toString().replace(" ", "T"));
						}
						addPositionDataEntry(posInRoute, containerForRoutingNode, routeId, positionsEntryCounter++);
					}
					Route r = new Route((rowIterator.hasNext()? prevId: currentId),routeId++, positionsOfRoute, beginAt);
					addMapMatchingDataEntry(r, containerForMapMatching);
					exec.setMessage("Inserting routes " + k + " of " + routeNumber  +" for " + prevId );
				}
				prevId = currentId;
				positions.clear();
				if(rowIterator.hasNext()) {
					if(p.isValid()) {
						positions.add(p);
					}
				}
			}else{
				/*
				 * In this case, meaning we have more positions to read for the current id, or the input table has still rows
				 * to read, we add the read position to the holding set.
				 */
				if(p.isValid()) {
					positions.add(p);
				}
			}

			currentRowCounter++;

			exec.checkCanceled();

			exec.setProgress(currentRowCounter / (double) inputTable.size(), "Processing" + currentRowCounter + " of " + inputTable.size());
		}


		containerForMapMatching.close();
		BufferedDataTable outputForMapMatching = containerForMapMatching.getTable();
		containerForRoutingNode.close();
		BufferedDataTable outputForRouting = containerForRoutingNode.getTable();
		return new BufferedDataTable[] { outputForMapMatching, outputForRouting };
	}

	private void addMapMatchingDataEntry(Route r, BufferedDataContainer containerForMapMatching) {
		List<DataCell> cells = new ArrayList<>();
		
		DataCell idCell = new IntCell(r.getOwnerId());
		cells.add(idCell);
		
		DataCell beginAtCell = null;
		try {
			beginAtCell = DateAndTimeCell.fromString(r.getRouteBeginsAt().toString());
		} catch (ParseException e1) {
			beginAtCell= DataType.getMissingCell();
			e1.printStackTrace();
		}
		cells.add(beginAtCell);
		
		DataCell toAdd = new StringCell(r.getRouteAsGPSLinestring());
		cells.add(toAdd);
		
		DataRow row = new DefaultRow("N " + r.getRouteId(), cells);
		
		try {
			containerForMapMatching.addRowToTable(row);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		return;

	}


	private void addPositionDataEntry(Position posInRoute, BufferedDataContainer containerForRouting, long routeId,
			long positionsEntryCounter) {

		List<DataCell> cells = new ArrayList<>();
		//1
		DataCell idCell = new IntCell(posInRoute.getId());
		cells.add(idCell);
		//2
		DataCell routeIdCell = new LongCell(routeId);
		cells.add(routeIdCell);
		//3
		DataCell beginAtCell = null;
		try {
			beginAtCell = DateAndTimeCell.fromString(posInRoute.getTimeOfRecord().toString().replace(" ", "T"));
		} catch (ParseException e1) {
			beginAtCell= DataType.getMissingCell();
			e1.printStackTrace();
		}
		cells.add(beginAtCell);
		//4
		DataCell toAdd = new StringCell(posInRoute.getLocation().asWKT());
		cells.add(toAdd);

		DataRow row = new DefaultRow("N " + positionsEntryCounter, cells);
		try {
			containerForRouting.addRowToTable(row);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		return;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		String routeAssigner = m_routeAssignerSettings.getStringValue();

		switch(routeAssigner) {
		case("default"):
			validateDefaultRouteAssigner();
		break;
		default:
			LOGGER.error(LogStringMaker.logError("You have not selected a Route Assigner!"));
			throw new InvalidSettingsException("You have not selected a Route Assigner!");
		}
		
		String colIDName = m_colIDSettings.getColumnName();
		String colTimestampName = m_colTimestampSettings.getColumnName();
		String colGeometryName = m_colGeometrySettings.getColumnName();	

		if(colIDName == null || colTimestampName == null || colGeometryName == null) {
			LOGGER.info(LogStringMaker.logError("All columns must be selected in the configuration dialog"));
			throw new InvalidSettingsException("All columns must be selected in the configuration dialog");
		}

		if(!(inSpecs[0].containsName(colIDName) && inSpecs[0].containsName(colTimestampName) && inSpecs[0].containsName(colGeometryName))) {
			if(!(inSpecs[0].getColumnSpec(colIDName).getType().getCellClass().equals(IntCell.TYPE) &&
					inSpecs[0].getColumnSpec(colTimestampName).getType().getCellClass().equals(StringCell.TYPE) && 
					inSpecs[0].getColumnSpec(colGeometryName).getType().getCellClass().equals(StringCell.TYPE))) {
				LOGGER.info(LogStringMaker.logError("The input columns must have the following type: id-Integer, timestamp-String, location-String"));
				throw new InvalidSettingsException("The input columns must have the following type: id-Integer, timestamp-String, location-String");
			}
			LOGGER.info(LogStringMaker.logError("The input table must contain the following columns: id, timestamp, location"));
			throw new InvalidSettingsException("The input table must contain the following columns: id, timestamp, location");
		}

		return new DataTableSpec[] {createOutputForMapMatching(), createOutputForRoutingNode()} ;		
	}


	private DataTableSpec createOutputForMapMatching() {

		List<DataColumnSpec> newColumnSpecs = new ArrayList<>();
		DataColumnSpecCreator specCreator = new DataColumnSpecCreator("id", IntCell.TYPE);

		newColumnSpecs.add(specCreator.createSpec());
		/*
		 * The DateAndTimeCell are used to be compatible with the latest desktop 
		 * release of KNIME (4.3). They are deprecated only in the 4.4, which is 
		 * has not yet been released to the public.
		 */
		specCreator = new DataColumnSpecCreator("begin_at", DateAndTimeCell.TYPE);
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

	private DataTableSpec createOutputForRoutingNode() {
		List<DataColumnSpec> newColumnSpecs = new ArrayList<>();

		DataColumnSpecCreator specCreator = new DataColumnSpecCreator("owner_id", IntCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());

		specCreator = new DataColumnSpecCreator("route_id", LongCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		/*
		 * The DateAndTimeCell are used to be compatible with the latest desktop 
		 * release of KNIME (4.3). They are deprecated only in the 4.4, which is 
		 * has not yet been released to the public.
		 */
		specCreator = new DataColumnSpecCreator("timestamp", DateAndTimeCell.TYPE);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_colIDSettings.saveSettingsTo(settings);
		m_colTimestampSettings.saveSettingsTo(settings);
		m_colGeometrySettings.saveSettingsTo(settings);
		
		m_routeAssignerSettings.saveSettingsTo(settings);
		m_pairTypeSettings.saveSettingsTo(settings);
		m_allowSpawnMultipleDaysSettings.saveSettingsTo(settings);
		m_maxTimeBetween.saveSettingsTo(settings);
		m_minRecordings.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_colIDSettings.loadSettingsFrom(settings);
		m_colTimestampSettings.loadSettingsFrom(settings);
		m_colGeometrySettings.loadSettingsFrom(settings);
		
		m_routeAssignerSettings.loadSettingsFrom(settings);
		m_pairTypeSettings.loadSettingsFrom(settings);
		m_allowSpawnMultipleDaysSettings.loadSettingsFrom(settings);
		m_maxTimeBetween.loadSettingsFrom(settings);
		m_minRecordings.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_colIDSettings.validateSettings(settings);
		m_colTimestampSettings.validateSettings(settings);
		m_colGeometrySettings.validateSettings(settings);
		
		m_routeAssignerSettings.validateSettings(settings);
		m_pairTypeSettings.validateSettings(settings);
		m_allowSpawnMultipleDaysSettings.validateSettings(settings);
		m_maxTimeBetween.validateSettings(settings);
		m_minRecordings.validateSettings(settings);
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

	private void validateDefaultRouteAssigner() throws InvalidSettingsException {

		/*
		 * Check if the pair Type format is valid, i.e. is {lon,lat} or {lat,lon}.
		 */
		String format = m_pairTypeSettings.getStringValue();
		if(!format.equals("{lat,lon}") && !format.equals("{lon,lat}")) {
			throw new InvalidSettingsException("The entered format is not a valid coordinate pair type!");
		}

		int maxTimeBetweenRecords = m_maxTimeBetween.getIntValue();

		if(maxTimeBetweenRecords < 1 ) {
			throw new InvalidSettingsException("The time limit for stationary minutes before a new Route cannot be less than 1!");
		}

		int minRouteRecordings = m_minRecordings.getIntValue();
		if(minRouteRecordings < 2) {
			throw new InvalidSettingsException("Each route should contain at least two positions!");
		}
	}
}

