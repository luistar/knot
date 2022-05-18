package org.unina.spatialanalysis.wktgeometryfilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CloseableRowIterator;
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
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import mil.nga.sf.Geometry;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.wkt.GeometryReader;


/**
 * This is an example implementation of the node model of the
 * "RouteFilter" node.
 * 
 * This example node performs simple number formatting
 * ({@link String#format(String, Object...)}) using a user defined format string
 * on all double columns of its input table.
 *
 * @author Roland Varriale
 */
public class WKTGeometryFilterNodeModel extends NodeModel {

	/**
	 * The logger is used to print info/warning/error messages to the KNIME console
	 * and to the KNIME log file. Retrieve it via 'NodeLogger.getLogger' providing
	 * the class of this node model.
	 */
	private static final NodeLogger LOGGER = NodeLogger.getLogger(WKTGeometryFilterNodeModel.class);

	/**
	 * The settings key to retrieve and store settings shared between node dialog
	 * and node model. In this case, the key for the number format String that
	 * should be entered by the user in the dialog.
	 */

	private static final String PAIR_TYPE_FORMAT = "m_pair_type";
	
	private static final String DEFAULT_PAIR_TYPE_FORMAT = "{lat,lon}";
	
	private static final String GEOMETRY_COL = "m_col_geometry";
	
	private static final String TOP_LEFT_CORNER_LON = "m_top_left_corner_lon";
	
	private static final String TOP_LEFT_CORNER_LAT = "m_top_left_corner_lat";
	
	private static final String BOTTOM_RIGHT_CORNER_LON = "m_bottom_right_corner_lon";
	
	private static final String BOTTOM_RIGHT_CORNER_LAT = "m_bottom_right_corner_lat";
	
	/**
	 * The settings model to manage the type of coordinate pairs provided to the node,
	 * either {lon,lat} or {lat,lon}.
	 **/
	private final SettingsModelString m_pairTypeSettings = createCoordinatePairTypeSettings();
		
	/**
	 * The settings model to manage the shared settings. This model will hold the
	 * value entered by the user in the dialog and will update once the user changes
	 * the value. Furthermore, it provides methods to easily load and save the value
	 * to and from the shared settings (see:
	 * <br>
	 * {@link #loadValidatedSettingsFrom(NodeSettingsRO)},
	 * {@link #saveSettingsTo(NodeSettingsWO)}). 
	 * <br>
	 * Here, we use a SettingsModelString as the number format is a String. 
	 * There are models for all common data types. Also have a look at the comments 
	 * in the constructor of the {@link WKTGeometryFilterNodeDialog} as the settings 
	 * models are also used to create simple dialogs.
	 */
	private final SettingsModelColumnName m_geometryColSettings = createGeometryColModel();

	private final SettingsModelDouble m_topLeftCornerLonSettings = createTopLeftCornerLonModel();
	
	private final SettingsModelDouble m_topLeftCornerLatSettings = createTopLeftCornerLatModel();
	
	private final SettingsModelDouble m_bottomRightCornerLonSettings = createBottomRightCornerLonModel();
	
	private final SettingsModelDouble m_bottomRightCornerLatSettings = createBottomRightCornerLatModel();
		
	/**
	 * Geometry column
	 * @return
	 */
	static SettingsModelColumnName createGeometryColModel()  {
		SettingsModelColumnName geometryColSettingModel = new SettingsModelColumnName(GEOMETRY_COL, null);
		geometryColSettingModel.setEnabled(true);
		return geometryColSettingModel;
	}
	

	/**
	 * Top Left Corner Lon
	 */
	static SettingsModelDouble createTopLeftCornerLonModel()  {
		SettingsModelDouble topLeftCornerLonModel = new SettingsModelDouble(TOP_LEFT_CORNER_LON, 0);
		topLeftCornerLonModel.setEnabled(true);
		return topLeftCornerLonModel;
	}
	

	/**
	 * Top Left Corner Lat
	 */
	static SettingsModelDouble createTopLeftCornerLatModel()  {
		SettingsModelDouble topLeftCornerLatModel = new SettingsModelDouble(TOP_LEFT_CORNER_LAT, 0);
		topLeftCornerLatModel.setEnabled(true);
		return topLeftCornerLatModel;
	}
	
	/**
	 * Bottom Right Corner Lon
	 */
	static SettingsModelDouble createBottomRightCornerLonModel()  {
		SettingsModelDouble bottomRightCornerLonModel = new SettingsModelDouble(BOTTOM_RIGHT_CORNER_LON, 0);
		bottomRightCornerLonModel.setEnabled(true);
		return bottomRightCornerLonModel;
	}
		
	/**
	 * Bottom Right Corner Lat
	 */
	static SettingsModelDouble createBottomRightCornerLatModel()  {
		SettingsModelDouble bottomRightCornerLatModel = new SettingsModelDouble(BOTTOM_RIGHT_CORNER_LAT, 0);
		bottomRightCornerLatModel.setEnabled(true);
		return bottomRightCornerLatModel;
	}
	
