package org.unina.spatialanalysis.mapmatcher;

import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.date.DateAndTimeValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * This is an example implementation of the node dialog of the
 * "MapMatcher" node.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}. In general, one can create an
 * arbitrary complex dialog using Java Swing.
 * 
 * @author Sinogrante Principe
 */
public class MapMatcherNodeDialog extends DefaultNodeSettingsPane {
	
	private static final String [] coordinatePairTypes = {"{lat,lon}", "{lon,lat}"};
	
	private static final String [] availableRouteDecoderServices = {"OSRM"};
	
	private static final String PAIR_TYPE_TOOLTIP = "<html>"
			  + "This setting informs the node about the type of points present"
			  + "<br>in the input data.</br>"
			  + "</html>";
	
	private static final String OSM_FILE_PICKER_TOOLTIP = "<html>"
			  + "The path of the file containing the OSM map data (.osm file)."
			  + "</html>";
	
	private static final String ROUTE_DECODER_STRATEGY_TOOLTIP = "<html>"
			+ "Choose the strategy to be used to match "
			+ "<br>the routes to the road segments.</br>" 
			+ "</html>";
	
	private static final String OSRM_SERVER_TOOLTIP = "<html>"
			+ "The url inserted here must correspond to a running instance"
			+ "<br>of the OSRM routing machine.</br>" 
			+ "</html>";

	private static final String INCLUDE_NEVER_VISITED_TOOLTIP = "<html>"
			  + "If checked, road segments that were never visited by the probe vehicles will be included in the output table."
			  + "<br>Otherwise, only segments that are visted at least one by a vehicle will be included."
			  + "</html>";
	
    @SuppressWarnings("unchecked")
	protected MapMatcherNodeDialog() {

    	super();
        
        ToolTipManager.sharedInstance().setDismissDelay(15000);
        
        this.createNewGroup("Coloumn Selector");
        
		SettingsModelColumnName idColSettingModel = MapMatcherNodeModel.createColIDSettings();
		DialogComponentColumnNameSelection idColumnSelection = new DialogComponentColumnNameSelection(
				idColSettingModel, "Vehicle ID Column", 0, false, true, IntValue.class);
		addDialogComponent(idColumnSelection);
        
		SettingsModelColumnName beginAtColSettingModel = MapMatcherNodeModel.createColIDSettings();
		DialogComponentColumnNameSelection beginAtColumnSelection = new DialogComponentColumnNameSelection(
				beginAtColSettingModel, "Timestamp Column", 0, false, true, DateAndTimeValue.class);
		addDialogComponent(beginAtColumnSelection);

		SettingsModelColumnName geomtryColSettingModel = MapMatcherNodeModel.createColIDSettings();
		DialogComponentColumnNameSelection geomtryColumnSelection = new DialogComponentColumnNameSelection(
				geomtryColSettingModel, "Geometry Column", 0, false, true, StringValue.class);
		addDialogComponent(geomtryColumnSelection);
		
        this.createNewGroup("Configuration"); 
        
        SettingsModelString osmPath = MapMatcherNodeModel.createOsmDataPath();
        DialogComponentFileChooser osmPathFilePicker = new DialogComponentFileChooser(osmPath, "", ".osm");
        osmPathFilePicker.setToolTipText(OSM_FILE_PICKER_TOOLTIP);;
		addDialogComponent(osmPathFilePicker);
		
		//this.createNewGroup("Map Matching Strategy");
		
		SettingsModelString routeDecoderService = MapMatcherNodeModel.createRouteDecoderServiceSettings();
		DialogComponentStringSelection routeDecoderServicePicker = new DialogComponentStringSelection(routeDecoderService, "Select a map matching strategy: ", availableRouteDecoderServices);
		routeDecoderServicePicker.setToolTipText(ROUTE_DECODER_STRATEGY_TOOLTIP);
		addDialogComponent(routeDecoderServicePicker);
		
		SettingsModelString decoderHost = MapMatcherNodeModel.createRouteDecoderHostSettings();
		DialogComponentString decoderHostField = new DialogComponentString(decoderHost, "Insert the routing service endpoint: ", true, 40);
		decoderHostField.setToolTipText(OSRM_SERVER_TOOLTIP);
		addDialogComponent(decoderHostField);
		
		SettingsModelString coordinatePairType = MapMatcherNodeModel.createCoordinatePairTypeSettings();
		DialogComponentStringSelection coordinatePairTypePicker = new DialogComponentStringSelection(coordinatePairType, "Coordinate input format:",coordinatePairTypes);
		coordinatePairTypePicker.setToolTipText(PAIR_TYPE_TOOLTIP);
		addDialogComponent(coordinatePairTypePicker);
		
		routeDecoderService.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				String decoderService = routeDecoderService.getStringValue();
				switch(decoderService) {
					case("OSRM"):
						coordinatePairType.setEnabled(true);
						decoderHost.setEnabled(true);
						decoderHostField.setToolTipText(OSRM_SERVER_TOOLTIP);
				}
			}
			
		});
		
		
		//this.createNewGroup("Map Decoding Strategy");
		
		SettingsModelBoolean includeNeverVisited = MapMatcherNodeModel.createIncludeNeverVisitedSetting();
		DialogComponentBoolean includeNeverVisitedPicker = new DialogComponentBoolean(includeNeverVisited, "Include road segments that were never visited to the output table: ");
		includeNeverVisitedPicker.setToolTipText(INCLUDE_NEVER_VISITED_TOOLTIP);
		addDialogComponent(includeNeverVisitedPicker);
    }
}

