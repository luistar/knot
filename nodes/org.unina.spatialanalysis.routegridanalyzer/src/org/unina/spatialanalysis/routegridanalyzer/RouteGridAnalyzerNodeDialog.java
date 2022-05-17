package org.unina.spatialanalysis.routegridanalyzer;

import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.date.DateAndTimeValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * @author Sinogrante Principe
 */
public class RouteGridAnalyzerNodeDialog extends DefaultNodeSettingsPane {

    @SuppressWarnings("unchecked")
	protected RouteGridAnalyzerNodeDialog() {
        super();
        
        this.createNewGroup("Column Selector");

    	SettingsModelColumnName colIDColSettingModel = RouteGridAnalyzerNodeModel.createColIDSettings();
		DialogComponentColumnNameSelection colIDColumnSelection = new DialogComponentColumnNameSelection(colIDColSettingModel, "Vehicle ID Column", 0, false, true, IntValue.class);
		addDialogComponent(colIDColumnSelection);
        
		SettingsModelColumnName beginAtColSettingModel = RouteGridAnalyzerNodeModel.createColBeginAtSettings();
		DialogComponentColumnNameSelection beginAtColumnSelection = new DialogComponentColumnNameSelection(
				beginAtColSettingModel, "Start Visit Timestamp Column", 0, false, true, DateAndTimeValue.class);
		addDialogComponent(beginAtColumnSelection); 
        
		SettingsModelColumnName endAtColSettingModel = RouteGridAnalyzerNodeModel.createColEndAtSettings();
		DialogComponentColumnNameSelection endAtColumnSelection = new DialogComponentColumnNameSelection(
				endAtColSettingModel, "End Visit Timestamp Column", 0, false, true, DateAndTimeValue.class);
		addDialogComponent(endAtColumnSelection);
		
		SettingsModelColumnName geomtryColSettingModel = RouteGridAnalyzerNodeModel.createColGeometrySettings();
		DialogComponentColumnNameSelection geomtryColumnSelection = new DialogComponentColumnNameSelection(
				geomtryColSettingModel, "Geometry Column", 0, false, true, StringValue.class);
		addDialogComponent(geomtryColumnSelection);
		
        this.createNewGroup("Configuration");
        
		SettingsModelString minLat = RouteGridAnalyzerNodeModel.createMinLatSetting();
		addDialogComponent(new DialogComponentString(minLat, "Minimum Y: ", true, 20));
		SettingsModelString maxLat = RouteGridAnalyzerNodeModel.createMaxLatSetting();
		addDialogComponent(new DialogComponentString(maxLat, "Maximum Y: ", true, 20));
		SettingsModelString minLon = RouteGridAnalyzerNodeModel.createMinLonSetting();
		addDialogComponent(new DialogComponentString(minLon, "Minimum X: ", true, 20));
		SettingsModelString maxLon = RouteGridAnalyzerNodeModel.createMaxLonSetting();
		addDialogComponent(new DialogComponentString(maxLon, "Maximum X: ", true, 20));
		SettingsModelIntegerBounded numberRows = RouteGridAnalyzerNodeModel.createNumberRowsSetting();
		addDialogComponent(new DialogComponentNumber(numberRows, "Number of grid rows: ", 1));
		SettingsModelIntegerBounded numberColumns = RouteGridAnalyzerNodeModel.createNumberColumnsSetting();
		addDialogComponent(new DialogComponentNumber(numberColumns, "Number of grids columns: ", 1));
		SettingsModelIntegerBounded minutesBetween = RouteGridAnalyzerNodeModel.createMinutesSetting();
		addDialogComponent(new DialogComponentNumber(minutesBetween, "Minimum number of minutes between distinct visits from the same vehicle: ", 0));
    }
}