	static SettingsModelString createCoordinatePairTypeSettings() {
		SettingsModelString coordinatePairTypeSettings = new SettingsModelString(PAIR_TYPE_FORMAT, DEFAULT_PAIR_TYPE_FORMAT);
		coordinatePairTypeSettings.setEnabled(true);
		return coordinatePairTypeSettings;
	}
	
	/**
	 * Constructor for the node model.
	 */
	protected WKTGeometryFilterNodeModel() {
		/**
		 * Here we specify how many data input and output tables the node should have.
		 * In this case its one input and one output table.
		 */
		super(1, 1);
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		/*
		 * The functionality of the node is implemented in the execute method. This
		 * implementation will format each double column of the input table using a user
		 * provided format String. The output will be one String column for each double
		 * column of the input containing the formatted number from the input table. For
		 * simplicity, all other columns are ignored in this example.
		 */


		/*
		 * The input data table to work with. The "inData" array will contain as many
		 * input tables as specified in the constructor. In this case it can only be one
		 * (see constructor).
		 */
		BufferedDataTable inputTable = inData[0];

		/*
		 * Create the spec of the output table, for each double column of the input
		 * table we will create one formatted String column in the output. See the
		 * javadoc of the "createOutputSpec(...)" for more information.
		 */
		DataTableSpec outputSpec = createOutputSpec(inputTable.getDataTableSpec());

		/*
		 * The execution context provides storage capacity, in this case a
		 * data container to which we will add rows sequentially. Note, this container
		 * can handle arbitrary big data tables, it will buffer to disc if necessary.
		 * The execution context is provided as an argument to the execute method by the
		 * framework. Have a look at the methods of the "exec". There is a lot of
		 * functionality to create and change data tables.
		 */
		BufferedDataContainer container = exec.createDataContainer(outputSpec);

		/*
		 * Get the row iterator over the input table which returns each row one-by-one
		 * from the input table.
		 */
		CloseableRowIterator rowIterator = inputTable.iterator();

		int lineStringIndex = -1;
		
		DataTableSpec specs = inputTable.getDataTableSpec();
		String colNameLineString = m_geometryColSettings.getStringValue();
		
		for(int i = 0; i<specs.getNumColumns(); i++) {
			DataColumnSpec columnspec = specs.getColumnSpec(i);
			if(columnspec.getName().equals(colNameLineString)) {
				lineStringIndex = i;
			}
		}
		
		/*
		 * A counter for how many rows have already been processed. This is used to
		 * calculate the progress of the node, which is displayed as a loading bar under
		 * the node icon.
		 */
		Double topLeftCornerLon = m_topLeftCornerLonSettings.getDoubleValue();
		Double topLeftCornerLat = m_topLeftCornerLatSettings.getDoubleValue();
		
		Double bottomRightCornerLon = m_bottomRightCornerLonSettings.getDoubleValue();
		Double bottomRightCornerLat = m_bottomRightCornerLatSettings.getDoubleValue();

		String pairTypeFormat = m_pairTypeSettings.getStringValue();

		GeometryEnvelope selectedAreaEnvelope = new GeometryEnvelope();

		if(pairTypeFormat.equals(DEFAULT_PAIR_TYPE_FORMAT)) { // lat, lon
			selectedAreaEnvelope.setMinY(topLeftCornerLon);
			selectedAreaEnvelope.setMaxX(topLeftCornerLat);
			
			selectedAreaEnvelope.setMaxY(bottomRightCornerLon);
			selectedAreaEnvelope.setMinX(bottomRightCornerLat);
		} else { // lon, lat 
			selectedAreaEnvelope.setMaxY(topLeftCornerLat);
			selectedAreaEnvelope.setMinX(topLeftCornerLon);
			
			selectedAreaEnvelope.setMinY(bottomRightCornerLat);
			selectedAreaEnvelope.setMaxX(bottomRightCornerLon);
		}
		
		int currentRowCounter = 0;
		
		// Iterate over the rows of the input table.
		while (rowIterator.hasNext()) {
			DataRow currentRow = rowIterator.next();
			
			// Recupero la linestring e la converto in un array di point(x,y);
	
			DataCell cell = currentRow.getCell(lineStringIndex);
			if(cell.getType().getCellClass().equals(StringCell.class)) {
				StringCell geometryStringCell = (StringCell) cell;
				String rowGeometryCellString = geometryStringCell.getStringValue();
				
				// Convert row cell geom from string to GeometryEnvelope
				Geometry rowGeometry = GeometryReader.readGeometry(rowGeometryCellString);
				GeometryEnvelope rowGeometryEnv = rowGeometry.getEnvelope();
			
				// Nel caso i punti escano fuori dal rettangolo ignoro la riga
				if(selectedAreaEnvelope.contains(rowGeometryEnv)) {
					container.addRowToTable(currentRow);
				}
			}
			
			// We finished processing one row, hence increase the counter
			currentRowCounter++;

			/*
			 * Here we check if a user triggered a cancel of the node. If so, this call will
			 * throw an exception and the execution will stop. This should be done
			 * frequently during execution, e.g. after the processing of one row if
			 * possible.
			 */
			exec.checkCanceled();

			/*
			 * Calculate the percentage of execution progress and inform the
			 * ExecutionMonitor. Additionally, we can set a message what the node is
			 * currently doing (the message will be displayed as a tooltip when hovering
			 * over the progress bar of the node). This is especially useful to inform the
			 * user about the execution status for long running nodes.
			 */
			exec.setProgress(currentRowCounter / (double) inputTable.size(), "Checking row " + currentRowCounter);
		}

		/*
		 * Once we are done, we close the container and return its table. Here we need
		 * to return as many tables as we specified in the constructor. This node has
		 * one output, hence return one table (wrapped in an array of tables).
		 */
		container.close();
		BufferedDataTable out = container.getTable();
		return new BufferedDataTable[] { out };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		LOGGER.info("Invoke configure");

		/*
		 * Check if the node is executable, e.g. all required user settings are
		 * available and valid, or the incoming types are feasible for the node to
		 * execute. In case the node can execute in its current configuration with the
		 * current input, calculate and return the table spec that would result of the
		 * execution of this node. I.e. this method precalculates the table spec of the
		 * output table.
		*/
			
		String colIDName = m_geometryColSettings.getColumnName();

		if(colIDName == null) {
			LOGGER.info("All columns must be selected in the configuration dialog");
			throw new InvalidSettingsException("All columns must be selected in the configuration dialog");
		}
		
		/*
		 * Similar to the return type of the execute method, we need to return an array
		 * of DataTableSpecs with the length of the number of outputs ports of the node
		 * (as specified in the constructor). The resulting table created in the execute
		 * methods must match the spec created in this method. As we will need to
		 * calculate the output table spec again in the execute method in order to
		 * create a new data container, we create a new method to do that.
		 */
		DataTableSpec inputTableSpec = inSpecs[0];
		return new DataTableSpec[] { createOutputSpec(inputTableSpec) };
	}

