package org.unina.spatialanalysis.routecalculator;

import java.util.Arrays;

import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * @author Sinogrante Principe
 */
public class RouteCalculatorNodeDialog extends DefaultNodeSettingsPane {
	
	private static final String [] avaibleRoutingServices = {"OSRM"};
	
	private static final String [] OSRMAvailableModes = {"shortest"};
	
	private static final String PAIR_TYPE_TOOLTIP = "<html>"
			  + "This setting is used to specify the coordinate input format to be used."
			  + "<br>Both (lat, lon) and (lon, lat) formats are supported</br>"
			  + "</html>";
	
	private static final String OSRM_SERVER_TOOLTIP = "<html>"
													+ "The url inserted here must correspond to a running instance"
													+ "<br>of the OSRM routing machine.</br>" 
													+ "</html>";
	
	private static final String ROUTING_MODE_TOOLTIP = "<html>"
													+ "Select a routing strategy. Currently, only the <i>shortest</i> strategy is supported."
													+ "<br>This strategy considers only the first and the last points in the original trajectory,</br>"
													+ "<br>and computes the shortest path between them. If the original trajectory contains more than two positions, </br>"
													+ "the additional positions will be ignored.</html>";
	
	private static final String MIN_ROUTE_DISTANCE_TOOLTIP =  "<html>"
															+ "This value (in meters) is used to determine wether a route should be discarded."
															+ "<br>Routes that are shorter than this value will be discarded.</br>"
															+ "</html>";
		
	private static final String MIN_ROUTE_DURATION_TOOLTIP = "<html>"
															+ "This value (in minutes) is used to determine wether a route should be discarded."
															+ "<br>Routes that last less than this value will be discarded.</br>"
															+ "</html>";
	
	
	/**
	 * New dialog pane for configuring the node. The dialog created here
	 * will show up when double clicking on a node in KNIME Analytics Platform.
	 */
    @SuppressWarnings("unchecked")
	protected RouteCalculatorNodeDialog() {
        super();

        ToolTipManager.sharedInstance().setDismissDelay(15000);

        this.createNewGroup("Column Selector");

        SettingsModelColumnName colownerIDColSettingModel = RouteCalculatorNodeModel.createColOwnerIDSettings();
		DialogComponentColumnNameSelection colownerIDColumnSelection = new DialogComponentColumnNameSelection(colownerIDColSettingModel, "Vehicle ID Column", 0, false, true, IntValue.class);
		addDialogComponent(colownerIDColumnSelection);

        SettingsModelColumnName colRouteIDColSettingModel = RouteCalculatorNodeModel.createColRouteIDSettings();
		DialogComponentColumnNameSelection colRouteIDColumnSelection = new DialogComponentColumnNameSelection(colRouteIDColSettingModel, "Trajectory ID Column", 0, false, true, IntValue.class);
		addDialogComponent(colRouteIDColumnSelection);
		
    	SettingsModelColumnName colTimestampSettingModel = RouteCalculatorNodeModel.createColTimestampSettings();
		DialogComponentColumnNameSelection timestampColumnSelection = new DialogComponentColumnNameSelection(colTimestampSettingModel, "Timestamp Column", 0, false, true, StringValue.class);
		addDialogComponent(timestampColumnSelection);
		
		SettingsModelColumnName geomtryColSettingModel = RouteCalculatorNodeModel.createColGeometrySettings();
		DialogComponentColumnNameSelection geomtryColumnSelection = new DialogComponentColumnNameSelection(
				geomtryColSettingModel, "Geometry Column", 0, false, true, StringValue.class);
		addDialogComponent(geomtryColumnSelection);
        
        this.createNewGroup("Configuration");
        
        SettingsModelString routingService = RouteCalculatorNodeModel.createRoutingServiceSettings();
        DialogComponentStringSelection routingServiceSelector = new DialogComponentStringSelection(routingService, "Select the routing service: ", avaibleRoutingServices);
        this.addDialogComponent(routingServiceSelector);
		
		SettingsModelString osrmHost = RouteCalculatorNodeModel.createRoutingServiceHostSettings();
		DialogComponentString osrmServerSelector = new DialogComponentString(osrmHost, "Routing Service endpoint: ", true, 40);
		osrmServerSelector.setToolTipText(OSRM_SERVER_TOOLTIP);
		osrmHost.setEnabled(true);
		addDialogComponent(osrmServerSelector);
		
		SettingsModelString routingMode = RouteCalculatorNodeModel.createRoutingModeSettings();
		DialogComponentStringSelection routingModeSelector = new DialogComponentStringSelection(routingMode, "Routing strategy:", OSRMAvailableModes);
		routingModeSelector.setToolTipText(ROUTING_MODE_TOOLTIP);
		routingMode.setEnabled(true);
		addDialogComponent(routingModeSelector);
		
		SettingsModelString coordinatePairType = RouteCalculatorNodeModel.createCoordinatePairTypeSettings();
		DialogComponentStringSelection pairTypeSelector = new DialogComponentStringSelection(coordinatePairType, "Coordinate input format:","{lat,lon}","{lon,lat}") ;
		pairTypeSelector.setToolTipText(PAIR_TYPE_TOOLTIP);
		coordinatePairType.setEnabled(true);
		addDialogComponent(pairTypeSelector);	
		
		SettingsModelIntegerBounded minRouteDistance = RouteCalculatorNodeModel.createMinRouteDistanceSettings();
		DialogComponentNumberEdit minRouteDistanceSelector = new DialogComponentNumberEdit(minRouteDistance, "Discard routes shorter than (meters): ", 10);
		minRouteDistanceSelector.setToolTipText(MIN_ROUTE_DISTANCE_TOOLTIP);
		minRouteDistance.setEnabled(true);
		addDialogComponent(minRouteDistanceSelector);
		
		SettingsModelIntegerBounded minRouteDuration = RouteCalculatorNodeModel.createMinRouteDurationSettings();
		DialogComponentNumberEdit minRouteDurationSelector = new DialogComponentNumberEdit(minRouteDuration, "Discard routes shorter than (minutes): ", 10);
		minRouteDurationSelector.setToolTipText(MIN_ROUTE_DURATION_TOOLTIP);
		minRouteDuration.setEnabled(true);
		addDialogComponent(minRouteDurationSelector);
		
		
		routingService.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				switch(routingService.getStringValue()) {
					case("OSRM"):{
						osrmHost.setEnabled(true);
						routingModeSelector.replaceListItems(Arrays.asList(OSRMAvailableModes), "shortest");
						routingMode.setEnabled(true);
						minRouteDistance.setEnabled(true);
						minRouteDuration.setEnabled(true);
						break;
					}
				}
			}
		});
    }
    
}

