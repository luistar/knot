package org.unina.spatialanalysis.routestepanalyzer;

import javax.swing.ToolTipManager;

import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.date.DateAndTimeValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * This is an example implementation of the node dialog of the
 * "RouteStepAnalyzer" node.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}. In general, one can create an
 * arbitrary complex dialog using Java Swing.
 * 
 * @author Sinogrante Principe
 */
public class RouteStepAnalyzerNodeDialog extends DefaultNodeSettingsPane {

	private static final String MIN_TIME_TOOLTIP = "<html>"
			+ "This value (seconds) is used to determine whether two subsequent visits to the same"
			+ "<br>segment should be considered as distinct.</br>"
			+ "<br>Visits that are less than this time apart from the last valid visit will be discarded.</br>"
			+ "</html>";
	/**
	 * New dialog pane for configuring the node. The dialog created here
	 * will show up when double clicking on a node in KNIME Analytics Platform.
	 */
    @SuppressWarnings("unchecked")
	protected RouteStepAnalyzerNodeDialog() {
        super();
        
        ToolTipManager.sharedInstance().setDismissDelay(15000);
        
        this.createNewGroup("Column Selector");

    	SettingsModelColumnName colIDColSettingModel = RouteStepAnalyzerNodeModel.createColIDSettings();
		DialogComponentColumnNameSelection colIDColumnSelection = new DialogComponentColumnNameSelection(colIDColSettingModel, "ID Column", 0, false, true, IntValue.class);
		addDialogComponent(colIDColumnSelection);
        
		SettingsModelColumnName beginAtColSettingModel = RouteStepAnalyzerNodeModel.createColBeginAtSettings();
		DialogComponentColumnNameSelection beginAtColumnSelection = new DialogComponentColumnNameSelection(
				beginAtColSettingModel, "Begin At Column", 0, false, true, DateAndTimeValue.class);
		addDialogComponent(beginAtColumnSelection);

		SettingsModelColumnName endAtColSettingModel = RouteStepAnalyzerNodeModel.createColEndAtSettings();
		DialogComponentColumnNameSelection endAtColumnSelection = new DialogComponentColumnNameSelection(
				endAtColSettingModel, "End At Column", 0, false, true, DateAndTimeValue.class);
		addDialogComponent(endAtColumnSelection);

    	SettingsModelColumnName originIDColSettingModel = RouteStepAnalyzerNodeModel.createColOriginIDSettings();
		DialogComponentColumnNameSelection originIDColumnSelection = new DialogComponentColumnNameSelection(originIDColSettingModel, "Origin ID Column", 0, false, true, IntValue.class);
		addDialogComponent(originIDColumnSelection);
		
    	SettingsModelColumnName destIDColSettingModel = RouteStepAnalyzerNodeModel.createColDestIDSettings();
		DialogComponentColumnNameSelection destIDColumnSelection = new DialogComponentColumnNameSelection(destIDColSettingModel, "Destionation ID Column", 0, false, true, IntValue.class);
		addDialogComponent(destIDColumnSelection);
		
    	SettingsModelColumnName tagsColSettingModel = RouteStepAnalyzerNodeModel.createColTagsSettings();
		DialogComponentColumnNameSelection tagsColumnSelection = new DialogComponentColumnNameSelection(tagsColSettingModel, "Tags Column", 0, false, true, StringValue.class);
		addDialogComponent(tagsColumnSelection);
				
		SettingsModelColumnName geomtryColSettingModel = RouteStepAnalyzerNodeModel.createColGeometrySettings();
		DialogComponentColumnNameSelection geomtryColumnSelection = new DialogComponentColumnNameSelection(
				geomtryColSettingModel, "Geometry Column", 0, false, true, StringValue.class);
		addDialogComponent(geomtryColumnSelection);
		
        this.createNewGroup("Configuration");
		
		SettingsModelIntegerBounded minTimeBetween = RouteStepAnalyzerNodeModel.createMinTimeSettings();
		DialogComponentNumberEdit minTimeSelector = new DialogComponentNumberEdit(minTimeBetween, "Minimum number of seconds between distinct visits: ", 10);
		minTimeSelector.setToolTipText(MIN_TIME_TOOLTIP);
		minTimeBetween.setEnabled(true);
		addDialogComponent(minTimeSelector);
		
    }
}

