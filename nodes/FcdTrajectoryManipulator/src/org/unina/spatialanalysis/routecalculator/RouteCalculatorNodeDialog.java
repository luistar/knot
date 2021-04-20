package org.unina.spatialanalysis.routecalculator;

import java.util.Arrays;

import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * @author Sinogrante Principe
 */
public class RouteCalculatorNodeDialog extends DefaultNodeSettingsPane {
	
	private static final String [] avaibleRoutingServices = {"OSRM"};
	
	private static final String [] OSRMAvailableModes = {"shortest"};
	
	private static final String PAIR_TYPE_TOOLTIP = "<html>"
												  + "This setting informs the node about the type of points present"
												  + "<br>in the input data.</br>"
												  + "</html>";
	
	private static final String OSRM_SERVER_TOOLTIP = "<html>"
													+ "The url inserted here must correspond to a running instance"
													+ "<br>of the OSRM routing machine.</br>" 
													+ "</html>";
	
	private static final String ROUTING_MODE_TOOLTIP = "<html>"
													+ "Choosing match means that a set of positions belonging to the same route"
													+ "<br>will be all used to found the most likely traversed path.</br>"
													+ "<br>Choosing shortest means that only the starting and ending position will be</br>"
													+ "<br>used to determine the shortest possible path between them.</br>"
													+ "</html>";
	
	private static final String MIN_ROUTE_DISTANCE_TOOLTIP =  "<html>"
															+ "This value (meters) is used to determine wether a route should be considered valid."
															+ "<br>Routes that are less long than this value will be discarded.</br>"
															+ "</html>";
		
	private static final String MIN_ROUTE_DURATION_TOOLTIP = "<html>"
															+ "This value (minutes) is used to determine wether a route should be considered valid."
															+ "<br>Routes that last less than this value will be discarded.</br>"
															+ "</html>";
	
	
	/**
	 * New dialog pane for configuring the node. The dialog created here
	 * will show up when double clicking on a node in KNIME Analytics Platform.
	 */
    protected RouteCalculatorNodeDialog() {
        super();
        
        
        ToolTipManager.sharedInstance().setDismissDelay(15000);
		
        SettingsModelString routingService = RouteCalculatorNodeModel.createRoutingServiceSettings();
        DialogComponentStringSelection routingServiceSelector = new DialogComponentStringSelection(routingService, "Select the Routing Machine: ", avaibleRoutingServices);
        this.addDialogComponent(routingServiceSelector);
		
		SettingsModelString osrmHost = RouteCalculatorNodeModel.createRoutingServiceHostSettings();
		DialogComponentString osrmServerSelector = new DialogComponentString(osrmHost, "Insert OSRM server: ", true, 40);
		osrmServerSelector.setToolTipText(OSRM_SERVER_TOOLTIP);
		osrmHost.setEnabled(true);
		addDialogComponent(osrmServerSelector);
		
		SettingsModelString routingMode = RouteCalculatorNodeModel.createRoutingModeSettings();
		DialogComponentStringSelection routingModeSelector = new DialogComponentStringSelection(routingMode, "Routing mode :", OSRMAvailableModes);
		routingModeSelector.setToolTipText(ROUTING_MODE_TOOLTIP);
		routingMode.setEnabled(true);
		addDialogComponent(routingModeSelector);
		
		SettingsModelString coordinatePairType = RouteCalculatorNodeModel.createCoordinatePairTypeSettings();
		DialogComponentStringSelection pairTypeSelector = new DialogComponentStringSelection(coordinatePairType, "Select the coordinates pair type of the input data:","{lat,lon}","{lon,lat}") ;
		pairTypeSelector.setToolTipText(PAIR_TYPE_TOOLTIP);
		coordinatePairType.setEnabled(true);
		addDialogComponent(pairTypeSelector);	
		
		SettingsModelIntegerBounded minRouteDistance = RouteCalculatorNodeModel.createMinRouteDistanceSettings();
		DialogComponentNumberEdit minRouteDistanceSelector = new DialogComponentNumberEdit(minRouteDistance, "Ignore al routes that are less than this value (meters) long : ", 10);
		minRouteDistanceSelector.setToolTipText(MIN_ROUTE_DISTANCE_TOOLTIP);
		minRouteDistance.setEnabled(true);
		addDialogComponent(minRouteDistanceSelector);
		
		SettingsModelIntegerBounded minRouteDuration = RouteCalculatorNodeModel.createMinRouteDurationSettings();
		DialogComponentNumberEdit minRouteDurationSelector = new DialogComponentNumberEdit(minRouteDuration, "Ignore al routes that are less than this value (minutes) long : ", 10);
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

