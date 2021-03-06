package org.unina.spatialanalysis.trajectoryassigner;

import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * This is an example implementation of the node dialog of the
 * "TrajectoryAssigner" node.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}. In general, one can create an
 * arbitrary complex dialog using Java Swing.
 * 
 * @author Sinogrante Principe
 */
public class TrajectoryAssignerNodeDialog extends DefaultNodeSettingsPane {
	
	private static final String PAIR_TYPE_TOOLTIP = "<html>"
			  + "This setting is used to specify the coordinate input format to be used."
			  + "<br>Both (lat, lon) and (lon, lat) formats are supported</br>"
			  + "</html>";
	
	private static final String ALLOW_SPAWN_MULTIPLE_DAYS = "<html>"
														  + "If true, when assigning positions to a route, positions recorded"
														  + "<br>in different days can belong to the same route. If false, positions</br>"
														  + "<br>recorded in different days will always belong to different routes</br>"
														  + "</html>";
	
	private static final String MINUTES_BETWEEN_TOOLTIP = "<html>"
														+ "This value is used to determine when the stream of FCD should be split in different"
														+ "<br>trajectories. If a vehicle is stationary for a number of minutes greater than</br>"
														+ "<br>the value inserted here, then the trajectory is split accordingly.</br>"
														+ "</html>";
	
	private static final String MINIMUM_RECORDING_IN_ROUTE = "<html>"
															+ "This value is used to determine when a route should be discarded."
															+ "<br>If a trajectory contains less than the specified number of GPS positions"
															+ "<br>it will be ignored.</br>"
															+ "</html>";
	
	/**
	 * New dialog pane for configuring the node. The dialog created here
	 * will show up when double clicking on a node in KNIME Analytics Platform.
	 */
    @SuppressWarnings("unchecked")
	protected TrajectoryAssignerNodeDialog() {
        super();
		  
        ToolTipManager.sharedInstance().setDismissDelay(15000);
       
        this.createNewGroup("Column Selector");
        
    	SettingsModelColumnName colIDSettingModel = TrajectoryAssignerNodeModel.createColIDSettings();
		DialogComponentColumnNameSelection colIDColumnSelection = new DialogComponentColumnNameSelection(colIDSettingModel, "Vehicle ID Column", 0, false, true, IntValue.class);
		addDialogComponent(colIDColumnSelection);
        
    	SettingsModelColumnName colTimestampSettingModel = TrajectoryAssignerNodeModel.createColTimestampSettings();
		DialogComponentColumnNameSelection timestampColumnSelection = new DialogComponentColumnNameSelection(colTimestampSettingModel, "Timestamp Column", 0, false, true, StringValue.class);
		addDialogComponent(timestampColumnSelection);

    	SettingsModelColumnName colGeomSettingModel = TrajectoryAssignerNodeModel.createColGeometrySettings();
		DialogComponentColumnNameSelection geomColumnSelection = new DialogComponentColumnNameSelection(colGeomSettingModel, "Geometry Column", 0, false, true, StringValue.class);
		addDialogComponent(geomColumnSelection);

        this.createNewGroup("Configuration");
        
        SettingsModelString routeAssignerMode = TrajectoryAssignerNodeModel.createRouteAssignerSettings();
        DialogComponentStringSelection routeAssignerSelector = new DialogComponentStringSelection(routeAssignerMode, "Partitioning strategy:","default");
        this.addDialogComponent(routeAssignerSelector);
        
    	SettingsModelString coordinatePairType = TrajectoryAssignerNodeModel.createCoordinatePairTypeSettings();
		DialogComponentStringSelection pairTypeSelector = new DialogComponentStringSelection(coordinatePairType, "Coordinate input format:","{lat,lon}","{lon,lat}") ;
		pairTypeSelector.setToolTipText(PAIR_TYPE_TOOLTIP);
		coordinatePairType.setEnabled(true);
		addDialogComponent(pairTypeSelector);	
        
    	SettingsModelBoolean allowSpawnMultipleDays = TrajectoryAssignerNodeModel.createSpawnMultipleDaysSettings();
		DialogComponentBoolean allowSpawnMultipleDaysSelector = (new DialogComponentBoolean(allowSpawnMultipleDays, "Allow trajectories spanning over multiple days"));
		allowSpawnMultipleDaysSelector.setToolTipText(ALLOW_SPAWN_MULTIPLE_DAYS);
		allowSpawnMultipleDays.setEnabled(true);
		addDialogComponent(allowSpawnMultipleDaysSelector);
        
		SettingsModelIntegerBounded minutesBetween = TrajectoryAssignerNodeModel.createMaxTimeBetweenSettings();
		DialogComponentNumberEdit minutesBetweenSelector = new DialogComponentNumberEdit(minutesBetween, "Minimum amount of minutes on hold to split trajectories: ", 10);
		minutesBetweenSelector.setToolTipText(MINUTES_BETWEEN_TOOLTIP);
		minutesBetween.setEnabled(true);
		addDialogComponent(minutesBetweenSelector);
		
		SettingsModelIntegerBounded minimumRecordings = TrajectoryAssignerNodeModel.createMinimumRecordingsSettings();
		DialogComponentNumberEdit minimumRecordingsSelector = new DialogComponentNumberEdit(minimumRecordings, "Discard trajectories having less points than: ", 10);
		minimumRecordingsSelector.setToolTipText(MINIMUM_RECORDING_IN_ROUTE);
		addDialogComponent(minimumRecordingsSelector);
		
		routeAssignerMode.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				switch(routeAssignerMode.getStringValue()) {
					case ("default"):{
						allowSpawnMultipleDays.setEnabled(true);
						minutesBetween.setEnabled(true);
						minimumRecordings.setEnabled(true);
						break;
					}
				}
			}
		});
    }
}

