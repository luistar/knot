package org.unina.spatialanalysis.routecalculator;

import java.io.File;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.sort.BufferedDataTableSorter;
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
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.osgi.framework.ServiceException;
import org.unina.spatialanalysis.routecalculator.entity.position.Position;
import org.unina.spatialanalysis.routecalculator.entity.position.PositionFactory;
import org.unina.spatialanalysis.routecalculator.entity.route.Route;
import org.unina.spatialanalysis.routecalculator.logger.LogStringMaker;
import org.unina.spatialanalysis.routecalculator.routingservice.*;
import org.unina.spatialanalysis.routecalculator.routingservice.osrmroutingservice.OsrmRoutingServiceFactory;

/**
 * This is the implementation of the node model of the
 * "RouteCalculator" node.
 * 
 * This node first separates an input table containing geographic information about vehicles
 * positioning into likely subsets and then calculates routes. The calculation of routes is 
 * done invoking an external service (currently the only supported service is OSRM).
 * 
 * @link https://github.com/Project-OSRM/osrm-backend
 *
 * @author Sinogrante Principe
 */
@SuppressWarnings("deprecation")
public class RouteCalculatorNodeModel extends NodeModel {
    
    /**
	 * The logger is used to print info/warning/error messages to the KNIME console
	 * and to the KNIME log file. Retrieve it via 'NodeLogger.getLogger' providing
	 * the class of this node model.
	 */
	private static final NodeLogger LOGGER = NodeLogger.getLogger(RouteCalculatorNodeModel.class);
	
	/**
	 * A series of constant values used to create the various SettingsModel objects, which
	 * are then used to extract the settings from the Node Dialog.
	 */
	
	private static final String ROUTING_SERVICE = "m_routing_service";
	
	private static final String DEFAULT_ROUTING_SERVICE = "OSRM";
	
	private static final String PAIR_TYPE_FORMAT = "m_pair_type";

	private static final String DEFAULT_PAIR_TYPE_FORMAT = "{lat,lon}";
	
	private static final String ROUTING_SERVICE_HOST = "m_routing_service_host";
	
	private static final String DEFAULT_ROUTING_SERVICE_HOST = "http://127.0.0.1:5000";
	
	private static final String ROUTING_MODE ="m_routing_mode";
	
	private static final String DEFAULT_ROUTING_MODE = "shortest";
	
	private static final String MINIMUM_ROUTE_DISTANCE  = "m_min_spatial_length";
	
	private static final int DEFAULT_MIN_ROUTE_DISTANCE = 500;
	
	private static final String MINIMUM_ROUTE_DURATION = "m_min_route_time_length";
	
	private static final int DEFAULT_MIN_ROUTE_DURATION = 5;
	
	
	/**
	 * The settings model to manage which service is used to calculate the routes.
	 */
	private final SettingsModelString m_routingServiceSettings = createRoutingServiceSettings();
	
	/**
	 * The settings model to manage the type of coordinate pairs provided to the node,
	 * either {lon,lat} or {lat,lon}.
	 **/
	private final SettingsModelString m_pairTypeSettings = createCoordinatePairTypeSettings();
	
	/**
	 * The settings model to manage the routing service, it contains the information required
	 * to access the routing service server.
	 */
	private final SettingsModelString m_routingServiceHostSettings = createRoutingServiceHostSettings();
	
	/**
	 * The settings model to manage the routing service, it contains the information required
	 * to access the routing service server.
	 */
	private final SettingsModelString m_routingMode = createRoutingModeSettings();
	
	private final SettingsModelIntegerBounded m_minRouteDistance = createMinRouteDistanceSettings();
	
	private final SettingsModelIntegerBounded m_minRouteDuration = createMinRouteDurationSettings();
	
	
	/**
	 * Constructor for the node model.
	 */
	protected RouteCalculatorNodeModel() {
		/**
		 * Here we specify how many data input and output tables the node should have.
		 * In this case its one input and one output table.
		 * @see this.createOutputSpec();
		 * @see this.configure()
		 * 
		 */
		super(1, 1);
	}
	
	
	static SettingsModelString createRoutingServiceSettings() {
		SettingsModelString  routingMachineSettings = new SettingsModelString(ROUTING_SERVICE, DEFAULT_ROUTING_SERVICE);
		routingMachineSettings.setEnabled(true);
		return routingMachineSettings;
	}
	
	
	/**.
	 * The following method will also be used in the {@link RouteCalculatorNodeDialog}. 
	 * @return a new SettingsModelString with the key for the OSRM host server String.
	 */
	static SettingsModelString createRoutingServiceHostSettings() {
		SettingsModelString routingMachineHostSettings = new SettingsModelString(ROUTING_SERVICE_HOST, DEFAULT_ROUTING_SERVICE_HOST);
		routingMachineHostSettings.setEnabled(true);
		return routingMachineHostSettings;
	}
	
