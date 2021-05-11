package org.unina.spatialanalysis.RouteStepAnalyzer;

import java.awt.Label;

import javax.swing.ToolTipManager;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

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
			+ "This value (seconds) is used to determine wether two subsegquent visit to the same"
			+ "<br>visit to the same segment should be considered as distinct.</br>"
			+ "<br>Visits that are less than this time apart from the last valid visit will be discarded.</br>"
			+ "</html>";
	/**
	 * New dialog pane for configuring the node. The dialog created here
	 * will show up when double clicking on a node in KNIME Analytics Platform.
	 */
    protected RouteStepAnalyzerNodeDialog() {
        super();
        
        ToolTipManager.sharedInstance().setDismissDelay(15000);
        
		SettingsModelIntegerBounded minTimeBetween = RouteStepAnalyzerNodeModel.createMinTimeSettings();
		DialogComponentNumberEdit minTimeSelector = new DialogComponentNumberEdit(minTimeBetween, "Ignore all visits that are less than this value(seconds) away from the last one : ", 10);
		minTimeSelector.setToolTipText(MIN_TIME_TOOLTIP);
		minTimeBetween.setEnabled(true);
		addDialogComponent(minTimeSelector);
		
    }
}

