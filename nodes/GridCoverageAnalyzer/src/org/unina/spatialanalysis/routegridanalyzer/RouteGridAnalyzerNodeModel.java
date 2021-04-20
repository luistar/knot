package org.unina.spatialanalysis.routegridanalyzer;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.knime.core.data.def.IntCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.unina.spatialanalysis.routegridanalyzer.RouteGridAnalyzerNodeModel;
import org.unina.spatialanalysis.routegridanalyzer.entity.TimeSlot;
import org.unina.spatialanalysis.routegridanalyzer.entity.dataentries.DetailedDataEntry;
import org.unina.spatialanalysis.routegridanalyzer.entity.dataentries.SimpleDataEntry;
import org.unina.spatialanalysis.routegridanalyzer.entity.grid.Grid;
import org.unina.spatialanalysis.routegridanalyzer.entity.grid.GridSlot;
import org.unina.spatialanalysis.routegridanalyzer.entity.grid.Hit;
import org.unina.spatialanalysis.routegridanalyzer.entity.supportstructures.HitHolder;
import org.unina.spatialanalysis.routegridanalyzer.entity.supportstructures.HitsInDay;


/**
 * This is an example implementation of the node model of the
 * "RouteGridAnalyzer" node.
 * 
 * This example node performs simple number formatting
 * ({@link String#format(String, Object...)}) using a user defined format string
 * on all double columns of its input table.
 *
 * @author Sinogrante Principe
 */
public class RouteGridAnalyzerNodeModel extends NodeModel {
	 
		private static final NodeLogger LOGGER = NodeLogger.getLogger(RouteGridAnalyzerNodeModel.class);
		
		private static final String MIN_LAT  = "m_min_lat";
		
		private static final String DEFAULT_MIN_LAT ="41.7775";
		
		private static final String MAX_LAT  = "m_max_lat";
		
		private static final String DEFAULT_MAX_LAT ="41.9962";
		
		private static final String MIN_LON  = "m_min_lon";
		
		private static final String DEFAULT_MIN_LON ="12.3473";
		
		private static final String MAX_LON  = "m_max_lot";
		
		private static final String DEFAULT_MAX_LON ="12.6508";
		
		private static final String N_ROWS = "m_n_rows";
		
		private static final int DEFAULT_N_ROWS = 10;
		
		private static final String N_COLUMNS = "m_n_columns";
		
		private static final int DEFAULT_N_COLUMNS = 10;
		
		private static final String MINUTES_BETWEEN = "m_minutes";
		
		private static final int DEFAULT_MINUTES_BETWEEN = 10;
		
		private final SettingsModelString m_minLat = createMinLatSetting();
		
		private final SettingsModelString m_maxLat = createMaxLatSetting();
		
		private final SettingsModelString m_minLon = createMinLonSetting();

		private final SettingsModelString m_maxLon = createMaxLonSetting();

		private final SettingsModelIntegerBounded m_numberOfRows = createNumberRowsSetting();
		
		private final SettingsModelIntegerBounded m_numberOfColumns = createNumberColumnsSetting();
		
		private final SettingsModelIntegerBounded m_minutes = createMinutesSetting();

		
		/**
		 * Constructor for the node model.
		 */
		protected RouteGridAnalyzerNodeModel() {
			/**
			 * Here we specify how many data input and output tables the node should have.
			 * In this case its one input and one output table.
			 */
			super(1, 2);
		}

		/**
		 * A convenience method to create a new settings model used for the number
		 * format String. This method will also be used in the {@link RouteStepAnalyzerNodeDialog}. 
		 * The settings model will sync via the above defined key.
		 * 
		 * @return a new SettingsModelString with the key for the number format String
		 */
		
		static SettingsModelString createMinLatSetting() {
			return new SettingsModelString(MIN_LAT, DEFAULT_MIN_LAT);
		}
		
		static SettingsModelString createMaxLatSetting() {
			return new SettingsModelString(MAX_LAT, DEFAULT_MAX_LAT);
		}
		
		static SettingsModelString createMinLonSetting() {
			return new SettingsModelString(MIN_LON, DEFAULT_MIN_LON);
		}
		
		static SettingsModelString createMaxLonSetting() {
			return new SettingsModelString(MAX_LON, DEFAULT_MAX_LON);
		}
		
