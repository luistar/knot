package org.unina.spatialanalysis.RouteStepAnalyzer;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JList;
import javax.swing.JTable;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.renderer.DataValueRenderer;
import org.knime.core.data.renderer.DataValueRendererFactory;
import org.knime.core.data.renderer.DoubleValueRenderer;
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
import org.unina.spatialanalysis.RouteStepAnalyzer.RouteStepAnalyzerNodeModel;
import org.unina.spatialanalysis.RouteStepAnalyzer.entity.DetailedDataEntry;
import org.unina.spatialanalysis.RouteStepAnalyzer.entity.SimpleDataEntry;
import org.unina.spatialanalysis.RouteStepAnalyzer.entity.routesteps.Hit;
import org.unina.spatialanalysis.RouteStepAnalyzer.entity.routesteps.HitHolder;
import org.unina.spatialanalysis.RouteStepAnalyzer.entity.routesteps.Segment;
import org.unina.spatialanalysis.RouteStepAnalyzer.entity.supportstructures.SegmentHolder;
import org.unina.spatialanalysis.RouteStepAnalyzer.entity.visit.TimeSlot;


/**
 * This is an example implementation of the node model of the
 * "RouteStepAnalyzer" node.
 * 
 * This example node performs simple number formatting
 * ({@link String#format(String, Object...)}) using a user defined format string
 * on all double columns of its input table.
 *
 * @author Sinogrante Principe
 */
@SuppressWarnings("deprecation")
public class RouteStepAnalyzerNodeModel extends NodeModel {
	
	private static final String MIN_TIME_SETTINGS = "m_min_time";
	private static final int DEFAULT_MIN_TIME_SETTINGS = 0;
	
    
	private static final NodeLogger LOGGER = NodeLogger.getLogger(RouteStepAnalyzerNodeModel.class);
	
