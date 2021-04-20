package org.unina.spatialanalysis.routegridanalyzer;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * @author Sinogrante Principe
 */
public class RouteGridAnalyzerNodeDialog extends DefaultNodeSettingsPane {

    protected RouteGridAnalyzerNodeDialog() {
        super();
		SettingsModelString minLat = RouteGridAnalyzerNodeModel.createMinLatSetting();
		addDialogComponent(new DialogComponentString(minLat, "Minimum Latitude: ", true, 20));
		SettingsModelString maxLat = RouteGridAnalyzerNodeModel.createMaxLatSetting();
		addDialogComponent(new DialogComponentString(maxLat, "Maximum Latitude: ", true, 20));
		SettingsModelString minLon = RouteGridAnalyzerNodeModel.createMinLonSetting();
		addDialogComponent(new DialogComponentString(minLon, "Minimum Longitude: ", true, 20));
		SettingsModelString maxLon = RouteGridAnalyzerNodeModel.createMaxLonSetting();
		addDialogComponent(new DialogComponentString(maxLon, "Maximum Longitude: ", true, 20));
		SettingsModelIntegerBounded numberRows = RouteGridAnalyzerNodeModel.createNumberRowsSetting();
		addDialogComponent(new DialogComponentNumber(numberRows, "Number of grid's rows : ", 1));
		SettingsModelIntegerBounded numberColumns = RouteGridAnalyzerNodeModel.createNumberColumnsSetting();
		addDialogComponent(new DialogComponentNumber(numberColumns, "Number of grids's columns : ", 1));
		SettingsModelIntegerBounded minutesBetween = RouteGridAnalyzerNodeModel.createMinutesSetting();
		addDialogComponent(new DialogComponentNumber(minutesBetween, "Minutes between distinct recordings : ", 0));
    }
}