		static SettingsModelIntegerBounded createNumberRowsSetting() {
			return new SettingsModelIntegerBounded(N_ROWS, DEFAULT_N_ROWS, 1, Integer.MAX_VALUE);
		}
		
		static SettingsModelIntegerBounded createNumberColumnsSetting() {
			return new SettingsModelIntegerBounded(N_COLUMNS, DEFAULT_N_COLUMNS, 1, Integer.MAX_VALUE);
		}
		
		static SettingsModelIntegerBounded createMinutesSetting() {
			return new SettingsModelIntegerBounded(MINUTES_BETWEEN, DEFAULT_MINUTES_BETWEEN, 0, Integer.MAX_VALUE);
		}
		

		/**
		 * 
		 * {@inheritDoc}
		 */
		@Override
		protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
				throws Exception {
			
			LOGGER.info("This is an example info.");
			BufferedDataTable inputTable = inData[0];
			DataTableSpec complexOutputSpec = createOutputForDetailed();
			BufferedDataContainer complexContainer = exec.createDataContainer(complexOutputSpec);
			DataTableSpec simpleOutputSpec = createOutputForSimple();
			BufferedDataContainer simpleContainer = exec.createDataContainer(simpleOutputSpec);
			
			double minLat = Double.parseDouble(m_minLat.getStringValue());
			double maxLat = Double.parseDouble(m_maxLat.getStringValue());
			double minLon = Double.parseDouble(m_minLon.getStringValue());
			double maxLon = Double.parseDouble(m_maxLon.getStringValue());
			
			int nRows = m_numberOfRows.getIntValue();
			int nColumns = m_numberOfColumns.getIntValue();
						
			Grid grid = new Grid(minLat, maxLat, minLon, maxLon, nRows, nColumns); 
			
			CloseableRowIterator rowIterator = inputTable.iterator();
			int currentRowCounter = 0;
			int idIndex = -1;
			int beginAtIndex = -1;
			int theGeomIndex =-1;
			int endAtIndex= -1;
		
			DataTableSpec specs = inputTable.getDataTableSpec();
			for(int i = 0; i<specs.getNumColumns(); i++) {
				DataColumnSpec columnspec = specs.getColumnSpec(i);
				if(columnspec.getName().equals("owner_id")) {
					idIndex = i;
				}else if(columnspec.getName().equals("begin_at")) {
					beginAtIndex = i;
				}else if(columnspec.getName().equals("the_geom")) {
					theGeomIndex = i;
				}else if(columnspec.getName().equals("end_at")) {
					endAtIndex = i;
				}
			}
			
			
			TreeSet<Hit> recordedPositions = new TreeSet<Hit>();
									
			/**
			 * After this iteration the variable recordedPositions contains all the recorded positions ordered by time.
			 */
			while(rowIterator.hasNext()) {
				exec.checkCanceled();
				DataRow currentRow = rowIterator.next();
				if(!currentRow.getKey().getString().contains("NEVER_VISITED")) {
					currentRowCounter++;
					int ownerId = Integer.MIN_VALUE;
					LocalDateTime beginAt = null;
					LocalDateTime endAt = null;
					String theGeom = null;
									
					DataCell cell = currentRow.getCell(idIndex);
					if(cell.getType().getCellClass().equals((IntCell.class))) {
						IntCell intCell = (IntCell) cell;
						ownerId = intCell.getIntValue();
					}
					
					cell = currentRow.getCell(beginAtIndex);
					if(cell.getType().getCellClass().equals(DateAndTimeCell.class)) {
						DateAndTimeCell dateTimeCell = (DateAndTimeCell) cell;
						beginAt = LocalDateTime.parse(dateTimeCell.getStringValue());
					}
					
					cell = currentRow.getCell(endAtIndex);
					if(cell.getType().getCellClass().equals(DateAndTimeCell.class)) {
						DateAndTimeCell dateTimeCell = (DateAndTimeCell) cell;
						endAt = LocalDateTime.parse(dateTimeCell.getStringValue());
					}
					
								
					cell = currentRow.getCell(theGeomIndex);
					if(cell.getType().getCellClass().equals(StringCell.class)) {
						StringCell theGeomCell = (StringCell) cell;
						theGeom = theGeomCell.getStringValue();
					}
					
					if(ownerId==Integer.MIN_VALUE || theGeom==null || beginAt== null || endAt==null) {
						LOGGER.info("Row n " + currentRowCounter + " has invalid values! It will be skipped.\n");
						
					}else {
						String tmp = theGeom.replace("LINESTRING(", "");
						tmp = tmp.replace(")", "");
						String origin [];
						String destination [];
						String s [] = tmp.split(",");
						System.out.println(s.length);
						if(s.length!=2) {
							
						}else {
							origin = s[0].trim().split(" ");
							destination = s[1].trim().split(" ");
							System.out.println("origin " + origin.length);
							System.out.println("destination " + destination.length);
	
							if(origin.length!=2 || destination.length!=2) {
								
							}else {
								double originLongitude = Double.parseDouble(origin[0]);
								double originLatitude = Double.parseDouble(origin[1]);
								double destinationLongitude = Double.parseDouble(destination[0]);
								double destinationLatitude = Double.parseDouble(destination[1]);
								double toAddLongitude = (originLongitude + destinationLongitude)/2;
								double toAddLatitude = (originLatitude + destinationLatitude)/2;
								long timeBetween = ChronoUnit.MICROS.between(beginAt, endAt);
								LocalDateTime time = ChronoUnit.MICROS.addTo(beginAt, timeBetween/2);
								Hit h = new Hit(toAddLongitude, toAddLatitude, time, ownerId);
								recordedPositions.add(h);
							}
						}
					}
				}
				exec.checkCanceled();
				exec.setProgress((currentRowCounter/(double)inputTable.size()), "Reading input data...");
			}
			
			/**
			 * After this cycle hitHolder contains a mapping of ids and hits, considering only those hits that are
			 * after x minutes after the last hit OR
			 * in a different grid slot than the last hit.
			 */
			
			HitHolder hitHolder = new HitHolder(m_minutes.getIntValue());
			
			int current = 0;
			for(Hit h: recordedPositions) {
				current++;
				exec.setProgress(current/(double) recordedPositions.size(), "Assigning position " + current + " of " + recordedPositions.size());
				if(grid.checkIfValidPosition(h)) {
					hitHolder.addHit(h, grid.getGridSlotPosition(h));
				}
				System.out.println(h.toString());
				exec.checkCanceled();
			}
			
			
			/**
			 * hitHolder now contains the valid information. We simply have to put it in the right position
			 * in the grid. During this operation we save the distinct dates we encounter.
			 */
			
			exec.setProgress(0, "Storing results...");
			HashSet<LocalDate> forNormalizing = new HashSet<LocalDate>();
			current = 0;
			for(Integer id: hitHolder.keySet()) {
				current++;
				exec.setProgress(current/(double)hitHolder.keySet().size(), "Storing results...");
				for(Hit h: hitHolder.get(id)) {
					grid.addHitToGrid(h);
					forNormalizing.add(h.getTime().toLocalDate());
				}
				exec.checkCanceled();
			}
			
			/**
			 * This iteration adds empty visit sets to the days in which a GridSlot was not visited at all.
			 * This is done so that for each day the result table holds information about each rectangle of the 
			 * grid, making it easier to visualize changes between days.
			 */
			
			exec.setMessage("Normalizing...");
			Iterator<GridSlot> normalizing = grid.iterator();
			while(normalizing.hasNext()) {
				GridSlot slot = normalizing.next();
				slot.normalizeForDay(forNormalizing);
			}
			
			
			/**
			 * Now we construct the results tables.
			 */
			int simpleEntryCounter = 0;
			int complexEntryCounter = 0;
			int gridSlotNumber = 1;
			
			Iterator<GridSlot> i = grid.iterator();
			while (i.hasNext()) {
				exec.setProgress(gridSlotNumber/(double)(nRows*nColumns), "Constructing output...");
				GridSlot gs = i.next();
				HashMap<LocalDate, HitsInDay> visits = gs.getVisits();
				for(LocalDate ld: visits.keySet()) {
					DetailedDataEntry de = new DetailedDataEntry(ld,gridSlotNumber ,gs.getTheGeom(), visits.get(ld));
					addComplexDataEntry(de, complexEntryCounter, complexContainer);
					complexEntryCounter++;
				}
				SimpleDataEntry se = new SimpleDataEntry(gridSlotNumber, gs);
				addSimpleDataEntry(se, simpleEntryCounter, simpleContainer);
				simpleEntryCounter++;
				gridSlotNumber++;
			}
			
			
			
			
			exec.checkCanceled();		

			/*
			 * Once we are done, we close the container and return its table. Here we need
			 * to return as many tables as we specified in the constructor. This node has
			 * one output, hence return one table (wrapped in an array of tables).
			 */
			complexContainer.close();
			BufferedDataTable out1 = complexContainer.getTable();
			simpleContainer.close();
			BufferedDataTable out2 = simpleContainer.getTable();
			return new BufferedDataTable[] { out1, out2 };
		}