	private final SettingsModelIntegerBounded m_minTimeBetween = createMinTimeSettings();

	
	public static SettingsModelIntegerBounded createMinTimeSettings() {
		return new SettingsModelIntegerBounded(MIN_TIME_SETTINGS, DEFAULT_MIN_TIME_SETTINGS, 0, Integer.MAX_VALUE);
	}
	
	
	/**
	 * Constructor for the node model.
	 */
	protected RouteStepAnalyzerNodeModel() {
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
	
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		
		BufferedDataTable inputTable = inData[0];
		DataTableSpec complexOutputSpec = createOutputForDetailed();
		BufferedDataContainer complexContainer = exec.createDataContainer(complexOutputSpec);
		DataTableSpec simpleOutputSpec = createOutputForSimple();
		BufferedDataContainer simpleContainer = exec.createDataContainer(simpleOutputSpec);

		CloseableRowIterator rowIterator = inputTable.iterator();
		int currentRowCounter = 0;
		int idIndex = -1;
		int beginAtIndex = -1;
		int theGeomIndex =-1;
		int endAtIndex= -1;
		int originIdIndex = -1;
		int destinationIdIndex = -1;
		int tagsIndex = -1;
		
		SegmentHolder neverVisitedSegmentHolder = new SegmentHolder();
		SegmentHolder visitedSegmentHolder = new SegmentHolder();
		
		Set<LocalDate> daysInDataSet = new HashSet<LocalDate>();

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
			}else if(columnspec.getName().equals("origin_id")) {
				originIdIndex = i;
			}else if(columnspec.getName().equals("destination_id")) {
				destinationIdIndex = i;
			}else if(columnspec.getName().equals("tags")) {
				tagsIndex=i;
			}
		}
		

		while(rowIterator.hasNext()) {
			DataRow currentRow = rowIterator.next();
			currentRowCounter++;
			
			int ownerId = Integer.MIN_VALUE;
			LocalDateTime beginAt = null;
			LocalDateTime endAt = null;
			long originId = Long.MIN_VALUE;
			long destinationId = Long.MIN_VALUE;
			String tags = "";
			String theGeom = null;
			String rowId = null;
			
			rowId = currentRow.getKey().getString();
			
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
			
			cell = currentRow.getCell(originIdIndex);
			if(cell.getType().getCellClass().equals((LongCell.class))) {
				LongCell longCell = (LongCell) cell;
				originId = longCell.getLongValue();
			}
			
			cell = currentRow.getCell(destinationIdIndex);
			if(cell.getType().getCellClass().equals((LongCell.class))) {
				LongCell longCell = (LongCell) cell;
				destinationId = longCell.getLongValue();
			}
			
			cell = currentRow.getCell(tagsIndex);
			if(cell.getType().getCellClass().equals(StringCell.class)) {
				StringCell tagCell = (StringCell) cell;
				tags = tagCell.getStringValue();
			}
			
				
			cell = currentRow.getCell(theGeomIndex);
			if(cell.getType().getCellClass().equals(StringCell.class)) {
				StringCell theGeomCell = (StringCell) cell;
				theGeom = theGeomCell.getStringValue();
			}
			
			if(rowId.contains("NEVER_VISITED")) {
				Segment	s = new Segment(originId, destinationId, tags, theGeom, m_minTimeBetween.getIntValue());
				neverVisitedSegmentHolder.addSegment(s);
			}else if(ownerId==Integer.MIN_VALUE || theGeom==null || beginAt== null || endAt==null || originId==Long.MIN_VALUE || destinationId == Long.MIN_VALUE) {
				LOGGER.info("Row n " + currentRowCounter + " has invalid values! It will be skipped.\n");
			}else {
				Segment s;
				if(visitedSegmentHolder.containsSegment(originId, destinationId)) {
					s = visitedSegmentHolder.getSegment(originId, destinationId);
				}else {
					s = new Segment(originId, destinationId, tags, theGeom, m_minTimeBetween.getIntValue());
					visitedSegmentHolder.addSegment(s);
				}
				daysInDataSet.add(beginAt.toLocalDate());
				s.addHit(new Hit(beginAt, endAt, ownerId));
			}
			exec.setProgress((currentRowCounter/(double)inputTable.size()), "Processing input data...");
		}
		

		int simpleEntryCounter = 0;
		int complexEntryCounter = 0;
		int visitedSize = visitedSegmentHolder.getAllSegments().size();
		int neverVisitedSize = neverVisitedSegmentHolder.getAllSegments().size();
		
		int counter = 0;
		

		for(Segment s: visitedSegmentHolder.getAllSegments()) {
			s.generateResults();
			HashMap<LocalDate, HitHolder> tmp = s.getData();
			for(LocalDate ld: daysInDataSet) {
				if(tmp.containsKey(ld)) {
					HitHolder hitHolder = tmp.get(ld);
					DetailedDataEntry dEntry = new DetailedDataEntry(ld, s, hitHolder);
					addComplexDataEntry(dEntry, complexEntryCounter, complexContainer);
					complexEntryCounter++;
				}else {
					DetailedDataEntry dEntry = new DetailedDataEntry(ld, s, new HitHolder());
					addComplexDataEntry(dEntry, complexEntryCounter, complexContainer);
					complexEntryCounter++;
				}
			}
			SimpleDataEntry sEntry = new SimpleDataEntry(s);
			addSimpleDataEntry(sEntry, simpleEntryCounter, simpleContainer);
			simpleEntryCounter++;
			double progressValue = counter++/(double) visitedSize;
			exec.setProgress(progressValue, "Adding results..." + counter);
		}
		
		counter = 0;

		for(Segment s: neverVisitedSegmentHolder.getAllSegments()) {
			s.generateResults();
			for(LocalDate ld: daysInDataSet) {
				DetailedDataEntry dEntry = new DetailedDataEntry(ld, s, new HitHolder());
				addComplexDataEntry(dEntry, complexEntryCounter, complexContainer);
				complexEntryCounter++;
			}
			SimpleDataEntry sEntry = new SimpleDataEntry(s);
			addSimpleDataEntry(sEntry, simpleEntryCounter, simpleContainer);
			simpleEntryCounter++;
			double progressValue = counter++/(double) neverVisitedSize;
			exec.setProgress(progressValue, "Adding results..." + counter);
		}
		
		exec.checkCanceled();
	
		
		complexContainer.close();
		BufferedDataTable out1 = complexContainer.getTable();
		simpleContainer.close();
		BufferedDataTable out2 = simpleContainer.getTable();
		return new BufferedDataTable[] { out1, out2 };
	}

	private void addComplexDataEntry(DetailedDataEntry dEntry, int complexEntryCounter,
			BufferedDataContainer complexContainer) {
			List<DataCell> complexCells = new ArrayList<>();
			DataCell idOriginCell = new LongCell(dEntry.getOriginId());
			complexCells.add(idOriginCell);
			DataCell idDestinationCell = new LongCell(dEntry.getDestinationId());
			complexCells.add(idDestinationCell);
			DataCell dayCell = null;
			try {
				dayCell = DateAndTimeCell.fromString(dEntry.getDay().toString().replace(" ", "T"));
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			complexCells.add(dayCell);
			//Day
			DataCell nDayCell = new IntCell(dEntry.getnVisitsInDay());
			complexCells.add(nDayCell);
			DataCell avgDayCell;
			if(dEntry.getAvgForDay()!=0) {
				 avgDayCell = new DoubleCell(dEntry.getAvgForDay());
			}else {
				 avgDayCell = DataType.getMissingCell();
			}
			complexCells.add(avgDayCell);
			
			DataCell medianDay;
			if(dEntry.getMedianForDay()!=0) {
				medianDay= new DoubleCell(dEntry.getMedianForDay());
			}else {
				medianDay = DataType.getMissingCell();
			}
			complexCells.add(medianDay);
			
			//Early Morning
			DataCell nEmCell = new IntCell(dEntry.getnVisitsEM());
			complexCells.add(nEmCell);
			DataCell avgEmCell; 
			if(dEntry.getAvgForEM()!=0) {
				avgEmCell= new DoubleCell(dEntry.getAvgForEM());
			}else {
				avgEmCell = DataType.getMissingCell();
			}
			complexCells.add(avgEmCell);
			DataCell medianEm;
			if(dEntry.getMedianForEM()!=0) {
				medianEm= new DoubleCell(dEntry.getMedianForEM());
			}else {
				medianEm = DataType.getMissingCell();
			}
			complexCells.add(medianEm);
			
			
			//Mid Morning
			DataCell nMmCell = new IntCell(dEntry.getnVisitsMM());
			complexCells.add(nMmCell);
			DataCell avgMmCell;
			if(dEntry.getAvgForMM()!=0) {
				avgMmCell = new DoubleCell(dEntry.getAvgForMM());
			}else {
				avgMmCell = DataType.getMissingCell();
			}
			complexCells.add(avgMmCell);
			DataCell medianMm;
			if(dEntry.getMedianForMM()!=0) {
				medianMm= new DoubleCell(dEntry.getMedianForMM());
			}else {
				medianMm = DataType.getMissingCell();
			}
			complexCells.add(medianMm);
			
			//Afternoon
			DataCell nACell = new IntCell(dEntry.getnVisistsA());
			complexCells.add(nACell);
			DataCell avgACell;
			if(dEntry.getAvgForA()!=0) {
				avgACell = new DoubleCell(dEntry.getAvgForA());
			}else {
				avgACell = DataType.getMissingCell();
			}
			complexCells.add(avgACell);
			DataCell medianA;
			if(dEntry.getMedianForDay()!=0) {
				medianA= new DoubleCell(dEntry.getMedianForA());
			}else {
				medianA = DataType.getMissingCell();
			}
			complexCells.add(medianA);
			
			
			//Evening
			DataCell nECell = new IntCell(dEntry.getnVisistsE());
			complexCells.add(nECell);
			DataCell avgECell;
			if(dEntry.getAvgForE()!=0) {
				avgECell = new DoubleCell(dEntry.getAvgForE());
			}else {
				avgECell = DataType.getMissingCell();
			}
			complexCells.add(avgECell);
			DataCell medianE;
			if(dEntry.getMedianForE()!=0) {
				medianE= new DoubleCell(dEntry.getMedianForE());
			}else {
				medianE = DataType.getMissingCell();
			}
			complexCells.add(medianE);
			
			
			DataCell tags = new StringCell(dEntry.getTags());
			complexCells.add(tags);
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
		DataCell idOriginCell = new LongCell(se.getOriginId());
		simpleCells.add(idOriginCell);
		DataCell idDestinationCell = new LongCell(se.getDestinationId());
		simpleCells.add(idDestinationCell);
		DataCell nV = new IntCell(se.getTotalVisit());
		simpleCells.add(nV);
		DataCell avg;
		if(se.getAvgTime()!=0) {
			avg = new DoubleCell(se.getAvgTime());
		}else {
			avg = DataType.getMissingCell();
		}
		simpleCells.add(avg);

		DataCell median;
		if(se.getMedianTime()!=0) {
			median= new DoubleCell(se.getMedianTime());
		}else {
			median = DataType.getMissingCell();
		}
		simpleCells.add(median);
		DataCell tags = new StringCell(se.getTags());
		simpleCells.add(tags);
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
		 		
		if(!(inSpecs[0].containsName("owner_id") && 
			 inSpecs[0].containsName("the_geom") && 
			 inSpecs[0].containsName("begin_at") && 
			 inSpecs[0].containsName("end_at") && 
			 inSpecs[0].containsName("origin_id") &&
			 inSpecs[0].containsName("destination_id") &&
			 inSpecs[0].containsName("tags"))) {
			if(!(inSpecs[0].getColumnSpec("owner_id").getType().getCellClass().equals(IntCell.TYPE) &&
				 inSpecs[0].getColumnSpec("the_geom").getType().getCellClass().equals(StringCell.TYPE) && 
				 inSpecs[0].getColumnSpec("begin_at").getType().getCellClass().equals(DateAndTimeCell.TYPE) &&
				 inSpecs[0].getColumnSpec("end_at").getType().getCellClass().equals(DateAndTimeCell.TYPE) &&	
				 inSpecs[0].getColumnSpec("origin_id").getType().getCellClass().equals(LongCell.TYPE) &&
				 inSpecs[0].getColumnSpec("destination_id").getType().getCellClass().equals(LongCell.TYPE) &&
				 inSpecs[0].getColumnSpec("tags").getType().getCellClass().equals(StringCell.TYPE) )) {
				throw new InvalidSettingsException("The input columns must have the following type: owner_id-Integer, begin_at-DateTime, end_at-DateTime, origin_id-Long, destination_id-Long, origin_tags-String, destination_tags-String, the_geom-String");
			}
			throw new InvalidSettingsException("The input table must contain the following columns: owner_id, begin_at, end_at, origin_id, destination_id, origin_tags, destination_tags, the_geom");
		}
		
		if(m_minTimeBetween.getIntValue()<0) {
			throw new InvalidSettingsException("The minimum time between visits cannot be negative!");

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
		DataColumnSpecCreator specCreator = new DataColumnSpecCreator("OriginId", LongCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("DestinationId", LongCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("Day", DateAndTimeCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		
		specCreator = new DataColumnSpecCreator("N° Visits(Day)", IntCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("AvgTime(Day)", DoubleCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("MedianTime(Day)", DoubleCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		
		specCreator = new DataColumnSpecCreator("N° Visits("+TimeSlot.EARLY_MORNING.toString()+")", IntCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("AvgTime("+TimeSlot.EARLY_MORNING.toString()+")", DoubleCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("MedianTime("+TimeSlot.EARLY_MORNING.toString()+")", DoubleCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		
		specCreator = new DataColumnSpecCreator("N° Visits("+TimeSlot.MID_MORNING.toString()+")", IntCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("AvgTime("+TimeSlot.MID_MORNING.toString()+")", DoubleCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("MedianTime("+TimeSlot.MID_MORNING.toString()+")", DoubleCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		
		specCreator = new DataColumnSpecCreator("N° Visits("+TimeSlot.AFTERNOON.toString()+")", IntCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("AvgTime("+TimeSlot.AFTERNOON.toString()+")", DoubleCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("MedianTime("+TimeSlot.AFTERNOON.toString()+")", DoubleCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		
		specCreator = new DataColumnSpecCreator("N° Visits("+TimeSlot.EVENING.toString()+")", IntCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("AvgTime("+TimeSlot.EVENING.toString()+")", DoubleCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("MedianTime("+TimeSlot.EVENING.toString()+")", DoubleCell.TYPE);
		newColumnSpecs.add(specCreator.createSpec());
		
		specCreator = new DataColumnSpecCreator("Tags", StringCell.TYPE);
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
		DataColumnSpecCreator specCreator = new DataColumnSpecCreator("OriginId", LongCell.TYPE);
		simpleColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("DestinationId", LongCell.TYPE);
		simpleColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("N° Visits", IntCell.TYPE);
		simpleColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("Avg time Between", DoubleCell.TYPE);
		simpleColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("Median time", DoubleCell.TYPE);
		simpleColumnSpecs.add(specCreator.createSpec());
		specCreator = new DataColumnSpecCreator("Tags", StringCell.TYPE);
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
		m_minTimeBetween.saveSettingsTo(settings);
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
		m_minTimeBetween.loadSettingsFrom(settings);

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
		m_minTimeBetween.validateSettings(settings);

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