	/**
	 * The following method will also be used in the {@link RouteCalculatorNodeDialog}. 
	 * @return a new SettingsModelString with the key for the Routing Mode setting, which controls
	 * how the routes are calculated.
	 * @see RoutingService
	 * @see OsrmRoutingService
	 */
	static SettingsModelString createRoutingModeSettings() {
		SettingsModelString createRoutingModeSettings =  new SettingsModelString(ROUTING_MODE, DEFAULT_ROUTING_MODE);
		createRoutingModeSettings.setEnabled(true);
		return createRoutingModeSettings;
	}
	
	static SettingsModelIntegerBounded createMinRouteDurationSettings() {
		SettingsModelIntegerBounded minRouteTimeLengthSetting = new SettingsModelIntegerBounded(MINIMUM_ROUTE_DURATION, DEFAULT_MIN_ROUTE_DURATION, 0, Integer.MAX_VALUE);
		minRouteTimeLengthSetting.setEnabled(true);
		return minRouteTimeLengthSetting;
	}
	
	
	static SettingsModelIntegerBounded createMinRouteDistanceSettings() {
		SettingsModelIntegerBounded minRouteSpatialLengthSetting = new SettingsModelIntegerBounded(MINIMUM_ROUTE_DISTANCE, DEFAULT_MIN_ROUTE_DISTANCE, 0, Integer.MAX_VALUE);
		minRouteSpatialLengthSetting.setEnabled(true);
		return minRouteSpatialLengthSetting;
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
	
	/**
	 * {@inheritDoc}
	 * The method separates the input data accordingly to the position owner and then separates the positions
	 * of each owner into most likely routes. The routes are then calculated, accordingly to the routing mode
	 * selected in the settings, and inserted into the output table.
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		
		String routingServiceSetting = m_routingServiceSettings.getStringValue();
		/*
		 * The String pairTypeFormat informs the PositionFactory if the coordinates are supplied as 
		 * {lat,lon}  or {lon,lat} pairs.
		 */
		String pairTypeFormat = m_pairTypeSettings.getStringValue();
		
		/*
		 * The host of the OSRM routing service.
		 */
		String host = m_routingServiceHostSettings.getStringValue();
		
		/*
		 * The routing mode for the routing service, either 'match' or 'shortest'.
		 */
		String routingMode = m_routingMode.getStringValue();
		
		int minRouteDuration = m_minRouteDuration.getIntValue() *60;
		
		int minRouteDistance = m_minRouteDistance.getIntValue();
		
		
		
		
		LOGGER.info(LogStringMaker.logCurrentExecutionSetting(pairTypeFormat, host, routingServiceSetting , routingMode, minRouteDistance, minRouteDuration/60));
		
		
		/*
		 * Variables holding information for the log.
		 */
		int numberOfMissedRoutes = 0;
		int numberOfDiscardedRoutes = 0;
		int numberOfFoundRoutes = 0;
		/*
		 * The class used to create Position objects.
		 */
		PositionFactory positionFactory = new PositionFactory(pairTypeFormat);
		

		/*
		 * This tree set will hold the positions read from the input table for each distinct id. When the recordings
		 * of one specific id have all been read then this object become the input for the Route Assigner, the routes get
		 * calculated and stored in the result table and then this Set gets cleared. 
		 */
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
		
				
		/*
		 * We create the Routing Service for the specified settings.
		 */
		AbstractRoutingServiceFactory routingServiceFactory;
		
		switch(routingServiceSetting) {
			case("OSRM"):
				routingServiceFactory = new OsrmRoutingServiceFactory(host);
				break;
			default:
				return null;
		}
		
		
		RoutingService<Position, Route> routingService = routingServiceFactory.getRoutingService(routingMode);
	
		/*
		 * Create the spec of the output table.
		 */
		DataTableSpec outputSpec = createOutputSpec();

		/*
		 * The execution context provides storage capacity, in this case a
		 * data container to which we will add rows sequentially. Note, this container
		 * can handle arbitrary big data tables, it will buffer to disc if necessary.
		 */
		BufferedDataContainer container = exec.createDataContainer(outputSpec);
		

		/*
		 * This block of code takes the input table and search for the relevant columns,
		 * if one of said columns could not be found the method returns null, which prompts
		 * an error in the execution. 
		 */
		int routeIdIndex = -1;
		int timeStampIndex = -1;
		int locationIndex = -1;
		int ownerIdIndex = -1;
		
		DataTableSpec specs = inData[0].getDataTableSpec();
		for(int i = 0; i<specs.getNumColumns(); i++) {
			DataColumnSpec columnspec = specs.getColumnSpec(i);
			if(columnspec.getName().equals("route_id")) {
				routeIdIndex = i;
			}else if(columnspec.getName().equals("timestamp")) {
				timeStampIndex = i;
			}else if(columnspec.getName().equals("the_geom")) {
				locationIndex=i;
			}else if(columnspec.getName().equals("owner_id")) {
				ownerIdIndex = i;
			}
		}
		
		
		if(routeIdIndex==-1 || timeStampIndex == -1 || locationIndex == -1 || ownerIdIndex==-1) {
			//unreachable (in theory), the configure method ensure the presence of the columns we searched for.
			LOGGER.info(LogStringMaker.logError("The input table must have the following columns: (id-Integer), (timestamp-String), (location-String)\n"));
			return null;
		}
		

		BufferedDataTable inputTable = inData[0];

		CloseableRowIterator rowIterator = inputTable.iterator();


		/*
		 * The current id that is being read from the input table.
		 */
		long currentRouteId = -1;
		
		/*
		 * The previous id that was read from the input table
		 */
		long prevRouteId= -1;
		
		/*
		 * The number of rows read, the value is used to generate the progress bar under the node.
		 */
		long currentRowCounter = 0;
		
		while (rowIterator.hasNext()) {
			
			//Begin reading 
			DataRow currentRow = rowIterator.next();
			
			long routeId = 0;
			int ownerId = 0;
			String timestamp = null;
			String position = null;
			Position p;
			
			DataCell cell = currentRow.getCell(routeIdIndex);
			if (cell.getType().getCellClass().equals((LongCell.class))) {
				LongCell routeIdCell = (LongCell) cell;
				routeId = routeIdCell.getLongValue();
				/*
				 * If the the prevId had value -1 initialize it to the current id.
				 */
				if(prevRouteId==-1) {			
					prevRouteId=routeId;
				}
				currentRouteId=routeId;
			}
			//Assigning data
			cell = currentRow.getCell(timeStampIndex);
			if(cell.getType().getCellClass().equals(DateAndTimeCell.class)) {
					DateAndTimeCell timestampCell = (DateAndTimeCell) cell;
					timestamp = timestampCell.getStringValue();
				}
			cell = currentRow.getCell(locationIndex);
			if(cell.getType().getCellClass().equals(StringCell.class)) {
				StringCell stringCell = (StringCell) cell;
				position = stringCell.getStringValue();
			}
			
			cell = currentRow.getCell(ownerIdIndex);
			if(cell.getClass().equals(IntCell.class)){
				IntCell ownerIdCell = (IntCell) cell;
				ownerId = ownerIdCell.getIntValue();
			}else {
				System.out.println("BUT WHY?");
			}
			
			//Create the current position
			p= positionFactory.createPosition(ownerId, timestamp, position);
			
			/*
			 * If either the current Id has changed from the previous Id, meaning we have read all
			 * the positions of said Id, OR the rowIterator does not have a nextValue, meaning we have
			 * finished reading the table, we begin the routing operations for the vehicle identified
			 * by the prevId.
			 */
			if(prevRouteId!=currentRouteId || !rowIterator.hasNext()) {
				/*
				 * In the case we have no next value the current position has to be added to the
				 * set holding the positions for id.
				 */
				if(!rowIterator.hasNext()) {
					positions.add(p);
				}
				Route route = null;
				try {
					route = routingService.findRoute(positions);
					if(route != null) {
						numberOfFoundRoutes++;
						if(route.getDuration()>=minRouteDuration && route.getDistance()>=minRouteDistance) {
							addRouteToResultTable(route, container, " "+numberOfFoundRoutes);
						}else {
							numberOfDiscardedRoutes++;
						}
					}			
					prevRouteId = currentRouteId;
					positions.clear();
					if(rowIterator.hasNext()) {
						if(p.isValid()) {
							positions.add(p);
						}
					}
				}catch(ServiceException e) {
					numberOfMissedRoutes++;
				}catch(IOException e) {
					numberOfMissedRoutes++;
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

			
			 
			
			// We finished processing one row, hence increase the counter
			currentRowCounter++;

			/*
			 * Here we check if a user triggered a cancel of the node. 
			 */
			exec.checkCanceled();

			/*
			 * Calculate the percentage of execution progress and inform the
			 * ExecutionMonitor. 
			 */
			exec.setProgress(currentRowCounter / (double) inputTable.size(), "Processing row " + currentRowCounter + " of " +inputTable.size());
		}
	
		/*
		 * Once we are done, we close the container and return its table.
		 */
		container.close();
		BufferedDataTable routes = container.getTable();
		
		LOGGER.info(LogStringMaker.logExecutionEnd(currentRowCounter, inputTable.size() ,numberOfFoundRoutes, numberOfMissedRoutes, numberOfDiscardedRoutes, routingMode));
		
		return new BufferedDataTable[] {routes};
	}
	
	/*
	 * A convenience method to add a route to the result table.
	 */
	private void addRouteToResultTable(Route r, BufferedDataContainer container, String specifier ) {
			List<DataCell> cells = new ArrayList<>();
			DataCell idCell = new IntCell(r.getId());
			cells.add(idCell);
			DataCell start = new StringCell(r.getStart().asWKT());
			cells.add(start);
			DataCell end = new StringCell(r.getEnd().asWKT());
			cells.add(end);
			DataCell time = new DoubleCell(r.getDuration());
			cells.add(time);
			DataCell length = new DoubleCell(r.getDistance());
			cells.add(length);
			DataCell beginAt = null;
			try {
				beginAt = DateAndTimeCell.fromString(r.getRouteBeginsAt().toString().replace(" ", "T"));
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			cells.add(beginAt);
			DataCell endsAt = null;
			try {
				endsAt = DateAndTimeCell.fromString(r.getRouteEndsAt().toString().replace(" ", "T"));
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			cells.add(endsAt);
			DataCell toAdd = new StringCell(r.getRouteAsGPSLinestring());
			cells.add(toAdd);
			DataRow row = new DefaultRow("N" + specifier, cells);
			try {
				container.addRowToTable(row);
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
		/*
		 * Check if the node is executable, e.g. all required user settings are
		 * available and valid, or the incoming types are feasible for the node to
		 * execute
		 */
		
		/*
		 * Take the Routing Machine selected and validate it.
		 */
		String routingMachine = m_routingServiceSettings.getStringValue();
		
		switch(routingMachine) {
			case("OSRM"):
				validateOSRMRoutingMachine();
				break;
			default:
				LOGGER.error(LogStringMaker.logError("You have not selected a Routing Machine!"));
				throw new InvalidSettingsException("You have not selected a Routing Machine!");
		}
		

		/*
		 * Check if the input table contains all that we need.
		 */
		if(!(inSpecs[0].containsName("route_id") && inSpecs[0].containsName("timestamp") && inSpecs[0].containsName("the_geom")) && inSpecs[0].containsName("owner_id")) {
			if(!(inSpecs[0].getColumnSpec("route_id").getType().getCellClass().equals(LongCell.TYPE) &&
					inSpecs[0].getColumnSpec("timestamp").getType().getCellClass().equals(DateAndTimeCell.TYPE) && 
							inSpecs[0].getColumnSpec("the_geom").getType().getCellClass().equals(StringCell.TYPE)) &&
					inSpecs[0].getColumnSpec("owner_id").getType().getCellClass().equals(IntCell.TYPE)) {
				LOGGER.info(LogStringMaker.logError("The input columns must have the following type: id-Integer, timestamp-LocalDateTime, location-String"));
				throw new InvalidSettingsException("The input columns must have the following type: id-Integer, timestamp-LocalDateTime, location-String");
			}
			LOGGER.info(LogStringMaker.logError("The input table must contain the following columns: id, timestamp, location"));
			throw new InvalidSettingsException("The input table must contain the following columns: id, timestamp, location");
		}

		return new DataTableSpec[] {createOutputSpec()};
	}
	
	

	private void validateOSRMRoutingMachine() throws InvalidSettingsException {
		/*
		 * Check if the OSRM server host inserted is a valid URL and if it available.
		 */
		String osrmHost = m_routingServiceHostSettings.getStringValue();
		osrmHost = osrmHost.toLowerCase();
		try {
		    URL myURL = new URL(osrmHost);
		    URLConnection myURLConnection = myURL.openConnection();
		    myURLConnection.connect();
		} 
		catch (MalformedURLException e) {
			LOGGER.error(LogStringMaker.logError("The entered host is not a valid URL!"));
			throw new InvalidSettingsException("The entered host is not a valid URL!");
		} 
		catch (IOException e) {   
			LOGGER.error(LogStringMaker.logError("The entered host could not be reached!"));
			throw new InvalidSettingsException("The entered host could not be reached!");
		}
		
		int minRouteTimeLength = m_minRouteDuration.getIntValue();
		
		if(minRouteTimeLength<0) {
			LOGGER.error(LogStringMaker.logError("The minimum route duration cannot be negative!"));
			throw new InvalidSettingsException("The minimum route duration cannot be negative!");
		}
		
		int minRouteSpatialLength = m_minRouteDistance.getIntValue();
		
		if(minRouteSpatialLength<0) {
			LOGGER.error(LogStringMaker.logError("The minimum route distance cannot be negative!"));
			throw new InvalidSettingsException("The minimum route distance cannot be negative!");
		}
		
		
		String routingMode = m_routingMode.getStringValue();
		if(!(routingMode.equals("match") || routingMode.equals("shortest"))) {
			LOGGER.error(LogStringMaker.logError("The routing mode selected is not available for the OSRM routing machine!"));
			throw new InvalidSettingsException("The routing mode selected is not available for the OSRM routing machine!");
		}
	}


	/**
	 * Creates the output table spec.
	 * @return
	 */
	private DataTableSpec createOutputSpec() {
		List<DataColumnSpec> newColumnSpecs = new ArrayList<>();
		DataColumnSpecCreator specCreator = new DataColumnSpecCreator("id", IntCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("start", StringCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("end", StringCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("time", DoubleCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("distance", DoubleCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		/*
		 * The DateAndTimeCell are used to be compatible with the latest desktop 
		 * release of KNIME (4.3). They are deprecated only in the 4.4, which is 
		 * has not yet been released to the public.
		 */
		specCreator = new DataColumnSpecCreator("begin_at", DateAndTimeCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("end_at", DateAndTimeCell.TYPE);
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
		/*
		 * Save user settings to the NodeSettings object. SettingsModels already know how to
		 * save them self to a NodeSettings object by calling the below method. In general,
		 * the NodeSettings object is just a key-value store and has methods to write
		 * all common data types. Hence, you can easily write your settings manually.
		 * See the methods of the NodeSettingsWO.
		 */
		m_routingServiceSettings.saveSettingsTo(settings);
		m_routingServiceHostSettings.saveSettingsTo(settings);
		m_pairTypeSettings.saveSettingsTo(settings);
		m_routingMode.saveSettingsTo(settings);
		m_minRouteDistance.saveSettingsTo(settings);
		m_minRouteDuration.saveSettingsTo(settings); 
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
		m_routingServiceSettings.loadSettingsFrom(settings);
		m_routingServiceHostSettings.loadSettingsFrom(settings);
		m_pairTypeSettings.loadSettingsFrom(settings);
		m_routingMode.loadSettingsFrom(settings);
		m_minRouteDistance.loadSettingsFrom(settings);
		m_minRouteDuration.loadSettingsFrom(settings); 
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
		m_routingServiceSettings.validateSettings(settings);
		m_routingServiceHostSettings.validateSettings(settings);
		m_pairTypeSettings.validateSettings(settings);
		m_routingMode.validateSettings(settings);
		m_minRouteDistance.validateSettings(settings);
		m_minRouteDuration.validateSettings(settings); 
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
	
	/**
	 * A method to sort the input table by Id.
	 * @deprecated due to heavy memory usage.
	 * @param toSort
	 * @param idIndex
	 * @param exec
	 * @throws CanceledExecutionException 
	 */
	@SuppressWarnings("unused")
	private void sortInputTableById(BufferedDataTable toSort, int idIndex, ExecutionContext exec) throws CanceledExecutionException {
		boolean [] check = new boolean [toSort.getDataTableSpec().getNumColumns()];
		for(int i = 0; i< check.length; i++) {
			if(i==idIndex) {
				check[i]= true;
			}else {
				check[i] = false;
			}
		}
		
		List<String> columnNames = new ArrayList<>();
		for(String s:toSort.getSpec().getColumnNames()) {
			columnNames.add(s);
		}
		
		BufferedDataTableSorter sorter = new BufferedDataTableSorter(toSort, columnNames , check );
		 
		toSort = sorter.sort(exec);
	}

}