		private void addComplexDataEntry(DetailedDataEntry dEntry, int complexEntryCounter,
				BufferedDataContainer complexContainer) {
				List<DataCell> complexCells = new ArrayList<>();
				DataCell idCell = new StringCell(dEntry.getGridId());
				complexCells.add(idCell);
				DataCell dayCell = null;
				try {
					dayCell = DateAndTimeCell.fromString(dEntry.getDay().toString().replace(" ", "T"));
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				complexCells.add(dayCell);
				
				//Day
				DataCell nDayCell = new IntCell(dEntry.getnVisitsInDay());
				complexCells.add(nDayCell);

				//Early Morning
				DataCell nEmCell = new IntCell(dEntry.getnVisitsEM());
				complexCells.add(nEmCell);

				//Mid Morning
				DataCell nMmCell = new IntCell(dEntry.getnVisitsMM());
				complexCells.add(nMmCell);

				//Afternoon
				DataCell nACell = new IntCell(dEntry.getnVisistsA());
				complexCells.add(nACell);
			
				//Evening
				DataCell nECell = new IntCell(dEntry.getnVisistsE());
				complexCells.add(nECell);
				
				DataCell theGeom = new StringCell(dEntry.getTheGeom());
				complexCells.add(theGeom);
				DataRow row = new DefaultRow("N " + complexEntryCounter,complexCells);
				try {
					complexContainer.addRowToTable(row);
				}catch(Exception e) {
					System.out.println(e.getMessage());
				}

			return;		
		}

		private void addSimpleDataEntry(SimpleDataEntry se, int simpleEntryCounter, BufferedDataContainer simpleContainer) {
			List<DataCell> simpleCells = new ArrayList<>();
			DataCell gridId = new StringCell(se.getGridId());
			simpleCells.add(gridId);
			DataCell nV = new IntCell(se.getTotalVisit());
			simpleCells.add(nV);
			DataCell theGeom = new StringCell(se.getTheGeom());
			simpleCells.add(theGeom);
			DataRow row = new DefaultRow("N " + simpleEntryCounter,simpleCells);
			try {
				simpleContainer.addRowToTable(row);
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
			try {
				double minLat = Double.parseDouble(m_minLat.getStringValue());
				double maxLat = Double.parseDouble(m_maxLat.getStringValue());
				double minLon = Double.parseDouble(m_minLon.getStringValue());
				double maxLon = Double.parseDouble(m_maxLon.getStringValue());
				if(minLat>= maxLat) {
					throw new InvalidSettingsException("The minimum Latitude is bigger or equal than the maximum Latitude!");
				}else if(minLon>= maxLon) {
					throw new InvalidSettingsException("The minimum Longitude is bigger or equal than the maximum Longitude!");
				}				
			}catch(NumberFormatException e) {
				throw new InvalidSettingsException("The inserted coordinates are not numbers!");
			}
					
			if(!(inSpecs[0].containsName("owner_id") && 
					 inSpecs[0].containsName("the_geom") && 
					 inSpecs[0].containsName("begin_at") && 
					 inSpecs[0].containsName("end_at"))) {
					if(!(inSpecs[0].getColumnSpec("owner_id").getType().getCellClass().equals(IntCell.TYPE) &&
						 inSpecs[0].getColumnSpec("the_geom").getType().getCellClass().equals(StringCell.TYPE) && 
						 inSpecs[0].getColumnSpec("begin_at").getType().getCellClass().equals(DateAndTimeCell.TYPE) &&
						 inSpecs[0].getColumnSpec("end_at").getType().getCellClass().equals(DateAndTimeCell.TYPE))) {
						throw new InvalidSettingsException("The input columns must have the following type: owner_id-Integer, begin_at-DateTime, end_at-DateTime, the_geom-String");
					}
					throw new InvalidSettingsException("The input table must contain the following columns: owner_id, begin_at, end_at, the_geom");
				}
			
			return new DataTableSpec[] { createOutputForDetailed(),createOutputForSimple() };
		}

		/**
		 * Creates the output table spec from the input spec. For each double column in
		 * the input, one String column will be created containing the formatted double
		 * value as String.
		 * 
		 * @param inputTableSpec
		 * @return
		 */
		private DataTableSpec createOutputForDetailed() {
			List<DataColumnSpec> newColumnSpecs = new ArrayList<>();
			DataColumnSpecCreator specCreator = new DataColumnSpecCreator("GridSlot", StringCell.TYPE);
			newColumnSpecs.add(specCreator.createSpec());
			specCreator = new DataColumnSpecCreator("Day", DateAndTimeCell.TYPE);
			newColumnSpecs.add(specCreator.createSpec());
			specCreator = new DataColumnSpecCreator("N° Visits(Day)", IntCell.TYPE);
			newColumnSpecs.add(specCreator.createSpec());
			specCreator = new DataColumnSpecCreator("N° Visits("+TimeSlot.EARLY_MORNING.toString()+")", IntCell.TYPE);
			newColumnSpecs.add(specCreator.createSpec());
			specCreator = new DataColumnSpecCreator("N° Visits("+TimeSlot.MID_MORNING.toString()+")", IntCell.TYPE);
			newColumnSpecs.add(specCreator.createSpec());
			specCreator = new DataColumnSpecCreator("N° Visits("+TimeSlot.AFTERNOON.toString()+")", IntCell.TYPE);
			newColumnSpecs.add(specCreator.createSpec());
			specCreator = new DataColumnSpecCreator("N° Visits("+TimeSlot.EVENING.toString()+")", IntCell.TYPE);
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
			DataColumnSpec[]  complexColumnSpecsArray = newColumnSpecs.toArray(new DataColumnSpec[newColumnSpecs.size()]);
			
			return new DataTableSpec(complexColumnSpecsArray);
		}
		
		private DataTableSpec createOutputForSimple() {
			List<DataColumnSpec> simpleColumnSpecs = new ArrayList<>();
			DataColumnSpecCreator specCreator = new DataColumnSpecCreator("GridSlot", StringCell.TYPE);
			simpleColumnSpecs.add(specCreator.createSpec());
			specCreator = new DataColumnSpecCreator("N° Visits", IntCell.TYPE);
			simpleColumnSpecs.add(specCreator.createSpec());
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
			simpleColumnSpecs.add(specCreator.createSpec());
			DataColumnSpec[]  simpleColumnSpecsArray = simpleColumnSpecs.toArray(new DataColumnSpec[simpleColumnSpecs.size()]);
			return new DataTableSpec(simpleColumnSpecsArray);
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
			m_minLat.saveSettingsTo(settings);
			m_maxLat.saveSettingsTo(settings);
			m_minLon.saveSettingsTo(settings);
			m_maxLon.saveSettingsTo(settings);
			m_numberOfRows.saveSettingsTo(settings);
			m_numberOfColumns.saveSettingsTo(settings);
			m_minutes.saveSettingsTo(settings);
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
			m_minLat.loadSettingsFrom(settings);
			m_maxLat.loadSettingsFrom(settings);
			m_minLon.loadSettingsFrom(settings);
			m_maxLon.loadSettingsFrom(settings);
			m_numberOfRows.loadSettingsFrom(settings);
			m_numberOfColumns.loadSettingsFrom(settings);
			m_minutes.loadSettingsFrom(settings);

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
			m_minLat.validateSettings(settings);
			m_maxLat.validateSettings(settings);
			m_minLon.validateSettings(settings);
			m_maxLon.validateSettings(settings);
			m_numberOfRows.validateSettings(settings);
			m_numberOfColumns.validateSettings(settings);
			m_minutes.validateSettings(settings);

			
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