	/**
	 * Creates the output table spec from the input spec. For each double column in
	 * the input, one String column will be created containing the formatted double
	 * value as String.
	 * 
	 * @param inputTableSpec
	 * @return
	 */
	private DataTableSpec createOutputSpec(DataTableSpec inputTableSpec) {
 		List<DataColumnSpec> newColumnSpecs = new ArrayList<>();
		
 		// Iterate over the input column specs
		for (int i = 0; i < inputTableSpec.getNumColumns(); i++) {
			DataColumnSpec columnSpec = inputTableSpec.getColumnSpec(i);
			newColumnSpecs.add(columnSpec);
		}

		// Create and return a new DataTableSpec from the list of DataColumnSpecs.
		DataColumnSpec[] newColumnSpecsArray = newColumnSpecs.toArray(new DataColumnSpec[newColumnSpecs.size()]);
		return new DataTableSpec(newColumnSpecsArray);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		LOGGER.info("Invoke saveSettingsTo");
		
		/*
		 * Save user settings to the NodeSettings object. SettingsModels already know how to
		 * save them self to a NodeSettings object by calling the below method. In general,
		 * the NodeSettings object is just a key-value store and has methods to write
		 * all common data types. Hence, you can easily write your settings manually.
		 * See the methods of the NodeSettingsWO.
		 */
		m_pairTypeSettings.saveSettingsTo(settings);

		m_geometryColSettings.saveSettingsTo(settings);
		m_topLeftCornerLonSettings.saveSettingsTo(settings);
		m_topLeftCornerLatSettings.saveSettingsTo(settings);
		m_bottomRightCornerLonSettings.saveSettingsTo(settings);
		m_bottomRightCornerLatSettings.saveSettingsTo(settings);		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		LOGGER.info("Invoke loadValidatedSettingsFrom");
		
		/*
		 * Load (valid) settings from the NodeSettings object. It can be safely assumed that
		 * the settings are validated by the method below.
		 * 
		 * The SettingsModel will handle the loading. After this call, the current value
		 * (from the view) can be retrieved from the settings model.
		 */
		m_pairTypeSettings.loadSettingsFrom(settings);

		m_geometryColSettings.loadSettingsFrom(settings);
		m_topLeftCornerLonSettings.loadSettingsFrom(settings);
		m_topLeftCornerLatSettings.loadSettingsFrom(settings);
		m_bottomRightCornerLonSettings.loadSettingsFrom(settings);
		m_bottomRightCornerLatSettings.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		LOGGER.info("Invoke validateSettings");
		
		/*
		 * Check if the settings could be applied to our model e.g. if the user provided
		 * format String is empty. In this case we do not need to check as this is
		 * already handled in the dialog. Do not actually set any values of any member
		 * variables.
		 */
		m_pairTypeSettings.validateSettings(settings);

		m_geometryColSettings.validateSettings(settings);
		m_topLeftCornerLonSettings.validateSettings(settings);
		m_topLeftCornerLatSettings.validateSettings(settings);
		m_bottomRightCornerLonSettings.validateSettings(settings);
		m_bottomRightCornerLatSettings.validateSettings(settings);
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

